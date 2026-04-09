package com.sep.realvista.domain.property.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class Property3DGenerationCompletedEvent extends ApplicationEvent {
    private final UUID propertyId;
    private final UUID uploaderId;
    private final boolean success;

    public Property3DGenerationCompletedEvent(Object source, UUID propertyId, UUID uploaderId, boolean success) {
        super(source);
        this.propertyId = propertyId;
        this.uploaderId = uploaderId;
        this.success = success;
    }
}
