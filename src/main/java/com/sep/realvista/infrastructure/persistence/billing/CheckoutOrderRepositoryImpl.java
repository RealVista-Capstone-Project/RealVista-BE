package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.checkout.CheckoutOrder;
import com.sep.realvista.domain.billing.checkout.CheckoutOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CheckoutOrderRepositoryImpl implements CheckoutOrderRepository {

    private final CheckoutOrderJpaRepository jpa;

    @Override
    public CheckoutOrder save(CheckoutOrder checkoutOrder) {
        return jpa.save(checkoutOrder);
    }

    @Override
    public Optional<CheckoutOrder> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<CheckoutOrder> findByOrderCode(Long orderCode) {
        return jpa.findByOrderCode(orderCode);
    }
}
