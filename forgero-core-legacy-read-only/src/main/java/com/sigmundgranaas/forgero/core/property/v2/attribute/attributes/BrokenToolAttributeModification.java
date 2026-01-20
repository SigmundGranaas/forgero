package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureContainerKey;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_KEY;

public record BrokenToolAttributeModification(float amount) implements AttributeModification {
	@Override
	public ComputedAttribute apply(ComputedAttribute attribute, PropertyContainer propertyContainer) {
		if (FeatureCache.check(FeatureContainerKey.of(propertyContainer, BROKEN_KEY))) {
			return ComputedAttribute.of(amount, attribute.key());
		}
		return attribute;
	}
}
