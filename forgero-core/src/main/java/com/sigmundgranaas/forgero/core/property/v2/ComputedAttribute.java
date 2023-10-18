package com.sigmundgranaas.forgero.core.property.v2;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeModification;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeModificationRegistry;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public interface ComputedAttribute {

	static ComputedAttribute of(float value, String key) {
		return new ComputedAttribute() {
			@Override
			public String key() {
				return key;
			}

			@Override
			public Float asFloat() {
				return value;
			}

			@Override
			public ComputedAttribute modify(AttributeModification mod) {
				return this;
			}
		};
	}

	static ComputedAttribute of(PropertyContainer container, String key) {
		var pair = ContainerTargetPair.of(container);
		return of(pair, key);
	}

	static ComputedAttribute of(ContainerTargetPair pair, String key) {
		if (ContainsFeatureCache.check(PropertyTargetCacheKey.of(pair.container(), BROKEN_TYPE_KEY))) {
			return ComputedAttribute.of(0f, key);
		}
		return AttributeCache.computeIfAbsent(pair, () -> compute(pair, key), key);
	}

	static ComputedAttribute compute(ContainerTargetPair pair, String key) {
		ComputedAttribute attribute = new com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.ComputedAttribute(key, pair);
		AttributeModificationRegistry.getModifications(key).forEach(attribute::modify);
		return attribute;
	}

	static Float apply(PropertyContainer container, String key) {
		return of(container, key).asFloat();
	}

	static Float apply(PropertyContainer container, String key, Matchable target, MatchContext context) {
		var pair = ContainerTargetPair.of(container, target, context);
		return of(pair, key).asFloat();
	}

	String key();

	default Integer asInt() {
		return asFloat().intValue();
	}

	Float asFloat();

	default Double asDouble() {
		return asFloat().doubleValue();
	}

	ComputedAttribute modify(AttributeModification mod);

}
