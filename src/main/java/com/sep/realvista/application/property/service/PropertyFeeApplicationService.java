package com.sep.realvista.application.property.service;

import com.sep.realvista.application.property.dto.CreatePropertyFeeRequest;
import com.sep.realvista.application.property.dto.PropertyFeeResponse;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import com.sep.realvista.domain.agent.PropertyAgentRepository;
import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import com.sep.realvista.domain.property.fee.PropertyFeeService;
import com.sep.realvista.domain.property.fee.PropertyFeeServiceRepository;
import com.sep.realvista.infrastructure.security.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PropertyFeeApplicationService {

    private final PropertyFeeServiceRepository feeRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyAgentRepository propertyAgentRepository;

    @Transactional(readOnly = true)
    public List<PropertyFeeResponse> getFees(UUID propertyId) {
        ensurePropertyExists(propertyId);
        return feeRepository.findByPropertyId(propertyId).stream()
                .map(PropertyFeeResponse::from)
                .collect(Collectors.toList());
    }

    public PropertyFeeResponse addFee(UUID propertyId, CreatePropertyFeeRequest request) {
        Property property = ensurePropertyExists(propertyId);
        ensureOwnerOrAdmin(property);

        PropertyFeeService fee = PropertyFeeService.builder()
                .propertyId(propertyId)
                .feeType(request.getFeeType())
                .feeName(request.getFeeName())
                .amount(request.getAmount())
                .billingCycle(request.getBillingCycle())
                .isOptional(Boolean.TRUE.equals(request.getIsOptional()))
                .description(request.getDescription())
                .build();

        PropertyFeeService saved = feeRepository.save(fee);
        log.info("Added fee {} to property {}", saved.getPropertyFeeServiceId(), propertyId);
        return PropertyFeeResponse.from(saved);
    }

    public void deleteFee(UUID propertyId, UUID feeId) {
        Property property = ensurePropertyExists(propertyId);
        ensureOwnerOrAdmin(property);

        feeRepository.findById(feeId).ifPresentOrElse(fee -> {
            if (!fee.getPropertyId().equals(propertyId)) {
                throw new ResourceNotFoundException("Fee", feeId.toString());
            }
            feeRepository.softDelete(feeId);
            log.info("Soft-deleted fee {} from property {}", feeId, propertyId);
        }, () -> {
            throw new ResourceNotFoundException("Fee", feeId.toString());
        });
    }

    public List<PropertyFeeResponse> syncFees(UUID propertyId, List<CreatePropertyFeeRequest> fees) {
        Property property = ensurePropertyExists(propertyId);
        ensureOwnerOrAdmin(property);

        feeRepository.softDeleteAllByPropertyId(propertyId);

        List<PropertyFeeResponse> result = fees.stream().map(req -> {
            PropertyFeeService fee = PropertyFeeService.builder()
                    .propertyId(propertyId)
                    .feeType(req.getFeeType())
                    .feeName(req.getFeeName())
                    .amount(req.getAmount())
                    .billingCycle(req.getBillingCycle())
                    .isOptional(Boolean.TRUE.equals(req.getIsOptional()))
                    .description(req.getDescription())
                    .build();
            return PropertyFeeResponse.from(feeRepository.save(fee));
        }).collect(Collectors.toList());

        log.info("Synced {} fees for property {}", result.size(), propertyId);
        return result;
    }

    private Property ensurePropertyExists(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId.toString()));
    }

    private void ensureOwnerOrAdmin(Property property) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return;
        }

        if (auth.getPrincipal() instanceof SecurityUserDetails userDetails) {
            UUID userId = userDetails.getUserId();
            if (property.getOwnerId().equals(userId)) {
                return;
            }
            if (propertyAgentRepository.existsByPropertyIdAndAgentId(property.getPropertyId(), userId)) {
                return;
            }
            throw new AccessDeniedException("Only the property owner or assigned agent can manage fees");
        }

        throw new AccessDeniedException("Authentication required");
    }
}
