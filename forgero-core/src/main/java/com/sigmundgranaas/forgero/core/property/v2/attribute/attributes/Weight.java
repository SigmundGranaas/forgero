package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;


public class Weight implements Attribute {

	public static final String KEY = "WEIGHT";
	private final float value;

	public Weight(ContainerTargetPair pair) {
		this.value = pair.container().stream().applyAttribute(pair.target(), AttributeType.WEIGHT);
	}

	public static Weight of(PropertyContainer container) {
		var pair = ContainerTargetPair.of(container);
		return (Weight) AttributeCache.computeIfAbsent(pair, () -> new Weight(pair), KEY);
	}

	public static Float apply(PropertyContainer container) {
		return of(container).asFloat();
	}

	public static Float apply(PropertyContainer container, Target target) {
		return of(container, target).asFloat();
	}

	public static Weight of(PropertyContainer container, Target target) {
		var pair = new ContainerTargetPair(container, target);
		return (Weight) AttributeCache.computeIfAbsent(pair, () -> new Weight(pair), KEY);
	}

	@Override
	public String key() {
		return KEY;
	}

	@Override
	public Float asFloat() {
		return value;
	}

	public float reduceAttackSpeed(float speed) {
		long negatedValue = Math.abs((int) this.value - 100);
		var percentage = (float) (Math.min(Math.max(0.1, (float) negatedValue / 100), 1));
		return (percentage * (speed + 4)) - 4;
	}

	public float reduceMiningSpeed(float speed) {
		long negatedValue = Math.abs((int) this.value - 100);
		var percentage = (float) (Math.min(Math.max(0.1, (float) negatedValue / 100), 1));
		return percentage * speed;
	}
}
