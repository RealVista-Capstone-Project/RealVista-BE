package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.transaction.Transaction;
import com.sep.realvista.domain.billing.transaction.TransactionRepository;
import com.sep.realvista.domain.billing.transaction.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpa;

    @Override
    public Transaction save(Transaction transaction) {
        return jpa.save(transaction);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Transaction> findByOrderCode(Long orderCode) {
        return jpa.findByOrderCode(orderCode);
    }

    @Override
    public List<Transaction> findByUserId(UUID userId) {
        return jpa.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Transaction> findAll() {
        return jpa.findAll();
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public List<Transaction> findTop10ByOrderByCreatedAtDesc() {
        return jpa.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public double sumTotalAmount() {
        return jpa.sumTotalAmount();
    }

    @Override
    public List<Transaction> findAllByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return jpa.findAllByCreatedAtBetween(start, end);
    }

    @Override
    public double sumTotalAmountByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        List<Transaction> transactions = jpa.findAllByCreatedAtBetweenAndPaymentStatus(
                start, end, PaymentStatus.COMPLETED);
        return transactions.stream()
                .map(Transaction::getAmount)
                .mapToDouble(java.math.BigDecimal::doubleValue)
                .sum();
    }

    @Override
    public double sumTotalAmountByPaymentStatus(PaymentStatus status) {
        return jpa.sumTotalAmountByPaymentStatus(status);
    }

    @Override
    public org.springframework.data.domain.Page<Transaction> findAllByCreatedAtBetweenPaged(
            java.time.LocalDateTime start, java.time.LocalDateTime end, 
            org.springframework.data.domain.Pageable pageable) {
        return jpa.findAllByCreatedAtBetweenPaged(start, end, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Transaction> findAllByCreatedAtBetweenAndPlanCodeInPaged(
            java.time.LocalDateTime start, java.time.LocalDateTime end, 
            java.util.List<String> planCodes,
            org.springframework.data.domain.Pageable pageable) {
        return jpa.findAllByCreatedAtBetweenAndPlanCodeInPaged(start, end, planCodes, pageable);
    }
}
