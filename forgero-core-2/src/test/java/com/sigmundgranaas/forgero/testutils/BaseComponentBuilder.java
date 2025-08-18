package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.context.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

@SuppressWarnings("unchecked")
public abstract class BaseComponentBuilder<T extends BaseComponentBuilder<T>> {
	protected final OpenIdentifier id;
	protected Set<OpenIdentifier> tags = new HashSet<>();
	protected Map<String, List<?>> properties = new HashMap<>();

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

	public <R> T withProperty(PropertyKey<R> key, R property) {
		if(properties.containsKey(key.key())) {
			List<R> newList = new ArrayList<>(List.of(property));
			List<R> list = (List<R>)properties.get(key);
			newList.addAll(list);
			properties.put(key.key(), newList);
		}else{
			properties.put(key.key(), new ArrayList<>(List.of(property)));
		}
		return (T) this;
	}

	public T withAttribute(OpenIdentifier type, float value) {
		return withProperty(KEY, ForgeroTestFactory.attribute(type).withValue(value).build());
	}
	public T withAttribute(Attribute attribute) {
		return withProperty(KEY, attribute);
	}
}
