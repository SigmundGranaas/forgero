package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.util.ForwardingStream;

import java.util.stream.Stream;

/**
 * The property stream is a special stream for handling property specific operations.
 * This will make it easier to use this stream of properties by providing convenience methods like applyAttribute for reducing a specific attribute to a desired number.
 */
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

    public Stream<Attribute> getAttributeOfType(AttributeType attributeType) {
        return getAttributes()
                .filter(attribute -> attributeType == attribute.getAttributeType())
                .sorted(Attribute::compareTo);
    }

    public Stream<Attribute> getAttributes() {
        return stream.filter(property -> property instanceof Attribute)
                .map(Attribute.class::cast);
    }
}
