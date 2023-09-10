package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;

public class AttackDamage {
	public static final String KEY = "ATTACK_DAMAGE";

	public static Attribute of(PropertyContainer container) {
		return Attribute.of(container, KEY);
	}

	public static Float apply(PropertyContainer container) {
		return of(container).asFloat();
	}

	public static Float apply(PropertyContainer container, Target target) {
		return of(container, target).asFloat();
	}

	public static Attribute of(PropertyContainer container, Target target) {
		return Attribute.of(new ContainerTargetPair(container, target), KEY);
	}

}
