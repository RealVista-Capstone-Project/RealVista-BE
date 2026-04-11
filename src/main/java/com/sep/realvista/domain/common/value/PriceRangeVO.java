package com.sep.realvista.domain.common.value;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceRangeVO {

    @JsonProperty("rent")
    private RangeVO rent;

    @JsonProperty("buy")
    private RangeVO buy;
}
