package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.transaction.Transaction;
import com.sep.realvista.domain.billing.transaction.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByOrderCode(Long orderCode);
    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
            + "WHERE t.paymentStatus = 'COMPLETED'")
    double sumTotalAmount();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
            + "WHERE t.paymentStatus = :status")
    double sumTotalAmountByPaymentStatus(@Param("status") PaymentStatus status);

    @Query("SELECT t FROM Transaction t "
            + "WHERE t.paymentStatus = 'COMPLETED' AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findAllByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Transaction t "
            + "WHERE t.paymentStatus = :status AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findAllByCreatedAtBetweenAndPaymentStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") PaymentStatus status);
}
