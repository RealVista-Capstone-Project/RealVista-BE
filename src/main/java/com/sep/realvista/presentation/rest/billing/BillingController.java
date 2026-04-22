package com.sep.realvista.presentation.rest.billing;

import com.sep.realvista.application.billing.dto.ActiveBoostPackageResponse;
import com.sep.realvista.application.billing.dto.ActiveFeatureSubscriptionResponse;
import com.sep.realvista.application.billing.dto.BoostPackageResponse;
import com.sep.realvista.application.billing.dto.CheckoutRequest;
import com.sep.realvista.application.billing.dto.CheckoutResponse;
import com.sep.realvista.application.billing.dto.FeaturePackageResponse;
import com.sep.realvista.application.billing.dto.TransactionResponse;
import com.sep.realvista.application.billing.dto.TransactionStatusResponse;
import com.sep.realvista.application.billing.service.BillingApplicationService;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Feature packages and boost checkout via PayOS and VNPay")
@Slf4j
public class BillingController {

    private final BillingApplicationService billingService;

    // -------------------------------------------------------------------------
    // Feature packages catalog (public)
    // -------------------------------------------------------------------------

    @GetMapping("/packages")
    @Operation(summary = "List all active feature packages")
    public ResponseEntity<ApiResponse<List<FeaturePackageResponse>>> getAllFeaturePackages() {
        return ResponseEntity.ok(ApiResponse.success(billingService.getFeaturePackages()));
    }

    @GetMapping("/plans/subscriptions")
    @Operation(summary = "List all active feature packages (legacy endpoint)")
    @Deprecated
    public ResponseEntity<ApiResponse<List<FeaturePackageResponse>>> getSubscriptionPlansLegacy() {
        return ResponseEntity.ok(ApiResponse.success(billingService.getFeaturePackages()));
    }

    @GetMapping("/packages/listings")
    @Operation(summary = "List active listing packages")
    public ResponseEntity<ApiResponse<List<FeaturePackageResponse>>> getListingPackages() {
        return ResponseEntity.ok(ApiResponse.success(billingService.getListingPackages()));
    }

    @GetMapping("/packages/3d-tours")
    @Operation(summary = "List active 3D tour packages")
    public ResponseEntity<ApiResponse<List<FeaturePackageResponse>>> get3dTourPackages() {
        return ResponseEntity.ok(ApiResponse.success(billingService.get3dTourPackages()));
    }

    @GetMapping("/packages/ai")
    @Operation(summary = "List active AI request packages")
    public ResponseEntity<ApiResponse<List<FeaturePackageResponse>>> getAiPackages() {
        return ResponseEntity.ok(ApiResponse.success(billingService.getAiPackages()));
    }

    @GetMapping("/plans/boosts")
    @Operation(summary = "List all active boost packages")
    public ResponseEntity<ApiResponse<List<BoostPackageResponse>>> getBoostPackages() {
        return ResponseEntity.ok(ApiResponse.success(billingService.getBoostPackages()));
    }

    // -------------------------------------------------------------------------
    // Checkout (authenticated)
    // -------------------------------------------------------------------------

    @PostMapping("/checkout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a payment link for a feature package or boost purchase")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            HttpServletRequest httpRequest
    ) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        try {
            log.info("Checkout request - traceId={}, userId={}, plan={}, method={}",
                    traceId, currentUser.getUserId(), request.getPlanCode(), request.getPaymentMethod());

            String clientIp = getClientIp(httpRequest);
            CheckoutResponse response = billingService.checkout(currentUser.getUserId(), request, clientIp);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payment link created", response));
        } finally {
            MDC.remove("traceId");
        }
    }

    // -------------------------------------------------------------------------
    // Transaction status polling (authenticated)
    // -------------------------------------------------------------------------

    @GetMapping("/transactions/{checkoutOrderId}/status")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Poll payment status for a checkout order")
    public ResponseEntity<ApiResponse<TransactionStatusResponse>> getCheckoutOrderStatus(
            @PathVariable String checkoutOrderId,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        try {
            UUID orderId = UUID.fromString(checkoutOrderId);
            TransactionStatusResponse status =
                    billingService.getCheckoutOrderStatus(orderId, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Checkout order not found");
        }
    }

    @PostMapping("/transactions/{checkoutOrderId}/sync-payos")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Sync PayOS payment status from PayOS API",
            description = "Calls PayOS GET /v2/payment-requests/{orderCode}. If PayOS reports "
                    + "payment settled, activates the package like the webhook would (useful when "
                    + "webhooks cannot reach localhost).")
    public ResponseEntity<ApiResponse<TransactionStatusResponse>> syncPayOsFromGateway(
            @PathVariable String checkoutOrderId,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        try {
            TransactionStatusResponse status =
                    billingService.syncPayOsFromCheckout(UUID.fromString(checkoutOrderId),
                            currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Checkout order not found");
        }
    }

    // -------------------------------------------------------------------------
    // My active feature subscriptions (authenticated)
    // -------------------------------------------------------------------------

    @GetMapping("/subscriptions/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the current user's active feature subscriptions")
    public ResponseEntity<ApiResponse<List<ActiveFeatureSubscriptionResponse>>> getMySubscriptions(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        var subscriptions = billingService.getMyFeatureSubscriptions(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @DeleteMapping("/subscriptions/{subscriptionId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cancel an active feature subscription owned by the current user")
    public ResponseEntity<ApiResponse<Void>> cancelMySubscription(
            @PathVariable UUID subscriptionId,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        billingService.cancelFeatureSubscription(currentUser.getUserId(), subscriptionId);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled", null));
    }

    @GetMapping("/subscriptions/me/listings")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the current user's active listing subscriptions")
    public ResponseEntity<ApiResponse<List<ActiveFeatureSubscriptionResponse>>> getMyListingSubscriptions(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getMyFeatureSubscriptionsByType(currentUser.getUserId(), FeatureType.LISTING)));
    }

    @GetMapping("/subscriptions/me/3d-tours")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the current user's active 3D tour subscriptions")
    public ResponseEntity<ApiResponse<List<ActiveFeatureSubscriptionResponse>>> getMy3dTourSubscriptions(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getMyFeatureSubscriptionsByType(currentUser.getUserId(), FeatureType._3D_TOUR)));
    }

    @GetMapping("/subscriptions/me/ai")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the current user's active AI subscriptions")
    public ResponseEntity<ApiResponse<List<ActiveFeatureSubscriptionResponse>>> getMyAiSubscriptions(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getMyFeatureSubscriptionsByType(currentUser.getUserId(), FeatureType.AI_REQUEST)));
    }

    @GetMapping("/boosts/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the current user's active boost packages")
    public ResponseEntity<ApiResponse<List<ActiveBoostPackageResponse>>> getMyBoostPackages(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        var boosts = billingService.getMyBoostPackages(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(boosts));
    }

    @GetMapping("/subscriptions/me/quota/{featureType}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get total remaining quota for a feature type (-1 = unlimited)")
    public ResponseEntity<ApiResponse<Integer>> getMyQuota(
            @PathVariable String featureType,
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        FeatureType type = FeatureType.fromDbValue(featureType.toUpperCase());
        int quota = billingService.getTotalQuotaForFeature(currentUser.getUserId(), type);
        return ResponseEntity.ok(ApiResponse.success(quota));
    }

    // -------------------------------------------------------------------------
    // PayOS webhook (public — called by PayOS server)
    // -------------------------------------------------------------------------

    @PostMapping("/webhook/payos")
    @Operation(summary = "PayOS payment webhook", description = "Receives payment confirmation from PayOS")
    public ResponseEntity<Void> payOsWebhook(@RequestBody Map<String, Object> payload) {
        log.info("PayOS webhook received");
        billingService.handlePayOsWebhook(payload);
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // VNPay IPN (server-to-server notification from VNPay)
    // -------------------------------------------------------------------------

    @GetMapping("/webhook/vnpay")
    @Operation(summary = "VNPay IPN webhook", description = "Server-to-server payment notification from VNPay")
    public ResponseEntity<Map<String, String>> vnPayIpn(@RequestParam Map<String, String> params) {
        log.info("VNPay IPN received, responseCode={}", params.get("vnp_ResponseCode"));
        Map<String, String> result = billingService.handleVnPayIpn(params);
        return ResponseEntity.ok(result);
    }

    // -------------------------------------------------------------------------
    // VNPay return URL (public — browser redirect from VNPay)
    // -------------------------------------------------------------------------

    @GetMapping("/payment/vnpay-return")
    @Operation(summary = "VNPay return redirect handler")
    public ResponseEntity<Void> vnPayReturn(@RequestParam Map<String, String> params) {
        log.info("VNPay return received, responseCode={}", params.get("vnp_ResponseCode"));
        String redirectUrl = billingService.handleVnPayReturn(params);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    // -------------------------------------------------------------------------
    // My transactions (authenticated)
    // -------------------------------------------------------------------------

    @GetMapping("/transactions/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the current user's completed transactions (payment history)")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal SecurityUserDetails currentUser
    ) {
        List<TransactionResponse> transactions = billingService.getMyTransactions(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    // -------------------------------------------------------------------------

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
