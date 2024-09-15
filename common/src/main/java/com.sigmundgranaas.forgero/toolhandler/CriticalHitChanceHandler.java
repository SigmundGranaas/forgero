package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;

import java.util.Optional;

public class CriticalHitChanceHandler {
	public static String CRITICAL_HIT_CHANCE_TYPE = "forgero:critical_hit_chance";

	public static Optional<ComputedAttribute> of(PropertyContainer container) {
		var key = AttributeCache.AttributeContainerKey.of(container, CRITICAL_HIT_CHANCE_TYPE);
		boolean has = AttributeCache.has(key);
		if (has) {
			return Optional.of(AttributeCache.computeIfAbsent(key, () -> ComputedAttribute.of(computeCriticalHitChance(container), CRITICAL_HIT_CHANCE_TYPE)));
		}
		return Optional.empty();
	}

	public static float computeCriticalHitChance(PropertyContainer container) {
		return container.stream()
				.applyAttribute(CRITICAL_HIT_CHANCE_TYPE);
	}
}
