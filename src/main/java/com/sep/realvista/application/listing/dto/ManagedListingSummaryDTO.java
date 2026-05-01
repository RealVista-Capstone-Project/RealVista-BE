package com.sep.realvista.application.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedListingSummaryDTO {
    private long all;
    private long rent;
    private long sale;
    private long currentMonthAll;
    private long currentMonthRent;
    private long currentMonthSale;
    private long previousAll;
    private long previousRent;
    private long previousSale;
}
