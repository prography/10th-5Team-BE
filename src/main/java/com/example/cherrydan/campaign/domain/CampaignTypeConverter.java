package com.example.cherrydan.campaign.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CampaignTypeConverter implements AttributeConverter<CampaignType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(CampaignType attribute) {
        return attribute != null ? attribute.getCode() : null;
    }

    @Override
    public CampaignType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? CampaignType.fromCode(dbData) : null;
    }
} 