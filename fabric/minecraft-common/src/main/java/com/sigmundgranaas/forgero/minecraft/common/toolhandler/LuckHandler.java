package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;

import java.util.Optional;

public class LuckHandler {
	public static String LUCK_TYPE = "minecraft:luck";

	public static Optional<ComputedAttribute> of(PropertyContainer container) {
		var key = AttributeCache.AttributeContainerKey.of(container, LUCK_TYPE);
		boolean has = AttributeCache.has(key);
		if (has) {
			return Optional.of(AttributeCache.computeIfAbsent(key, () -> ComputedAttribute.of(compute(container), LUCK_TYPE)));
		}
		return Optional.empty();
	}

	public static float compute(PropertyContainer container) {
		return container.stream()
				.applyAttribute(LUCK_TYPE);
	}
}
