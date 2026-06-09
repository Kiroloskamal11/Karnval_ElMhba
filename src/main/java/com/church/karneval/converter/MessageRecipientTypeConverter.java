package com.church.karneval.converter;

import com.church.karneval.enums.MessageRecipientType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MessageRecipientTypeConverter implements AttributeConverter<MessageRecipientType, String> {
    @Override
    public String convertToDatabaseColumn(MessageRecipientType attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public MessageRecipientType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return MessageRecipientType.valueOf(dbData.toUpperCase());
    }
}
