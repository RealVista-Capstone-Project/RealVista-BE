package com.sep.realvista.infrastructure.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * DocuSign eSignature configuration loaded from application properties.
 * <p>
 * All values are sourced from environment variables via the {@code docusign.*} prefix.
 * The application functions without DocuSign (graceful degradation) if credentials
 * are not configured — use {@link #isAvailable()} to check before operations.
 */
@Configuration
@ConfigurationProperties(prefix = "docusign")
@Data
@Slf4j
public class DocuSignConfig {

    /** DocuSign Integration Key (OAuth2 Client ID). */
    private String integrationKey;

    /** GUID of the user whose credentials are used for JWT impersonation. */
    private String userId;

    /** DocuSign Account ID (numeric or GUID). */
    private String accountId;

    /**
     * RSA private key in PEM format (single line, with \\n for line breaks).
     * Set via DOCUSIGN_RSA_PRIVATE_KEY environment variable.
     */
    private String rsaPrivateKey;

    /** DocuSign REST API base URL. Defaults to demo (sandbox) environment. */
    private String baseUrl = "https://demo.docusign.net/restapi";

    /** DocuSign OAuth2 authorization server host. */
    private String authServer = "account-d.docusign.com";

    /** Frontend URL to redirect to after embedded signing completes. */
    private String returnUrl = "http://localhost:3000/leases/signing-complete";

    /** HMAC key for verifying DocuSign Connect webhook payloads. */
    private String webhookHmacKey;

    /** DocuSign template ID for lease agreements with pre-configured fields. */
    private String leaseTemplateId;

    /**
     * Returns true if the minimum required DocuSign credentials are configured.
     * Used for graceful degradation — if false, signing operations are disabled.
     */
    public boolean isAvailable() {
        return StringUtils.hasText(integrationKey)
                && StringUtils.hasText(userId)
                && StringUtils.hasText(accountId)
                && StringUtils.hasText(rsaPrivateKey);
    }

    /**
     * Returns true if DocuSign is available and a lease template ID is configured.
     * When true, the template-based signing flow is used instead of PDF upload.
     */
    public boolean isTemplateAvailable() {
        return isAvailable() && StringUtils.hasText(leaseTemplateId);
    }

    /**
     * Returns the RSA private key with literal \\n replaced by actual newlines,
     * allowing the key to be stored as a single-line environment variable.
     */
    public String getParsedRsaPrivateKey() {
        if (!StringUtils.hasText(rsaPrivateKey)) {
            return null;
        }
        return rsaPrivateKey.replace("\\n", "\n");
    }
}
