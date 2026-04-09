package com.sep.realvista.domain.billing.subscription;

public enum FeatureType {
    LISTING,
    @SuppressWarnings("java:S115")
    _3D_TOUR,
    AI_REQUEST;

    public String toDbValue() {
        if (this == _3D_TOUR) {
            return "3D_TOUR";
        }
        return this.name();
    }

    public static FeatureType fromDbValue(String dbValue) {
        if ("3D_TOUR".equals(dbValue)) {
            return _3D_TOUR;
        }
        return FeatureType.valueOf(dbValue);
    }
}
