package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;

import java.util.Optional;

public class AdditionalHealthHandler {
	public static String HEALTH_ADDITION_TYPE = "minecraft:health";

	public static Optional<ComputedAttribute> of(PropertyContainer container) {
		var key = AttributeCache.AttributeContainerKey.of(container, HEALTH_ADDITION_TYPE);
		if (AttributeCache.has(key)) {
			return Optional.of(AttributeCache.computeIfAbsent(key, () -> ComputedAttribute.of(container, HEALTH_ADDITION_TYPE)));
		}
		return Optional.empty();
	}
}
