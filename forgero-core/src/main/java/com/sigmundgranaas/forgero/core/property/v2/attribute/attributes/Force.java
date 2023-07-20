package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;


public class Force implements Attribute {

	public static final String KEY = "FORCE";
	private final float value;

	public Force(ContainerTargetPair pair) {
		this.value = pair.container().stream().applyAttribute(pair.target(), AttributeType.FORCE);
	}

	public static Force of(PropertyContainer container) {
		var pair = ContainerTargetPair.of(container);
		return (Force) AttributeCache.computeIfAbsent(pair, () -> new Force(pair), KEY);
	}

	public static Float apply(PropertyContainer container) {
		return of(container).asFloat();
	}

	public static Float apply(PropertyContainer container, Target target) {
		return of(container, target).asFloat();
	}

	public static Force of(PropertyContainer container, Target target) {
		var pair = new ContainerTargetPair(container, target);
		return (Force) AttributeCache.computeIfAbsent(pair, () -> new Force(pair), KEY);
	}

	@Override
	public String key() {
		return KEY;
	}

	@Override
	public Float asFloat() {
		return value;
	}

}
