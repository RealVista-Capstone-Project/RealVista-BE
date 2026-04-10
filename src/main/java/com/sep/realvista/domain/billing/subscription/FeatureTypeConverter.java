package com.sep.realvista.domain.billing.subscription;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FeatureTypeConverter implements AttributeConverter<FeatureType, String> {

    @Override
    public String convertToDatabaseColumn(FeatureType featureType) {
        if (featureType == null) {
            return null;
        }
        return featureType.toDbValue();
    }

    @Override
    public FeatureType convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return FeatureType.fromDbValue(dbValue);
    }
}
