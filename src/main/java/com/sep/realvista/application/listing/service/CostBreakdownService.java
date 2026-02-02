package com.sep.realvista.application.listing.service;

import com.sep.realvista.application.listing.dto.CostBreakdownDTO;
import com.sep.realvista.application.listing.dto.PropertyFeeDTO;
import com.sep.realvista.domain.listing.Listing;
import com.sep.realvista.domain.listing.ListingType;
import com.sep.realvista.domain.property.fee.PropertyFeeService;
import com.sep.realvista.domain.property.fee.PropertyFeeServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating cost breakdown for listing details.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostBreakdownService {

        private final PropertyFeeServiceRepository propertyFeeServiceRepository;

        private static final String UNIT = "đ/tháng";
        private static final String DISCLAIMER = "* Các chi phí ước tính, thực tế có thể thay đổi";

        /**
         * Calculate cost breakdown for a listing.
         * Only applicable for RENT listings.
         *
         * @param listing the listing
         * @return cost breakdown DTO, or null if not applicable
         */
        public CostBreakdownDTO calculateCostBreakdown(Listing listing) {
                // Only show cost breakdown for rent listings
                if (listing.getListingType() != ListingType.RENT) {
                        return null;
                }

                // Fetch property fees
                List<PropertyFeeService> fees = propertyFeeServiceRepository
                                .findByPropertyId(listing.getProperty().getPropertyId());

                // Separate required and optional fees
                List<PropertyFeeService> requiredFees = fees.stream()
                                .filter(fee -> !fee.getIsOptional())
                                .collect(Collectors.toList());

                List<PropertyFeeService> optionalFees = fees.stream()
                                .filter(PropertyFeeService::getIsOptional)
                                .collect(Collectors.toList());

                // Calculate totals
                long basePrice = listing.getPrice().longValue();
                long requiredFeesTotal = requiredFees.stream()
                                .mapToLong(fee -> fee.getAmount() != null ? fee.getAmount().longValue() : 0L)
                                .sum();
                long optionalFeesTotal = optionalFees.stream()
                                .mapToLong(fee -> fee.getAmount() != null ? fee.getAmount().longValue() : 0L)
                                .sum();
                long totalCost = basePrice + requiredFeesTotal;

                // Build DTO
                return CostBreakdownDTO.builder()
                                .basePrice(basePrice)
                                .basePriceUnit(UNIT)
                                .requiredFees(mapToDTO(requiredFees))
                                .requiredFeesSubtotal(requiredFeesTotal)
                                .optionalFees(mapToDTO(optionalFees))
                                .optionalFeesSubtotal(optionalFeesTotal)
                                .totalCost(totalCost)
                                .disclaimer(DISCLAIMER)
                                .build();
        }

        /**
         * Map list of PropertyFeeService entities to DTOs.
         *
         * @param fees list of property fees
         * @return list of property fee DTOs
         */
        private List<PropertyFeeDTO> mapToDTO(List<PropertyFeeService> fees) {
                return fees.stream()
                                .map(fee -> PropertyFeeDTO.builder()
                                                .name(fee.getFeeName())
                                                .amount(fee.getAmount() != null ? fee.getAmount().longValue() : 0L)
                                                .feeType(fee.getFeeType().name())
                                                .build())
                                .collect(Collectors.toList());
        }
}
