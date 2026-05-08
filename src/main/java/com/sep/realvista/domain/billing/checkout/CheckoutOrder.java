package com.sep.realvista.domain.billing.checkout;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.billing.transaction.PaymentMethod;
import com.sep.realvista.domain.billing.transaction.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "checkout_orders", indexes = {
        @Index(name = "idx_checkout_user", columnList = "user_id"),
        @Index(name = "idx_checkout_order_code", columnList = "order_code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CheckoutOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "checkout_order_id")
    private UUID checkoutOrderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    /** The plan/package code that will be purchased */
    @Column(name = "plan_code", nullable = false, length = 50)
    private String planCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /** Gateway order code (PayOS integer or VNPay txn ref equivalent). Unique per checkout. */
    @Column(name = "order_code", unique = true, nullable = false)
    private Long orderCode;

    /**
     * VNPay {@code vnp_CreateDate} from the pay URL (yyyyMMddHHmmss, Asia/Ho_Chi_Minh).
     * Required for QueryDR {@code vnp_TransactionDate}. Null for PayOS checkouts.
     */
    @Setter
    @Column(name = "vnp_create_date", length = 14)
    private String vnpCreateDate;
}
