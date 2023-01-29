package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyDataBuilder;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SoulLevelPropertyData;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoulLevelPropertyDataProcessor implements PropertyLevelProvider {
    private final SoulLevelPropertyData data;

    public SoulLevelPropertyDataProcessor(SoulLevelPropertyData data) {
        var newDataBuilder = SoulLevelPropertyData.builder();
        if (data.getProperties() != null) {
            var props = new PropertyPojo();
            props.features = Objects.requireNonNullElse(data.getProperties().features, Collections.emptyList());
            props.attributes = Objects.requireNonNullElse(data.getProperties().attributes, Collections.emptyList());
            newDataBuilder.properties(props);
        }
        this.data = newDataBuilder.build();
    }

    public String target() {
        return data.getId();
    }


    @Override
    public List<Property> apply(Integer level) {
        List<Property> attributes = data.getProperties().attributes.stream()
                .map(AttributeBuilder::createAttributeBuilder)
                .map(builder -> builder.applyLevel(level))
                .map(AttributeBuilder::build)
                .collect(Collectors.toList());

        List<Property> features = data.getProperties().features.stream()
                .map(PropertyDataBuilder::buildFromPojo)
                .map(builder -> builder.toBuilder().level(level).build())
                .collect(Collectors.toList());

        return Stream.of(attributes, features).flatMap(List::stream).toList();
    }
}
