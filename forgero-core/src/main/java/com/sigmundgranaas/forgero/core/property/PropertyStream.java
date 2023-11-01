package com.sigmundgranaas.forgero.core.property;

import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.util.ForwardingStream;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * The property stream is a special stream for handling property specific operations.
 * This will make it easier to use this stream of properties by providing convenience methods like applyAttribute for reducing a specific attribute to a desired number.
 */
public record PropertyStream(
		Stream<Property> stream, Matchable target, MatchContext context) implements ForwardingStream<Property> {

	private static final PropertyStream EMPTY = new PropertyStream(Stream.empty(), Matchable.DEFAULT_TRUE, MatchContext.of());

	public static PropertyStream empty() {
		return EMPTY;
	}

	@Override
	public Stream<Property> getStream() {
		return stream;
	}

	public float applyAttribute(String attributeType) {
		return getAttributeOfType(attributeType)
				.reduce(0f, (collector, attribute) -> attribute.applyAttribute(target, context, collector), (a, b) -> b);
	}

	public ComputedAttribute compute(String attributeType) {
		return ComputedAttribute.of(applyAttribute(attributeType), attributeType);
	}

	public Stream<Attribute> getAttributeOfType(String attributeType) {
		var rootAttributes = getAttributes()
				.filter(attribute -> attributeType.equals(attribute.getAttributeType())).toList();
		
		return rootAttributes.stream()
				.sorted(Attribute::compareTo);
	}

	public Stream<Attribute> getAttributes() {
		return stream.filter(property -> property instanceof Attribute)
				.map(Attribute.class::cast);
	}

	public Stream<Feature> features() {
		return stream.filter(property -> property instanceof Feature)
				.map(Feature.class::cast);
	}

	public <T extends Feature> Stream<T> features(ClassKey<T> key) {
		return stream
				.filter(property -> property.type().equals(key.type()))
				.filter(key.clazz()::isInstance)
				.map(key.clazz()::cast);
	}

	public PropertyStream with(Property property) {
		return new PropertyStream(Stream.concat(stream, Stream.of(property)), target, context);
	}

	public PropertyStream with(Stream<Property> properties) {
		return new PropertyStream(Stream.concat(stream, properties), target, context);
	}
}
