package com.sep.realvista.infrastructure.external.docusign;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.client.auth.OAuth;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.RecipientViewRequest;
import com.docusign.esign.model.RecipientPhoneAuthentication;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.SignHere;
import com.docusign.esign.model.Signer;
import com.docusign.esign.model.Tabs;
import com.docusign.esign.model.CompositeTemplate;
import com.docusign.esign.model.ServerTemplate;
import com.docusign.esign.model.InlineTemplate;
import com.docusign.esign.model.Text;
import com.docusign.esign.model.ViewUrl;
import com.sep.realvista.application.listing.contract.dto.LeaseTemplateData;
import com.sep.realvista.application.service.DocuSignService;
import com.sep.realvista.infrastructure.config.DocuSignConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

/**
 * Infrastructure implementation of {@link DocuSignService} using the DocuSign eSign Java SDK.
 * <p>
 * Handles JWT Grant authentication, envelope lifecycle, and embedded signing URLs.
 * Degrades gracefully when DocuSign is not configured ({@code isAvailable()} returns false).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocuSignServiceImpl implements DocuSignService {

    private static final String SIGNING_STATUS = "sent";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    /** DocuSign JWT scopes required for eSignature operations. */
    private static final List<String> JWT_SCOPES = List.of(
            OAuth.Scope_SIGNATURE, OAuth.Scope_IMPERSONATION
    );

    private final DocuSignConfig docuSignConfig;

    private ApiClient apiClient;
    private volatile long tokenExpiresAtMs = 0;

    @PostConstruct
    public void init() {
        if (!docuSignConfig.isAvailable()) {
            log.warn("DocuSign credentials are not configured. eSignature features will be disabled. "
                    + "Set DOCUSIGN_INTEGRATION_KEY, DOCUSIGN_USER_ID, DOCUSIGN_ACCOUNT_ID, and "
                    + "DOCUSIGN_RSA_PRIVATE_KEY environment variables to enable.");
            return;
        }

        try {
            apiClient = new ApiClient(docuSignConfig.getBaseUrl());
            apiClient.setOAuthBasePath(docuSignConfig.getAuthServer());
            log.info("DocuSign ApiClient initialized for base URL: {}", docuSignConfig.getBaseUrl());
        } catch (Exception e) {
            log.error("Failed to initialize DocuSign ApiClient: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return docuSignConfig.isAvailable() && apiClient != null;
    }

    // ── JWT Token Management ──────────────────────────────────────────────────

    /**
     * Ensures a valid access token is set on the ApiClient, refreshing if needed.
     * Tokens are cached until 5 minutes before expiry.
     */
    private synchronized void ensureAuthenticated() {
        long nowMs = System.currentTimeMillis();
        // Refresh 5 minutes before expiry
        if (nowMs < tokenExpiresAtMs - 300_000L) {
            return;
        }

        try {
            byte[] privateKeyBytes = docuSignConfig.getParsedRsaPrivateKey()
                    .getBytes(StandardCharsets.UTF_8);

            OAuth.OAuthToken token = apiClient.requestJWTUserToken(
                    docuSignConfig.getIntegrationKey(),
                    docuSignConfig.getUserId(),
                    JWT_SCOPES,
                    privateKeyBytes,
                    3600 // token TTL in seconds
            );

            apiClient.setAccessToken(token.getAccessToken(), token.getExpiresIn());
            tokenExpiresAtMs = nowMs + (token.getExpiresIn() * 1000L);
            log.debug("DocuSign JWT token refreshed, expires in {}s", token.getExpiresIn());
        } catch (Exception e) {
            throw new DocuSignException("Failed to authenticate with DocuSign: " + e.getMessage(), e);
        }
    }

    // ── Envelope Operations ───────────────────────────────────────────────────

    @Override
    public String createEnvelopeForSigning(
            byte[] documentBytes,
            String documentName,
            String signerEmail,
            String signerName,
            String signerPhone,
            String signerClientUserId) {

        if (!isAvailable()) {
            log.warn("DocuSign not available — skipping envelope creation for signer: {}", signerEmail);
            return null;
        }

        ensureAuthenticated();

        try {
            Signer signer = buildSigner(signerEmail, signerName, signerPhone, signerClientUserId, "1");
            EnvelopeDefinition envelope = buildEnvelopeDefinition(
                    documentBytes, documentName,
                    signer, SIGNING_STATUS
            );

            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            EnvelopeSummary summary = envelopesApi.createEnvelope(
                    docuSignConfig.getAccountId(), envelope
            );

            log.info("DocuSign envelope created: {} for signer: {}", summary.getEnvelopeId(), signerEmail);
            return summary.getEnvelopeId();

        } catch (ApiException e) {
            throw new DocuSignException("Failed to create DocuSign envelope: " + e.getMessage(), e);
        }
    }

    @Override
    public String getEmbeddedSigningUrl(
            String envelopeId,
            String signerEmail,
            String signerName,
            String signerClientUserId,
            String returnUrl) {

        if (!isAvailable()) {
            log.warn("DocuSign not available — cannot generate signing URL for envelope: {}", envelopeId);
            return null;
        }

        ensureAuthenticated();

        try {
            RecipientViewRequest viewRequest = new RecipientViewRequest();
            viewRequest.setEmail(signerEmail);
            viewRequest.setUserName(signerName);
            viewRequest.setClientUserId(signerClientUserId);
            viewRequest.setAuthenticationMethod("none");
            viewRequest.setReturnUrl(returnUrl);

            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            ViewUrl viewUrl = envelopesApi.createRecipientView(
                    docuSignConfig.getAccountId(), envelopeId, viewRequest
            );

            log.debug("Embedded signing URL generated for envelope: {}", envelopeId);
            return viewUrl.getUrl();

        } catch (ApiException e) {
            throw new DocuSignException("Failed to create recipient view: " + e.getMessage(), e);
        }
    }

    @Override
    public String getEnvelopeStatus(String envelopeId) {
        if (!isAvailable()) {
            return null;
        }

        ensureAuthenticated();

        try {
            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            return envelopesApi.getEnvelope(docuSignConfig.getAccountId(), envelopeId).getStatus();
        } catch (ApiException e) {
            log.error("Failed to get envelope status for {}: {}", envelopeId, e.getMessage());
            return null;
        }
    }

    @Override
    public byte[] downloadCompletedDocument(String envelopeId) {
        if (!isAvailable()) {
            log.warn("DocuSign not available — cannot download completed document for envelope: {}", envelopeId);
            return null;
        }

        ensureAuthenticated();

        try {
            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            byte[] documentBytes = envelopesApi.getDocument(
                    docuSignConfig.getAccountId(), envelopeId, "combined"
            );
            log.info("Downloaded completed DocuSign document for envelope: {}", envelopeId);
            return documentBytes;
        } catch (ApiException e) {
            throw new DocuSignException("Failed to download completed document: " + e.getMessage(), e);
        }
    }

    @Override
    public String addLandlordSigner(
            String envelopeId,
            byte[] documentBytes,
            String documentName,
            String signerEmail,
            String signerName,
            String signerPhone,
            String signerClientUserId) {

        if (!isAvailable()) {
            log.warn("DocuSign not available — cannot add landlord signer to envelope: {}", envelopeId);
            return null;
        }

        ensureAuthenticated();

        try {
            // Check current envelope status; if completed or voided, create a new envelope
            String status = getEnvelopeStatus(envelopeId);
            if ("completed".equalsIgnoreCase(status) || "voided".equalsIgnoreCase(status)) {
                log.info("Envelope {} is {}, creating new envelope for landlord", envelopeId, status);
                return createEnvelopeForSigning(
                        documentBytes, documentName,
                        signerEmail, signerName, signerPhone, signerClientUserId
                );
            }

            // Add landlord as a new recipient to the existing envelope (routing order 2)
            Signer landlordSigner = buildSigner(signerEmail, signerName, signerPhone, signerClientUserId, "2");

            Recipients recipients = new Recipients();
            recipients.setSigners(List.of(landlordSigner));

            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            envelopesApi.updateRecipients(docuSignConfig.getAccountId(), envelopeId, recipients);

            log.info("Landlord signer added to envelope: {}", envelopeId);
            return envelopeId;

        } catch (ApiException e) {
            throw new DocuSignException("Failed to add landlord signer: " + e.getMessage(), e);
        }
    }

    // ── Template-Based Envelope Creation ──────────────────────────────────────

    @Override
    public String createEnvelopeFromTemplate(String templateId, LeaseTemplateData data) {
        if (!isAvailable()) {
            log.warn("DocuSign not available — skipping template envelope creation");
            return null;
        }

        ensureAuthenticated();

        try {
            // Build tabs with dynamic field values for renter role
            Tabs renterTabs = new Tabs();
            renterTabs.setTextTabs(List.of(
                    buildTextTab("renterName", data.getRenterName()),
                    buildTextTab("landlordName", data.getLandlordName()),
                    buildTextTab("handoverDate", data.getHandoverDate()),
                    buildTextTab("leaseDurationMonths", data.getLeaseDurationMonths()),
                    buildTextTab("monthlyRent", data.getMonthlyRent()),
                    buildTextTab("monthlyRentByText", data.getMonthlyRentByText()),
                    buildTextTab("securityDeposit", data.getSecurityDeposit()),
                    buildTextTab("securityDepositByText", data.getSecurityDepositByText()),
                    buildTextTab("contractDayOfWeek", data.getContractDayOfWeek()),
                    buildTextTab("contractDay", data.getContractDay()),
                    buildTextTab("contractMonth", data.getContractMonth()),
                    buildTextTab("contractYear", data.getContractYear())
            ));

            // Build tabs with dynamic field values for landlord role
            Tabs landlordTabs = new Tabs();
            landlordTabs.setTextTabs(List.of(
                    buildTextTab("renterName", data.getRenterName()),
                    buildTextTab("landlordName", data.getLandlordName()),
                    buildTextTab("handoverDate", data.getHandoverDate()),
                    buildTextTab("leaseDurationMonths", data.getLeaseDurationMonths()),
                    buildTextTab("monthlyRent", data.getMonthlyRent()),
                    buildTextTab("monthlyRentByText", data.getMonthlyRentByText()),
                    buildTextTab("securityDeposit", data.getSecurityDeposit()),
                    buildTextTab("securityDepositByText", data.getSecurityDepositByText()),
                    buildTextTab("contractDayOfWeek", data.getContractDayOfWeek()),
                    buildTextTab("contractDay", data.getContractDay()),
                    buildTextTab("contractMonth", data.getContractMonth()),
                    buildTextTab("contractYear", data.getContractYear())
            ));

            // Landlord Signer (routing order 1 — signs first)
            Signer landlordSigner = new Signer();
            landlordSigner.setEmail(data.getLandlordEmail());
            landlordSigner.setName(data.getLandlordName());
            landlordSigner.setRoleName("landlord");
            landlordSigner.setRecipientId("1");
            landlordSigner.setRoutingOrder("1");
            landlordSigner.setClientUserId(data.getLandlordClientUserId());
            landlordSigner.setTabs(landlordTabs);

            if (StringUtils.hasText(data.getLandlordPhone())) {
                String formattedPhone = formatPhoneWithCountryCode(data.getLandlordPhone());
                RecipientPhoneAuthentication phoneAuth = new RecipientPhoneAuthentication();
                phoneAuth.setSenderProvidedNumbers(List.of(formattedPhone));
                landlordSigner.setPhoneAuthentication(phoneAuth);
                landlordSigner.setRequireIdLookup("true");
                landlordSigner.setIdCheckConfigurationName("Phone Auth $");
            }

            // Renter Signer (routing order 2 — signs after landlord)
            Signer renterSigner = new Signer();
            renterSigner.setEmail(data.getRenterEmail());
            renterSigner.setName(data.getRenterName());
            renterSigner.setRoleName("renter");
            renterSigner.setRecipientId("2");
            renterSigner.setRoutingOrder("2");
            renterSigner.setClientUserId(data.getRenterClientUserId());
            renterSigner.setTabs(renterTabs);

            if (StringUtils.hasText(data.getRenterPhone())) {
                String formattedPhone = formatPhoneWithCountryCode(data.getRenterPhone());
                RecipientPhoneAuthentication phoneAuth = new RecipientPhoneAuthentication();
                phoneAuth.setSenderProvidedNumbers(List.of(formattedPhone));
                renterSigner.setPhoneAuthentication(phoneAuth);
                renterSigner.setRequireIdLookup("true");
                renterSigner.setIdCheckConfigurationName("Phone Auth $");
            }

            Recipients recipients = new Recipients();
            recipients.setSigners(List.of(landlordSigner, renterSigner));

            InlineTemplate inlineTemplate = new InlineTemplate();
            inlineTemplate.setSequence("2");
            inlineTemplate.setRecipients(recipients);

            ServerTemplate serverTemplate = new ServerTemplate();
            serverTemplate.setSequence("1");
            serverTemplate.setTemplateId(templateId);

            CompositeTemplate compositeTemplate = new CompositeTemplate();
            compositeTemplate.setServerTemplates(List.of(serverTemplate));
            compositeTemplate.setInlineTemplates(List.of(inlineTemplate));

            EnvelopeDefinition envelope = new EnvelopeDefinition();
            envelope.setCompositeTemplates(List.of(compositeTemplate));
            envelope.setStatus(SIGNING_STATUS);

            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            EnvelopeSummary summary = envelopesApi.createEnvelope(
                    docuSignConfig.getAccountId(), envelope
            );

            log.info("DocuSign template envelope created: {} (template: {})", summary.getEnvelopeId(), templateId);
            return summary.getEnvelopeId();

        } catch (ApiException e) {
            throw new DocuSignException("Failed to create envelope from template: " + e.getMessage(), e);
        }
    }

    // ── Webhook HMAC Verification ─────────────────────────────────────────────

    @Override
    public boolean verifyWebhookSignature(byte[] payload, String hmacHeader) {
        String hmacKey = docuSignConfig.getWebhookHmacKey();
        if (!StringUtils.hasText(hmacKey)) {
            // HMAC not configured — allow all (log warning in production)
            log.debug("DocuSign webhook HMAC key not configured, skipping signature verification");
            return true;
        }

        if (!StringUtils.hasText(hmacHeader)) {
            log.warn("DocuSign webhook received without HMAC signature header");
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    hmacKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM
            );
            mac.init(secretKey);
            byte[] computed = mac.doFinal(payload);
            String computedBase64 = Base64.getEncoder().encodeToString(computed);

            boolean valid = computedBase64.equals(hmacHeader);
            if (!valid) {
                log.warn("DocuSign webhook HMAC signature mismatch");
            }
            return valid;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to verify DocuSign webhook HMAC: {}", e.getMessage(), e);
            return false;
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private EnvelopeDefinition buildEnvelopeDefinition(
            byte[] documentBytes,
            String documentName,
            Signer signer,
            String status) {

        // Build document
        Document document = new Document();
        document.setDocumentBase64(Base64.getEncoder().encodeToString(documentBytes));
        document.setName(documentName);
        document.setFileExtension("pdf");
        document.setDocumentId("1");

        // Build envelope
        EnvelopeDefinition envelope = new EnvelopeDefinition();
        envelope.setEmailSubject("Please sign your Lease Agreement – RealVista");
        envelope.setDocuments(List.of(document));
        envelope.setRecipients(new Recipients());
        envelope.getRecipients().setSigners(List.of(signer));
        envelope.setStatus(status);

        return envelope;
    }

    private Signer buildSigner(String email, String name, String phone, String clientUserId, String routingOrder) {
        // Sign-here tab placed at bottom of the last page
        SignHere signHere = new SignHere();
        signHere.setAnchorString("/sig1/");
        signHere.setAnchorUnits("pixels");
        signHere.setAnchorYOffset("10");
        signHere.setAnchorXOffset("20");
        // Fallback position if anchor string not found in PDF
        signHere.setPageNumber("1");
        signHere.setXPosition("100");
        signHere.setYPosition("700");

        Tabs tabs = new Tabs();
        tabs.setSignHereTabs(List.of(signHere));

        Signer signer = new Signer();
        signer.setEmail(email);
        signer.setName(name);
        signer.setClientUserId(clientUserId);  // Required for embedded signing
        signer.setRecipientId("1");
        signer.setRoutingOrder(routingOrder);
        signer.setTabs(tabs);

        if (StringUtils.hasText(phone)) {
            String formattedPhone = formatPhoneWithCountryCode(phone);
            RecipientPhoneAuthentication phoneAuth = new RecipientPhoneAuthentication();
            phoneAuth.setSenderProvidedNumbers(List.of(formattedPhone));
            signer.setPhoneAuthentication(phoneAuth);
            signer.setRequireIdLookup("true");
            signer.setIdCheckConfigurationName("Phone Auth $");
        }

        return signer;
    }

    private String formatPhoneWithCountryCode(String phone) {
        if (phone == null) {
            return null;
        }
        String formatted = phone.trim();
        // If it starts with 0 (e.g. 0912345678), convert it to +84
        if (formatted.startsWith("0") && formatted.length() > 9) {
            return "+84" + formatted.substring(1);
        }
        // If it already has +, return as is
        if (formatted.startsWith("+")) {
            return formatted;
        }
        // Otherwise prepend + for international format
        return "+" + formatted;
    }


    private Text buildTextTab(String tabLabel, String value) {
        Text text = new Text();
        text.setTabLabel(tabLabel);
        text.setValue(value != null ? value : "");
        return text;
    }
}
