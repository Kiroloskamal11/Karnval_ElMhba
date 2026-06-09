package com.church.karneval.converter;

import com.church.karneval.enums.AgeGroup;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AgeGroupConverter implements AttributeConverter<AgeGroup, String> {
    @Override
    public String convertToDatabaseColumn(AgeGroup attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public AgeGroup convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return AgeGroup.valueOf(dbData.toUpperCase());
    }
}
