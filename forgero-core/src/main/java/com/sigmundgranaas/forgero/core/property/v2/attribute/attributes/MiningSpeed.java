package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class MiningSpeed {

	public static final String KEY = "MINING_SPEED";


	public static ComputedAttribute of(PropertyContainer container) {
		return ComputedAttribute.of(container, KEY);
	}

	public static Float apply(PropertyContainer container) {
		return ComputedAttribute.apply(container, KEY);
	}

	public static Float apply(PropertyContainer container, Matchable target, MatchContext context) {
		return ComputedAttribute.apply(container, KEY, target, context);
	}

	public static ComputedAttribute of(PropertyContainer container, Matchable target, MatchContext context) {
		return ComputedAttribute.of(new ContainerTargetPair(container, target, context), KEY);
	}
}
