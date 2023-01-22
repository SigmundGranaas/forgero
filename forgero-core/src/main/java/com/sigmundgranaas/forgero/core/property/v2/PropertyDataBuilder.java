package com.sigmundgranaas.forgero.core.property.v2;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

public class PropertyDataBuilder {
    public static Property buildFromPojo(PropertyPojo.Feature pojo) {
        return PropertyData.builder()
                .level(pojo.level)
                .priority(pojo.priority)
                .id(pojo.id)
                .type(pojo.type)
                .name(pojo.name)
                .tags(pojo.tags)
                .direction(pojo.direction)
                .pattern(pojo.pattern)
                .description(pojo.description)
                .value(pojo.value)
                .build();
    }
}
