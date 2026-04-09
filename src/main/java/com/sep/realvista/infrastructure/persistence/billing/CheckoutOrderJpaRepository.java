package com.sep.realvista.infrastructure.persistence.billing;

import com.sep.realvista.domain.billing.checkout.CheckoutOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CheckoutOrderJpaRepository extends JpaRepository<CheckoutOrder, UUID> {
    Optional<CheckoutOrder> findByOrderCode(Long orderCode);
}
