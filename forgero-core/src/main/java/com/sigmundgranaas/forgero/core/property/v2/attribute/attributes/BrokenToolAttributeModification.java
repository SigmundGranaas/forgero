package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;

public record BrokenToolAttributeModification(float amount) implements AttributeModification {
	@Override
	public Attribute apply(Attribute attribute, PropertyContainer propertyContainer) {
		if (ContainsFeatureCache.check(PropertyTargetCacheKey.of(propertyContainer, BROKEN_TYPE_KEY))) {
			return Attribute.of(amount, attribute.key());
		}
		return attribute;
	}
}
