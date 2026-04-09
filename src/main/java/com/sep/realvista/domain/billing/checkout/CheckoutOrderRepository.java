package com.sep.realvista.domain.billing.checkout;

import java.util.Optional;
import java.util.UUID;

public interface CheckoutOrderRepository {
    CheckoutOrder save(CheckoutOrder checkoutOrder);
    Optional<CheckoutOrder> findById(UUID id);
    Optional<CheckoutOrder> findByOrderCode(Long orderCode);
}
