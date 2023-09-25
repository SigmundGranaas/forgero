package com.sigmundgranaas.forgero.core.property;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.property.passive.PassiveProperty;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.core.util.ForwardingStream;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * The property stream is a special stream for handling property specific operations.
 * This will make it easier to use this stream of properties by providing convenience methods like applyAttribute for reducing a specific attribute to a desired number.
 */
public record PropertyStream(
		Stream<Property> stream, Matchable target, MatchContext context) implements ForwardingStream<Property> {

	@Override
	public Stream<Property> getStream() {
		return stream;
	}

	public float applyAttribute(String attributeType) {
		return getAttributeOfType(attributeType)
				.reduce(0f, (collector, attribute) -> attribute.applyAttribute(target, context, collector), (a, b) -> b);
	}

	public Stream<Attribute> getAttributeOfType(String attributeType) {
		var rootAttributes = getAttributes()
				.filter(attribute -> attributeType.equals(attribute.getAttributeType())).toList();

		Map<String, Attribute> idMap = rootAttributes
				.stream()
				.filter(attribute -> !attribute.getId().equals(Identifiers.EMPTY_IDENTIFIER))
				.collect(Collectors.toMap(Attribute::getId, attribute -> attribute, (existing, replacement) -> existing.getPriority() > replacement.getPriority() ? existing : replacement));

		var nonIdAttributes = rootAttributes
				.stream()
				.filter(attribute -> attribute.getId().equals(Identifiers.EMPTY_IDENTIFIER))
				.toList();

		return Stream.of(idMap.values(), nonIdAttributes)
				.flatMap(Collection::stream)
				.sorted(Attribute::compareTo);
	}

	public Stream<Attribute> getAttributes() {
		return stream.filter(property -> property instanceof Attribute)
				.map(Attribute.class::cast);
	}

	public Stream<PropertyData> features() {
		return stream.filter(property -> property instanceof PropertyData)
				.map(PropertyData.class::cast);
	}

}
