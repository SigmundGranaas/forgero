package com.sigmundgranaas.forgero.core.property.v2;


import java.util.Objects;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class ComputedAttributeBuilder {
	private PropertyContainer source;
	private MatchContext context;
	private String attributeKey;

	public ComputedAttributeBuilder(String attributeKey) {
		this.source = PropertyContainer.EMPTY;
		this.context = MatchContext.of();
		this.attributeKey = attributeKey;
	}

	public static ComputedAttributeBuilder of(String type) {
		return new ComputedAttributeBuilder(type);
	}

	public ComputedAttributeBuilder addSource(PropertyContainer container) {
		this.source = this.source.with(container);
		return this;
	}

	public ComputedAttributeBuilder addSource(Attribute attribute) {
		this.source = this.source.with(PropertyContainer.of(attribute));
		return this;
	}

	public ComputedAttributeBuilder context(MatchContext context) {
		this.context = context;
		return this;
	}

	public ComputedAttributeBuilder attribute(String attributeKey) {
		this.attributeKey = attributeKey;
		return this;
	}

	public ComputedAttribute build() {
		return ComputedAttribute.of(source, attributeKey);
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComputedAttributeBuilder that = (ComputedAttributeBuilder) o;
		return Objects.equals(source, that.source) && Objects.equals(context, that.context) && Objects.equals(attributeKey, that.attributeKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, context, attributeKey);
	}
}
