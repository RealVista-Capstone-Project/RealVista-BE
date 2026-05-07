package com.sep.realvista.application.property.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SyncPropertyFeesRequest {

    @NotNull
    @Valid
    private List<CreatePropertyFeeRequest> fees;
}
