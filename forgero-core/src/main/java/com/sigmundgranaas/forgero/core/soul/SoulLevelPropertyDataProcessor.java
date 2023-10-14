package com.sigmundgranaas.forgero.core.soul;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureRegistry;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SoulLevelPropertyData;

public class SoulLevelPropertyDataProcessor implements PropertyLevelProvider {
	private final SoulLevelPropertyData data;

	public SoulLevelPropertyDataProcessor(SoulLevelPropertyData data) {
		var newDataBuilder = SoulLevelPropertyData.builder();
		if (data.getProperties() != null) {
			var props = new PropertyPojo();
			props.features = Objects.requireNonNullElse(data.getProperties().features, Collections.emptyList());
			props.setAttributes(Objects.requireNonNullElse(data.getProperties().getAttributes(), Collections.emptyList()));
			newDataBuilder.properties(props);
		}
		this.data = newDataBuilder.build();
	}

	public String target() {
		return data.getId();
	}


	@Override
	public List<Property> apply(Integer level) {
		List<Property> attributes = data.getProperties().getAttributes().stream()
				.map((attribute) -> AttributeBuilder.createAttributeBuilder(attribute, new PredicateFactory()))
				.map(builder -> builder.applyLevel(level))
				.map(AttributeBuilder::build)
				.collect(Collectors.toList());

		List<Property> features = data.getProperties().features.stream()
				.map(FeatureRegistry::of)
				.flatMap(Optional::stream)
				.collect(Collectors.toList());

		return Stream.of(attributes, features).flatMap(List::stream).toList();
	}
}
