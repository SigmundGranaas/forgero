package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class ComputedAttribute implements com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute {
	private final String key;
	private final PropertyContainer container;
	private final Matchable target;
	private final List<AttributeModification> modifications;


	public ComputedAttribute(String key, ContainerTargetPair pair) {
		this.key = key;
		this.container = pair.container();
		this.target = pair.target();
		this.modifications = new ArrayList<>();
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public Float asFloat() {
		float value = container.stream(target).applyAttribute(key());
		if (modifications.isEmpty()) {
			return value;
		}
		com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute computed = com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute.of(value, key());
		for (AttributeModification mod : modifications) {
			computed = mod.apply(computed, container);
		}
		return computed.asFloat();
	}

	@Override
	public com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute modify(AttributeModification mod) {
		modifications.add(mod);
		return this;
	}
}
