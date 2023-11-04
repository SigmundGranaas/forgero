package com.sigmundgranaas.forgero.core.property.v2;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.PropertyStream;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class ComputedAttributeBuilder {
	private List<PropertyContainer> sources;
	private MatchContext context;
	private String attributeKey;
	private Function<PropertyStream, ComputedAttribute> computeFunction;
	private Function<Collection<PropertyContainer>, PropertyStream> streamFunction = (containers) -> containers.stream().reduce(PropertyStream.empty(), (c1, c2) -> c1.stream(Matchable.DEFAULT_TRUE, this.context()).with(c2.stream()));


	public ComputedAttributeBuilder(List<PropertyContainer> sources, MatchContext context, String attributeKey, Function<PropertyStream, ComputedAttribute> computeFunction) {
		this.sources = sources;
		this.context = context;
		this.attributeKey = attributeKey;
		this.computeFunction = computeFunction;
	}

	public ComputedAttributeBuilder(String attributeKey) {
		this.sources = new ArrayList<>();
		this.context = MatchContext.of();
		this.attributeKey = attributeKey;
		this.computeFunction = (propertyStream -> ComputedAttribute.of(propertyStream.applyAttribute(attributeKey), attributeKey));
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComputedAttributeBuilder that = (ComputedAttributeBuilder) o;
		return Objects.equals(sources, that.sources) && Objects.equals(context, that.context) && Objects.equals(attributeKey, that.attributeKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sources, context, attributeKey);
	}
}
