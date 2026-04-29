package com.sep.realvista.application.billing.service;

import com.sep.realvista.application.billing.dto.ActiveBoostPackageResponse;
import com.sep.realvista.application.billing.dto.ActiveFeatureSubscriptionResponse;
import com.sep.realvista.application.billing.dto.BoostPackageResponse;
import com.sep.realvista.application.billing.dto.CheckoutRequest;
import com.sep.realvista.application.billing.dto.CheckoutResponse;
import com.sep.realvista.application.billing.dto.FeaturePackageResponse;
import com.sep.realvista.application.billing.dto.TransactionResponse;
import com.sep.realvista.application.billing.dto.TransactionStatusResponse;
import com.sep.realvista.domain.billing.boost.BoostPackage;
import com.sep.realvista.domain.billing.boost.UserListingBoostPackage;
import com.sep.realvista.domain.billing.boost.UserListingBoostPackageStatus;
import com.sep.realvista.domain.billing.boost.repository.BoostPackageRepository;
import com.sep.realvista.domain.billing.boost.repository.UserListingBoostPackageRepository;
import com.sep.realvista.domain.billing.checkout.CheckoutOrder;
import com.sep.realvista.domain.billing.checkout.CheckoutOrderRepository;
import com.sep.realvista.domain.billing.subscription.FeaturePackage;
import com.sep.realvista.domain.billing.subscription.FeaturePackageTierHelper;
import com.sep.realvista.domain.billing.subscription.FeatureType;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscription;
import com.sep.realvista.domain.billing.subscription.UserFeatureSubscriptionStatus;
import com.sep.realvista.domain.billing.subscription.repository.FeaturePackageRepository;
import com.sep.realvista.domain.billing.subscription.repository.UserFeatureSubscriptionRepository;
import com.sep.realvista.domain.billing.transaction.PaymentMethod;
import com.sep.realvista.domain.billing.transaction.PaymentStatus;
import com.sep.realvista.domain.billing.transaction.Transaction;
import com.sep.realvista.domain.billing.transaction.TransactionRepository;
import com.sep.realvista.domain.billing.transaction.TransactionType;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.DomainException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.infrastructure.payment.payos.PayOsPaymentRequestInfo;
import com.sep.realvista.infrastructure.payment.payos.PayOsPaymentResult;
import com.sep.realvista.infrastructure.payment.payos.PayOsService;
import com.sep.realvista.infrastructure.payment.vnpay.VnPayProperties;
import com.sep.realvista.infrastructure.payment.vnpay.VnPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillingApplicationService {

    private final FeaturePackageRepository featurePackageRepository;
    private final UserFeatureSubscriptionRepository userFeatureSubscriptionRepository;
    private final BoostPackageRepository boostPackageRepository;
    private final UserListingBoostPackageRepository userListingBoostPackageRepository;
    private final CheckoutOrderRepository checkoutOrderRepository;
    private final TransactionRepository transactionRepository;
    private final PayOsService payOsService;
    private final VnPayService vnPayService;
    private final VnPayProperties vnPayProperties;
    private final TransactionTemplate transactionTemplate;

    @Value("${spring.application.frontend.url}")
    private String frontendUrl;

    // -------------------------------------------------------------------------
    // Feature packages catalog
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<FeaturePackageResponse> getFeaturePackages() {
        return featurePackageRepository.findAllActive().stream()
                .map(this::toFeaturePackageResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeaturePackageResponse> getFeaturePackagesByType(FeatureType featureType) {
        return featurePackageRepository.findAllActiveByFeatureType(featureType).stream()
                .map(this::toFeaturePackageResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeaturePackageResponse> getListingPackages() {
        return getFeaturePackagesByType(FeatureType.LISTING);
    }

    @Transactional(readOnly = true)
    public List<FeaturePackageResponse> get3dTourPackages() {
        return getFeaturePackagesByType(FeatureType._3D_TOUR);
    }

    @Transactional(readOnly = true)
    public List<FeaturePackageResponse> getAiPackages() {
        return getFeaturePackagesByType(FeatureType.AI_REQUEST);
    }

    private FeaturePackageResponse toFeaturePackageResponse(FeaturePackage pkg) {
        return FeaturePackageResponse.builder()
                .id(pkg.getFeaturePackageId())
                .code(pkg.getCode())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .featureType(pkg.getFeatureType().toDbValue())
                .quota(pkg.getQuota())
                .durationDays(pkg.getDurationDays())
                .price(pkg.getPrice())
                .unlimited(pkg.isUnlimited())
                .free(pkg.isFree())
                .build();
    }

    // -------------------------------------------------------------------------
    // Boost packages catalog
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BoostPackageResponse> getBoostPackages() {
        return boostPackageRepository.findAllActive().stream()
                .map(p -> BoostPackageResponse.builder()
                        .id(p.getBoostPackageId())
                        .code(p.getCode())
                        .name(p.getName())
                        .description(p.getDescription())
                        .featuredQuota(p.getFeaturedQuota())
                        .hotBadgeQuota(p.getHotBadgeQuota())
                        .durationDays(p.getDurationDays())
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Checkout
    // -------------------------------------------------------------------------

    public CheckoutResponse checkout(UUID userId, CheckoutRequest request, String clientIp) {
        long orderCode = generateOrderCode();

        if (request.getPlanType() == CheckoutRequest.PlanType.SUBSCRIPTION) {
            return checkoutFeaturePackage(userId, request, orderCode, clientIp);
        } else {
            return checkoutBoost(userId, request, orderCode, clientIp);
        }
    }

    private CheckoutResponse checkoutFeaturePackage(
            UUID userId, CheckoutRequest request, long orderCode, String clientIp) {
        FeaturePackage pkg = featurePackageRepository.findByCode(request.getPlanCode())
                .filter(FeaturePackage::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feature package not found: " + request.getPlanCode()));

        assertNoDowngrade(userId, pkg);

        PaymentMethod method = toPaymentMethod(request.getPaymentMethod());

        CheckoutOrder order = CheckoutOrder.builder()
                .userId(userId)
                .transactionType(TransactionType.SUBSCRIPTION)
                .planCode(pkg.getCode())
                .orderCode(orderCode)
                .amount(pkg.getPrice())
                .paymentMethod(method)
                .build();
        order = checkoutOrderRepository.save(order);

        return buildCheckoutResponse(order, pkg.getName(), pkg.getPrice(), "SUBSCRIPTION", clientIp);
    }

    private CheckoutResponse checkoutBoost(UUID userId, CheckoutRequest request, long orderCode, String clientIp) {
        BoostPackage pkg = boostPackageRepository.findByCode(request.getPlanCode())
                .filter(BoostPackage::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Boost package not found: " + request.getPlanCode()));

        PaymentMethod method = toPaymentMethod(request.getPaymentMethod());

        CheckoutOrder order = CheckoutOrder.builder()
                .userId(userId)
                .transactionType(TransactionType.BOOST)
                .planCode(pkg.getCode())
                .orderCode(orderCode)
                .amount(pkg.getPrice())
                .paymentMethod(method)
                .build();
        order = checkoutOrderRepository.save(order);

        return buildCheckoutResponse(order, pkg.getName(), pkg.getPrice(), "BOOST", clientIp);
    }

    private CheckoutResponse buildCheckoutResponse(
            CheckoutOrder order, String planName, BigDecimal price, String planType, String clientIp
    ) {
        long amountLong = price.longValue();
        String payOsDescription = "RealVista " + planName;
        String vnPayOrderInfo = "Thanh toan don hang:" + order.getCheckoutOrderId();
        String vnPayReturnUrl = vnPayProperties.getReturnUrl().trim();
        String payOsReturnUrl = frontendUrl.replaceAll("/$", "") + "/vi/subscribe";
        String cancelUrl = frontendUrl.replaceAll("/$", "") + "/vi/subscribe?payment=cancelled";

        if (order.getPaymentMethod() == PaymentMethod.PAYOS) {
            // PayOS order stays open 30 min so in-progress bank transfers can still complete
            long payosExpiredAt = Instant.now().plusSeconds(1800).getEpochSecond();
            // UI countdown is shorter (10 min) to prompt user to act before the link goes stale
            long uiExpiredAt = Instant.now().plusSeconds(600).getEpochSecond();
            PayOsPaymentResult result = payOsService.createPaymentLink(
                    order.getOrderCode(), (int) (amountLong / 10),
                    payOsDescription, payOsReturnUrl, cancelUrl, payosExpiredAt);

            return CheckoutResponse.builder()
                    .checkoutOrderId(order.getCheckoutOrderId().toString())
                    .orderCode(order.getOrderCode())
                    .checkoutUrl(result.getCheckoutUrl())
                    .qrCode(result.getQrCode())
                    .paymentMethod("PAYOS")
                    .planName(planName)
                    .amount(amountLong)
                    .expiredAt(uiExpiredAt)
                    .build();

        } else {
            String checkoutUrl = vnPayService.buildPaymentUrl(
                    order.getCheckoutOrderId().toString(), amountLong / 10, vnPayOrderInfo, vnPayReturnUrl, clientIp);

            return CheckoutResponse.builder()
                    .checkoutOrderId(order.getCheckoutOrderId().toString())
                    .orderCode(order.getOrderCode())
                    .checkoutUrl(checkoutUrl)
                    .paymentMethod("VNPAY")
                    .planName(planName)
                    .amount(amountLong)
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // PayOS webhook
    // -------------------------------------------------------------------------

    public void handlePayOsWebhook(Map<String, Object> payload) {
        String receivedSignature = (String) payload.get("signature");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");

        if (data == null) {
            log.info("PayOS webhook: no data (test/confirmation webhook)");
            return;
        }

        // PayOS sends a confirmation webhook with orderCode=0 when verifying the URL
        Object orderCodeObj = data.get("orderCode");
        if (orderCodeObj == null || "0".equals(orderCodeObj.toString())) {
            log.info("PayOS webhook: confirmation/test webhook received (orderCode={})", orderCodeObj);
            return;
        }

        if (!payOsService.verifyWebhookSignature(data, receivedSignature)) {
            log.warn("PayOS webhook signature mismatch");
            return;
        }

        String code = (String) payload.get("code");
        long orderCode = Long.parseLong(orderCodeObj.toString());

        checkoutOrderRepository.findByOrderCode(orderCode).ifPresentOrElse(order -> {
            if ("00".equals(code)) {
                completeCheckoutOrder(order);
            } else {
                log.info("PayOS payment failed for orderCode={}", orderCode);
            }
        }, () -> log.warn("PayOS webhook: no checkout order found for orderCode={}", orderCode));
    }

    // -------------------------------------------------------------------------
    // VNPay IPN (server-to-server)
    // -------------------------------------------------------------------------

    @Transactional
    public Map<String, String> handleVnPayIpn(Map<String, String> params) {
        if (!vnPayService.verifyReturnSignature(params)) {
            log.warn("VNPay IPN signature mismatch");
            return Map.of("RspCode", "97", "Message", "Invalid Checksum");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        try {
            UUID checkoutOrderId = UUID.fromString(txnRef);
            return checkoutOrderRepository.findById(checkoutOrderId).map(order -> {
                if (transactionRepository.findByOrderCode(order.getOrderCode()).isPresent()) {
                    log.info("VNPay IPN: order already confirmed ref={}", txnRef);
                    return Map.of("RspCode", "02", "Message", "Order already confirmed");
                }
                if (vnPayService.isSuccess(responseCode)) {
                    completeCheckoutOrder(order);
                    log.info("VNPay IPN: payment success ref={}", txnRef);
                    return Map.of("RspCode", "00", "Message", "Confirm Success");
                } else {
                    log.info("VNPay IPN: payment failed ref={}, code={}", txnRef, responseCode);
                    return Map.of("RspCode", "00", "Message", "Confirm Success");
                }
            }).orElseGet(() -> {
                log.warn("VNPay IPN: order not found ref={}", txnRef);
                return Map.of("RspCode", "01", "Message", "Order not found");
            });
        } catch (IllegalArgumentException e) {
            log.warn("VNPay IPN: invalid txnRef={}", txnRef);
            return Map.of("RspCode", "99", "Message", "Unknown error");
        }
    }

    // -------------------------------------------------------------------------
    // VNPay return
    // -------------------------------------------------------------------------

    public String handleVnPayReturn(Map<String, String> params) {
        if (!vnPayService.verifyReturnSignature(params)) {
            log.warn("VNPay return signature mismatch");
            return frontendUrl + "/vi/subscribe?payment=invalid_signature";
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        try {
            UUID checkoutOrderId = UUID.fromString(txnRef);
            return checkoutOrderRepository.findById(checkoutOrderId).map(order -> {
                if (vnPayService.isSuccess(responseCode)) {
                    if (transactionRepository.findByOrderCode(order.getOrderCode()).isEmpty()) {
                        completeCheckoutOrder(order);
                    }
                    return frontendUrl + "/vi/subscribe?payment=success";
                } else {
                    return frontendUrl + "/vi/subscribe?payment=failed";
                }
            }).orElseGet(() -> {
                log.warn("VNPay return: transaction not found for ref={}", txnRef);
                return frontendUrl + "/vi/subscribe?payment=not_found";
            });
        } catch (IllegalArgumentException e) {
            return frontendUrl + "/vi/subscribe?payment=failed";
        }
    }

    // -------------------------------------------------------------------------
    // Checkout order status polling
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public TransactionStatusResponse getCheckoutOrderStatus(UUID checkoutOrderId, UUID requestingUserId) {
        CheckoutOrder order = checkoutOrderRepository.findById(checkoutOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout order not found"));

        if (!order.getUserId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Checkout order not found");
        }

        // Check if a transaction was created from this order
        Transaction txn = transactionRepository.findByOrderCode(order.getOrderCode())
                .orElse(null);

        if (txn != null) {
            return toTransactionStatusResponse(txn);
        }

        // Order exists but no transaction yet - still pending
        return TransactionStatusResponse.builder()
                .transactionId(checkoutOrderId.toString())
                .status("PENDING")
                .planCode(order.getPlanCode())
                .planType(order.getTransactionType().name())
                .build();
    }

    /**
     * Polls PayOS for payment link status and creates + completes the transaction when PayOS reports settlement.
     * Use when webhooks cannot reach the server (e.g. localhost without a tunnel).
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public TransactionStatusResponse syncPayOsFromCheckout(UUID checkoutOrderId, UUID requestingUserId) {
        CheckoutOrder order = checkoutOrderRepository.findById(checkoutOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout order not found"));

        if (!order.getUserId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Checkout order not found");
        }
        if (order.getPaymentMethod() != PaymentMethod.PAYOS) {
            throw new DomainException(
                    "Only PayOS checkout orders can be synced from PayOS",
                    "ERROR_BILLING_PAYOS_ORDER_ONLY");
        }

        PayOsPaymentRequestInfo info = payOsService.fetchPaymentRequestByOrderCode(order.getOrderCode());
        long expectedVnd = order.getAmount().longValue();

        if (payOsReportsPaid(info, expectedVnd)) {
            transactionTemplate.executeWithoutResult(status -> {
                // Check if transaction already exists
                Transaction existing = transactionRepository.findByOrderCode(order.getOrderCode()).orElse(null);
                if (existing == null) {
                    completeCheckoutOrder(order);
                }
            });
        }

        // Return updated status
        Transaction txn = transactionRepository.findByOrderCode(order.getOrderCode())
                .orElse(null);

        if (txn != null) {
            return toTransactionStatusResponse(txn);
        }

        return TransactionStatusResponse.builder()
                .transactionId(checkoutOrderId.toString())
                .status("PENDING")
                .planCode(order.getPlanCode())
                .planType(order.getTransactionType().name())
                .build();
    }

    // -------------------------------------------------------------------------
    // Transaction status polling (legacy - kept for compatibility)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public TransactionStatusResponse getTransactionStatus(UUID transactionId, UUID requestingUserId) {
        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!txn.getUserId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        return toTransactionStatusResponse(txn);
    }

    /**
     * Polls PayOS for payment link status and completes the transaction locally when PayOS reports settlement.
     * Use when webhooks cannot reach the server (e.g. localhost without a tunnel).
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public TransactionStatusResponse syncPayOsFromGateway(UUID transactionId, UUID requestingUserId) {
        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!txn.getUserId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        if (txn.getPaymentMethod() != PaymentMethod.PAYOS) {
            throw new DomainException(
                    "Only PayOS transactions can be synced from PayOS",
                    "ERROR_BILLING_PAYOS_TRANSACTION_ONLY");
        }
        if (txn.getPaymentStatus() != PaymentStatus.PENDING) {
            return toTransactionStatusResponse(txn);
        }

        PayOsPaymentRequestInfo info = payOsService.fetchPaymentRequestByOrderCode(txn.getOrderCode());
        long expectedVnd = txn.getAmount().longValue();

        if (payOsReportsPaid(info, expectedVnd)) {
            transactionTemplate.executeWithoutResult(status -> {
                Transaction fresh = transactionRepository.findById(transactionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
                if (fresh.getPaymentStatus() != PaymentStatus.PENDING) {
                    return;
                }
                activateAfterPayment(fresh);
            });
        }

        Transaction latest = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!latest.getUserId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        return toTransactionStatusResponse(latest);
    }

    private static boolean payOsReportsPaid(PayOsPaymentRequestInfo info, long expectedAmountVnd) {
        String st = info.getStatus();
        if (st != null && st.equalsIgnoreCase("CANCELLED")) {
            return false;
        }
        if (info.getAmountRemaining() == 0 && info.getAmount() > 0) {
            return true;
        }
        if (expectedAmountVnd > 0 && info.getAmountPaid() * 10 >= expectedAmountVnd) {
            return true;
        }
        return st != null && (st.equalsIgnoreCase("PAID") || st.equalsIgnoreCase("SUCCESS"));
    }

    private static TransactionStatusResponse toTransactionStatusResponse(Transaction txn) {
        return TransactionStatusResponse.builder()
                .transactionId(txn.getTransactionId().toString())
                .status(txn.getPaymentStatus().name())
                .planCode(txn.getPlanCode())
                .planType(txn.getTransactionType().name())
                .build();
    }

    // -------------------------------------------------------------------------
    // My active feature subscriptions
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ActiveFeatureSubscriptionResponse> getMyFeatureSubscriptions(UUID userId) {
        List<ActiveFeatureSubscriptionResponse> responses = userFeatureSubscriptionRepository
                .findAllActiveByUserId(userId).stream()
                .filter(UserFeatureSubscription::isUsable)
                .map(this::toActiveFeatureSubscriptionResponse)
                .collect(Collectors.toList());
        enrichQuotaWithFreeBaseline(responses);
        return responses;
    }

    @Transactional(readOnly = true)
    public List<ActiveFeatureSubscriptionResponse> getMyFeatureSubscriptionsByType(
            UUID userId, FeatureType featureType) {
        List<ActiveFeatureSubscriptionResponse> responses = userFeatureSubscriptionRepository
                .findActiveByUserIdAndFeatureType(userId, featureType).stream()
                .filter(UserFeatureSubscription::isUsable)
                .map(this::toActiveFeatureSubscriptionResponse)
                .collect(Collectors.toList());
        enrichQuotaWithFreeBaseline(responses);
        return responses;
    }

    private ActiveFeatureSubscriptionResponse toActiveFeatureSubscriptionResponse(UserFeatureSubscription sub) {
        FeaturePackage pkg = sub.getFeaturePackage();
        // Use originalQuota (snapshotted at checkout) so admin updates to the package
        // never retroactively change what the user sees as their quota limit.
        // -1 means unlimited. For legacy rows with null originalQuota, fall back to
        // remainingQuota, never the live package quota.
        Integer originalQuota = sub.getOriginalQuota();
        boolean unlimited = originalQuota != null
                ? originalQuota == -1
                : sub.getRemainingQuota() == null;
        Integer quotaLimit = unlimited ? null : originalQuota;
        if (quotaLimit == null && !unlimited) {
            quotaLimit = sub.getRemainingQuota();
        }
        return ActiveFeatureSubscriptionResponse.builder()
                .subscriptionId(sub.getUserFeatureSubscriptionId())
                .packageCode(pkg != null ? pkg.getCode() : "")
                .packageName(pkg != null ? pkg.getName() : sub.getFeaturePackageId().toString())
                .featureType(pkg != null ? pkg.getFeatureType().toDbValue() : "")
                .quotaLimit(quotaLimit)
                .remainingQuota(sub.getRemainingQuota())
                .unlimited(unlimited)
                .tierLevel(FeaturePackageTierHelper.tierLevel(pkg))
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .status(sub.getStatus().name())
                .build();
    }

    private void enrichQuotaWithFreeBaseline(List<ActiveFeatureSubscriptionResponse> responses) {
        Map<String, List<ActiveFeatureSubscriptionResponse>> byType = responses.stream()
                .collect(Collectors.groupingBy(ActiveFeatureSubscriptionResponse::getFeatureType));

        for (List<ActiveFeatureSubscriptionResponse> group : byType.values()) {
            ActiveFeatureSubscriptionResponse freeSub = null;
            ActiveFeatureSubscriptionResponse paidSub = null;

            for (ActiveFeatureSubscriptionResponse r : group) {
                if (r.getTierLevel() == 0) {
                    freeSub = r;
                } else if (paidSub == null || r.getTierLevel() > paidSub.getTierLevel()) {
                    paidSub = r;
                }
            }

            if (freeSub != null && paidSub != null) {
                if (!paidSub.isUnlimited()
                        && freeSub.getRemainingQuota() != null && paidSub.getRemainingQuota() != null) {
                    paidSub.setRemainingQuota(paidSub.getRemainingQuota() + freeSub.getRemainingQuota());
                }
                responses.remove(freeSub);
            }
        }
    }

    /**
     * Cancels an active feature subscription owned by the user (stops renewal benefits immediately).
     */
    public void cancelFeatureSubscription(UUID userId, UUID subscriptionId) {
        UserFeatureSubscription sub = userFeatureSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        if (!sub.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Subscription not found");
        }
        if (sub.getStatus() != UserFeatureSubscriptionStatus.ACTIVE) {
            throw new BusinessConflictException(
                    "Gói không còn ở trạng thái hoạt động.",
                    "ERROR_SUBSCRIPTION_NOT_ACTIVE");
        }
        sub.cancel();
        userFeatureSubscriptionRepository.save(sub);
    }

    /**
     * Get user's active boost packages (purchased boost quotas).
     */
    public List<ActiveBoostPackageResponse> getMyBoostPackages(UUID userId) {
        List<UserListingBoostPackage> boosts = userListingBoostPackageRepository.findAllActiveByUserId(userId);
        return boosts.stream()
                .map(this::toActiveBoostPackageResponse)
                .collect(Collectors.toList());
    }

    private ActiveBoostPackageResponse toActiveBoostPackageResponse(UserListingBoostPackage boost) {
        BoostPackage pkg = boost.getBoostPackage();
        // Use originalFeaturedQuota/originalHotBadgeQuota (snapshotted at checkout)
        // so admin updates to the package never retroactively affect existing boosts.
        Integer originalFeatured = boost.getOriginalFeaturedQuota();
        Integer originalHotBadge = boost.getOriginalHotBadgeQuota();
        if (originalFeatured == null) {
            originalFeatured = pkg != null ? pkg.getFeaturedQuota() : null;
        }
        if (originalHotBadge == null) {
            originalHotBadge = pkg != null ? pkg.getHotBadgeQuota() : null;
        }
        return ActiveBoostPackageResponse.builder()
                .boostPackageId(pkg != null ? pkg.getBoostPackageId() : null)
                .code(pkg != null ? pkg.getCode() : "")
                .name(pkg != null ? pkg.getName() : "")
                .description(pkg != null ? pkg.getDescription() : "")
                .featuredQuota(originalFeatured)
                .hotBadgeQuota(originalHotBadge)
                .durationDays(pkg != null ? pkg.getDurationDays() : null)
                .startDate(boost.getStartDate())
                .endDate(boost.getEndDate())
                .remainingFeaturedQuota(boost.getRemainingFeaturedQuota())
                .remainingHotBadgeQuota(boost.getRemainingHotBadgeQuota())
                .status(boost.getStatus().name())
                .build();
    }

    // -------------------------------------------------------------------------
    // Get user's total quota for a feature type
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public int getTotalQuotaForFeature(UUID userId, FeatureType featureType) {
        List<UserFeatureSubscription> subs = userFeatureSubscriptionRepository
                .findActiveByUserIdAndFeatureType(userId, featureType);

        int totalQuota = 0;
        for (UserFeatureSubscription sub : subs) {
            if (!sub.isUsable()) {
                continue;
            }
            FeaturePackage pkg = sub.getFeaturePackage();
            if (pkg != null && pkg.isUnlimited()) {
                return -1; // Unlimited
            }
            
            if (sub.getRemainingQuota() != null) {
                totalQuota += sub.getRemainingQuota();
            }
        }
        return totalQuota;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void assertNoDowngrade(UUID userId, FeaturePackage pkg) {
        if (pkg.isFree()) {
            return;
        }
        List<UserFeatureSubscription> existing = userFeatureSubscriptionRepository
                .findActiveByUserIdAndFeatureType(userId, pkg.getFeatureType());
        int maxTier = existing.stream()
                .filter(UserFeatureSubscription::isUsable)
                .mapToInt(s -> FeaturePackageTierHelper.tierLevel(s.getFeaturePackage()))
                .max()
                .orElse(0);
        int newTier = FeaturePackageTierHelper.tierLevel(pkg);
        if (newTier < maxTier) {
            throw new BusinessConflictException(
                    "Bạn đang dùng gói cấp cao hơn. Không thể mua hoặc hạ xuống gói cấp thấp hơn.",
                    "ERROR_SUBSCRIPTION_DOWNGRADE_BLOCKED");
        }
    }

    private void supersedeActiveSubscriptionsForFeatureType(UUID userId, FeatureType featureType) {
        List<UserFeatureSubscription> existing = userFeatureSubscriptionRepository
                .findActiveByUserIdAndFeatureType(userId, featureType);
        for (UserFeatureSubscription sub : existing) {
            if (sub.getStatus() == UserFeatureSubscriptionStatus.ACTIVE
                    && !sub.getFeaturePackage().isFree()) {
                sub.cancel();
                userFeatureSubscriptionRepository.save(sub);
            }
        }
    }

    private void cancelActiveBoostPackages(UUID userId) {
        List<UserListingBoostPackage> activeBoosts = userListingBoostPackageRepository
                .findByUserIdAndStatus(userId, UserListingBoostPackageStatus.ACTIVE);

        for (UserListingBoostPackage boost : activeBoosts) {
            boost.cancel();
            userListingBoostPackageRepository.save(boost);
        }
    }

    private void activateAfterPayment(Transaction txn) {
        if (txn.getTransactionType() == TransactionType.SUBSCRIPTION) {
            FeaturePackage pkg = featurePackageRepository.findByCode(txn.getPlanCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Feature package not found: " + txn.getPlanCode()));

            supersedeActiveSubscriptionsForFeatureType(txn.getUserId(), pkg.getFeatureType());

            LocalDate endDate = pkg.hasNoExpiration() ? null : LocalDate.now().plusDays(pkg.getDurationDays());
            Integer remainingQuota = pkg.isUnlimited() ? null : pkg.getQuota();

            UserFeatureSubscription sub = UserFeatureSubscription.builder()
                    .userId(txn.getUserId())
                    .featurePackageId(pkg.getFeaturePackageId())
                    .startDate(LocalDate.now())
                    .endDate(endDate)
                    .remainingQuota(remainingQuota)
                    .originalQuota(pkg.getQuota())
                    .status(UserFeatureSubscriptionStatus.ACTIVE)
                    .build();

            UserFeatureSubscription saved = userFeatureSubscriptionRepository.save(sub);
            txn.complete(saved.getUserFeatureSubscriptionId());
        } else {
            // BOOST type - cancel existing active boost if user buys a different one
            cancelActiveBoostPackages(txn.getUserId());

            BoostPackage pkg = boostPackageRepository.findByCode(txn.getPlanCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Boost package not found: " + txn.getPlanCode()));

            LocalDate endDate = LocalDate.now().plusDays(pkg.getDurationDays());

            UserListingBoostPackage boost = UserListingBoostPackage.builder()
                    .userId(txn.getUserId())
                    .boostPackageId(pkg.getBoostPackageId())
                    .startDate(LocalDate.now())
                    .endDate(endDate)
                    .remainingFeaturedQuota(pkg.getFeaturedQuota())
                    .remainingHotBadgeQuota(pkg.getHotBadgeQuota())
                    .originalFeaturedQuota(pkg.getFeaturedQuota())
                    .originalHotBadgeQuota(pkg.getHotBadgeQuota())
                    .status(UserListingBoostPackageStatus.ACTIVE)
                    .build();

            UserListingBoostPackage saved = userListingBoostPackageRepository.save(boost);
            txn.complete(saved.getUserListingBoostPackageId());
        }
        transactionRepository.save(txn);
        log.info("Payment activated for txn={}, type={}, plan={}",
                txn.getTransactionId(), txn.getTransactionType(), txn.getPlanCode());
    }

    private PaymentMethod toPaymentMethod(CheckoutRequest.PaymentMethodRequest method) {
        return switch (method) {
            case PAYOS -> PaymentMethod.PAYOS;
            case VNPAY -> PaymentMethod.VNPAY;
        };
    }

    private long generateOrderCode() {
        return ThreadLocalRandom.current().nextLong(1_000_000L, 9_000_000_000L);
    }

    /**
     * Completes a checkout order by creating a Transaction and activating it.
     * Called when payment is confirmed via PayOS webhook or VNPay return.
     */
    private void completeCheckoutOrder(CheckoutOrder order) {
        // Create Transaction from CheckoutOrder
        Transaction txn = Transaction.builder()
                .userId(order.getUserId())
                .transactionType(order.getTransactionType())
                .planCode(order.getPlanCode())
                .orderCode(order.getOrderCode())
                .amount(order.getAmount())
                .paymentMethod(order.getPaymentMethod())
                .build();
        txn = transactionRepository.save(txn);

        // Activate subscription/boost (handles auto-cancel old & create new subscription)
        activateAfterPayment(txn);
    }

    // -------------------------------------------------------------------------
    // My transactions
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(UUID userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse toTransactionResponse(Transaction txn) {
        FeaturePackage pkg = null;
        String description = "";

        if (txn.getTransactionType() == TransactionType.SUBSCRIPTION) {
            pkg = featurePackageRepository.findByCode(txn.getPlanCode()).orElse(null);
            description = pkg != null ? pkg.getName() : txn.getPlanCode();
        } else {
            description = txn.getPlanCode();
        }

        return TransactionResponse.builder()
                .transactionId(txn.getTransactionId().toString())
                .planCode(txn.getPlanCode())
                .planType(txn.getTransactionType().name())
                .paymentMethod(txn.getPaymentMethod().name())
                .status(txn.getPaymentStatus().name())
                .amount(txn.getAmount())
                .createdAt(txn.getCreatedAt())
                .description(description)
                .build();
    }

    /**
     * Assigns the default AI_FREE package to a user if they don't have one.
     */
    public void assignDefaultAiPackage(UUID userId) {
        assignDefaultFreePackage(userId, "AI_FREE", FeatureType.AI_REQUEST);
    }

    /**
     * Assigns LISTING_FREE and 3D_TOUR_FREE packages to a user (called when user becomes OWNER/AGENT).
     */
    public void assignDefaultOwnerPackages(UUID userId) {
        assignDefaultFreePackage(userId, "LISTING_FREE", FeatureType.LISTING);
        assignDefaultFreePackage(userId, "3D_TOUR_FREE", FeatureType._3D_TOUR);
    }

    /**
     * Assigns all 3 free packages to a user (AI_FREE, LISTING_FREE, 3D_TOUR_FREE).
     */
    public void assignAllDefaultFreePackages(UUID userId) {
        assignDefaultAiPackage(userId);
        assignDefaultOwnerPackages(userId);
    }

    private void assignDefaultFreePackage(UUID userId, String packageCode, FeatureType featureType) {
        log.info("Assigning default {} package to user: {}", packageCode, userId);

        featurePackageRepository.findByCode(packageCode).ifPresent(pkg -> {
            List<UserFeatureSubscription> existing = userFeatureSubscriptionRepository
                    .findActiveByUserIdAndFeatureType(userId, featureType);

            if (existing.stream().noneMatch(s -> s.getFeaturePackage() != null
                    && s.getFeaturePackage().isFree())) {
                UserFeatureSubscription sub = UserFeatureSubscription.builder()
                        .userId(userId)
                        .featurePackageId(pkg.getFeaturePackageId())
                        .startDate(LocalDate.now())
                        .endDate(null)
                        .remainingQuota(pkg.getQuota())
                        .originalQuota(pkg.getQuota())
                        .status(UserFeatureSubscriptionStatus.ACTIVE)
                        .build();
                userFeatureSubscriptionRepository.save(sub);
                log.info("Assigned default {} package to user={}", packageCode, userId);
            }
        });
    }

}
