package com.sigmundgranaas.forgero.core.property.v2.feature;

import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

import java.util.Collections;

public class PropertyDataBuilder {
    public static PropertyData buildFromPojo(PropertyPojo.Feature pojo) {
        return PropertyData.builder()
                .level(pojo.level)
                .priority(pojo.priority)
                .id(pojo.id)
                .type(pojo.type)
                .name(pojo.name)
                .tags(pojo.tags == null ? Collections.emptyList() : pojo.tags)
                .direction(pojo.direction)
                .pattern(pojo.pattern)
                .description(pojo.description)
                .value(pojo.value)
                .build();
    }
}
