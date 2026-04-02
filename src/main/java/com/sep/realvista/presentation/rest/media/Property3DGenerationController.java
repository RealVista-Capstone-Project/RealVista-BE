package com.sep.realvista.presentation.rest.media;

import com.sep.realvista.application.media.dto.CreateProperty3DOperationRequest;
import com.sep.realvista.application.media.dto.Property3DGenerationDto;
import com.sep.realvista.application.media.service.Property3DGenerationService;
import com.sep.realvista.domain.user.User;
import com.sep.realvista.presentation.common.util.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/3d-operations")
@RequiredArgsConstructor
@Tag(name = "Property 3D Operations", description = "Manage Marble AI 3D generations for properties")
public class Property3DGenerationController {

    private final Property3DGenerationService generationService;
    private final ControllerUtils controllerUtils;

    @PostMapping
    @Operation(summary = "Initiate tracking for a new 3D world generation operation")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Property3DGenerationDto> initiateOperation(
            @PathVariable UUID propertyId,
            @RequestBody CreateProperty3DOperationRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = controllerUtils.getCurrentUser(auth);

        Property3DGenerationDto result = generationService.initiateOperation(
                propertyId,
                request,
                currentUser.getUserId());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Get the list of 3D generation operations and automatically poll status for pending ones")
    public ResponseEntity<List<Property3DGenerationDto>> getOperations(
            @PathVariable UUID propertyId) {

        List<Property3DGenerationDto> results = generationService.getOperations(propertyId);
        return ResponseEntity.ok(results);
    }
}
