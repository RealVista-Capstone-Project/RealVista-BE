package com.sep.realvista.application.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProperty3DOperationRequest {

    @NotBlank(message = "Room name must not be blank")
    @Size(max = 255, message = "Room name must not exceed 255 characters")
    private String roomName;
}
