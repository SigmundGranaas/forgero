package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.util.ForwardingStream;

import java.util.stream.Stream;

public record PropertyStream(
        Stream<Property> stream) implements ForwardingStream<Property> {

    @Override
    public Stream<Property> getStream() {
        return stream;
    }


    public float applyAttribute(Target target, AttributeType attributeType) {
        return getAttributeOfType(attributeType)
                .reduce(0f, (collector, attribute) -> attribute.applyAttribute(target, collector), (a, b) -> b);
    }

    private Stream<Attribute> getAttributeOfType(AttributeType attributeType) {
        return getAttributes()
                .filter(attribute -> attributeType == attribute.getAttributeType())
                .sorted(Attribute::compareTo);
    }

    private Stream<Attribute> getAttributes() {
        return stream.filter(property -> property instanceof Attribute)
                .map(Attribute.class::cast);
    }
}
