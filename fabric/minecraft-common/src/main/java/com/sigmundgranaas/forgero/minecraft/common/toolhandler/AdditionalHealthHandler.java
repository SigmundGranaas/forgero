package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;

public class AdditionalHealthHandler {
	public static String HEALTH_ADDITION_TYPE = "HEALTH_ADDITION";

	public static Optional<Attribute> of(PropertyContainer container) {
		var key = PropertyTargetCacheKey.of(container, HEALTH_ADDITION_TYPE);
		boolean has = ContainsFeatureCache.check(key);
		if (has) {
			return Optional.of(AttributeCache.computeIfAbsent(key, () -> Attribute.of(compute(container), HEALTH_ADDITION_TYPE)));
		}
		return Optional.empty();
	}

	public static int compute(PropertyContainer container) {
		return container.stream()
				.features()
				.filter(feature -> feature.type().equals(HEALTH_ADDITION_TYPE))
				.map(data -> 1f)
				.reduce(0f, Float::sum)
				.intValue();
	}
}
