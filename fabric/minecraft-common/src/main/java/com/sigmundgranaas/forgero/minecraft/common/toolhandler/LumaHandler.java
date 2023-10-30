package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;

import java.util.Optional;

public class LumaHandler {
	public static String EMISSIVE_TYPE = "forgero:emissive";

	public static Optional<ComputedAttribute> of(PropertyContainer container) {
		AttributeCache.AttributeContainerKey key = AttributeCache.AttributeContainerKey.of(container, EMISSIVE_TYPE);

		if (AttributeCache.has(key)) {
			return Optional.of(AttributeCache.computeIfAbsent(key, () -> ComputedAttribute.of(computeLuma(container), EMISSIVE_TYPE)));
		}
		return Optional.empty();
	}

	public static int computeLuma(PropertyContainer container) {
		int value = ComputedAttribute.of(container, EMISSIVE_TYPE).asInt();
		return Math.min(value, 16);
	}
}
