package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByOrderCode(Long orderCode);
    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
            + "WHERE t.paymentStatus = 'COMPLETED'")
    double sumTotalAmount();

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Transaction t "
            + "WHERE t.paymentStatus = 'COMPLETED' AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findAllByCreatedAtBetween(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}




