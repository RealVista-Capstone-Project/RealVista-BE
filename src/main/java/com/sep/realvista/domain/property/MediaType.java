package com.sep.realvista.domain.property.media;

public enum MediaType {
    VIDEO,
    IMAGE,
    THREE_D("3D");

    private final String value;

    MediaType() {
        this.value = name();
    }

    MediaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
