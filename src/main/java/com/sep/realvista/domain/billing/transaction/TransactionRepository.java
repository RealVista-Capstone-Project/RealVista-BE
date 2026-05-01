package com.sep.realvista.domain.billing.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    Optional<Transaction> findByOrderCode(Long orderCode);
    List<Transaction> findByUserId(UUID userId);
    List<Transaction> findAll();
    long count();
    List<Transaction> findTop10ByOrderByCreatedAtDesc();
    double sumTotalAmount();
    List<Transaction> findAllByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    double sumTotalAmountByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    double sumTotalAmountByPaymentStatus(PaymentStatus status);
}


