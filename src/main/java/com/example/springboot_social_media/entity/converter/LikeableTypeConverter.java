package com.example.springboot_social_media.entity.converter;

import com.example.springboot_social_media.entity.LikeableType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LikeableTypeConverter implements AttributeConverter<LikeableType, String> {
    @Override
    public String convertToDatabaseColumn(LikeableType attribute) {
        if (attribute == null) return null;
        return attribute.getValue(); // already lowercase per enum value
    }

    @Override
    public LikeableType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (LikeableType type : LikeableType.values()) {
            if (type.getValue().equalsIgnoreCase(dbData)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown likeable type: " + dbData);
    }
}

