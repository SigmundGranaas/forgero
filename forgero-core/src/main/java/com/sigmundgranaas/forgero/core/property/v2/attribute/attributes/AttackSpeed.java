package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;

public class AttackSpeed implements Attribute {
	public static String KEY = "ATTACK_SPEED";

	private final float value;

	public AttackSpeed(ContainerTargetPair pair) {
		this.value = pair.container().stream().applyAttribute(pair.target(), AttributeType.ATTACK_SPEED);
	}

	public static Attribute of(PropertyContainer container) {
		var pair = ContainerTargetPair.of(container);
		return AttributeCache.computeIfAbsent(pair, () -> new AttackSpeed(pair), KEY);
	}

	public static Float apply(PropertyContainer container) {
		return Weight.of(container).reduceAttackSpeed(of(container).asFloat());
	}

	public static Float apply(PropertyContainer container, Target target) {
		return Weight.of(container).reduceAttackSpeed(of(container, target).asFloat());
	}

	public static Attribute of(PropertyContainer container, Target target) {
		var pair = new ContainerTargetPair(container, target);
		return AttributeCache.computeIfAbsent(pair, () -> new AttackSpeed(pair), KEY);
	}

	@Override
	public String key() {
		return KEY;
	}

	@Override
	public Float asFloat() {
		return Math.max(value, -3.9f);
	}
}
