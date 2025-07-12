package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public abstract class BaseComponentBuilder<T extends BaseComponentBuilder<T>> {
	protected final OpenIdentifier id;
	protected Set<OpenIdentifier> tags = new HashSet<>();
	protected List<Property> properties = new ArrayList<>();

	protected BaseComponentBuilder(OpenIdentifier id) {
		this.id = id;
	}

	public T withTag(OpenIdentifier tag) {
		this.tags.add(tag);
		return (T) this;
	}

	public T withTag(String tag) {
		this.tags.add(TestIdentifiers.id(tag));
		return (T) this;
	}

	public T withProperty(Property property) {
		this.properties.add(property);
		return (T) this;
	}

	public T withAttribute(OpenIdentifier type, float value) {
		return withProperty(ForgeroTestFactory.attribute(type).withValue(value).build());
	}
}
