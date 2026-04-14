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
            log.warn("PayOS webhook received with no data");
            return;
        }

        if (!payOsService.verifyWebhookSignature(data, receivedSignature)) {
            log.warn("PayOS webhook signature mismatch");
            return;
        }

        String code = (String) payload.get("code");
        long orderCode = Long.parseLong(data.get("orderCode").toString());

        checkoutOrderRepository.findByOrderCode(orderCode).ifPresentOrElse(order -> {
            if ("00".equals(code)) {
                completeCheckoutOrder(order);
            } else {
                log.info("PayOS payment failed for orderCode={}", orderCode);
            }
        }, () -> log.warn("PayOS webhook: no checkout order found for orderCode={}", orderCode));
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
                    completeCheckoutOrder(order);
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
            throw new IllegalArgumentException("Only PayOS checkout orders can be synced from PayOS");
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
            throw new IllegalArgumentException("Only PayOS transactions can be synced from PayOS");
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
        return userFeatureSubscriptionRepository.findAllActiveByUserId(userId).stream()
                .filter(UserFeatureSubscription::isUsable)
                .map(this::toActiveFeatureSubscriptionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActiveFeatureSubscriptionResponse> getMyFeatureSubscriptionsByType(
            UUID userId, FeatureType featureType) {
        return userFeatureSubscriptionRepository.findActiveByUserIdAndFeatureType(userId, featureType).stream()
                .filter(UserFeatureSubscription::isUsable)
                .map(this::toActiveFeatureSubscriptionResponse)
                .collect(Collectors.toList());
    }

    private ActiveFeatureSubscriptionResponse toActiveFeatureSubscriptionResponse(UserFeatureSubscription sub) {
        FeaturePackage pkg = sub.getFeaturePackage();
        Integer quotaLimit = null;
        if (pkg != null && !pkg.isUnlimited()) {
            quotaLimit = pkg.getQuota();
        }
        return ActiveFeatureSubscriptionResponse.builder()
                .subscriptionId(sub.getUserFeatureSubscriptionId())
                .packageCode(pkg != null ? pkg.getCode() : "")
                .packageName(pkg != null ? pkg.getName() : sub.getFeaturePackageId().toString())
                .featureType(pkg != null ? pkg.getFeatureType().toDbValue() : "")
                .quotaLimit(quotaLimit)
                .remainingQuota(sub.getRemainingQuota())
                .unlimited(pkg != null && pkg.isUnlimited())
                .tierLevel(FeaturePackageTierHelper.tierLevel(pkg))
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .status(sub.getStatus().name())
                .build();
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
            throw new BusinessConflictException("Gói không còn ở trạng thái hoạt động.", "SUBSCRIPTION_NOT_ACTIVE");
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
        return ActiveBoostPackageResponse.builder()
                .boostPackageId(pkg.getBoostPackageId())
                .code(pkg.getCode())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .featuredQuota(pkg.getFeaturedQuota())
                .hotBadgeQuota(pkg.getHotBadgeQuota())
                .durationDays(pkg.getDurationDays())
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
                    "SUBSCRIPTION_DOWNGRADE_BLOCKED");
        }
    }

    private void supersedeActiveSubscriptionsForFeatureType(UUID userId, FeatureType featureType) {
        List<UserFeatureSubscription> existing = userFeatureSubscriptionRepository
                .findActiveByUserIdAndFeatureType(userId, featureType);
        for (UserFeatureSubscription sub : existing) {
            if (sub.getStatus() == UserFeatureSubscriptionStatus.ACTIVE) {
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

}
