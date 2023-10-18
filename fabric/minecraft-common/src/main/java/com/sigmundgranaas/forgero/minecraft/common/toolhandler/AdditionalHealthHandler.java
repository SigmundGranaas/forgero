package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;

public class AdditionalHealthHandler {
	public static String HEALTH_ADDITION_TYPE = "minecraft:health";

	public static Optional<ComputedAttribute> of(PropertyContainer container) {
		var key = PropertyTargetCacheKey.of(container, HEALTH_ADDITION_TYPE);

		return Optional.of(AttributeCache.computeIfAbsent(key, () -> ComputedAttribute.of(container, HEALTH_ADDITION_TYPE)));
	}
}
