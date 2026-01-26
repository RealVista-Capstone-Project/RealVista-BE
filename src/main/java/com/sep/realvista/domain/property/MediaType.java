package com.sep.realvista.domain.property;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum MediaType {
    VIDEO("VIDEO"),
    IMAGE("IMAGE"),
    THREE_D("3D");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MediaType fromValue(String value) {
        for (MediaType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MediaType: " + value);
    }

    @Converter(autoApply = true)
    public static class MediaTypeConverter implements AttributeConverter<MediaType, String> {
        @Override
        public String convertToDatabaseColumn(MediaType mediaType) {
            return mediaType != null ? mediaType.getValue() : null;
        }

        @Override
        public MediaType convertToEntityAttribute(String value) {
            return value != null ? MediaType.fromValue(value) : null;
        }
    }
}
