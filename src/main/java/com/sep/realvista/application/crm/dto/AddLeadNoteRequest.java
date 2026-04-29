package com.sep.realvista.application.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddLeadNoteRequest {

    @NotBlank
    private String content;
}
