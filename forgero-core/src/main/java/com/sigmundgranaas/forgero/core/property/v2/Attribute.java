package com.sigmundgranaas.forgero.core.property.v2;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeModification;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeModificationRegistry;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public interface Attribute {

	static Attribute of(float value, String key) {
		return new Attribute() {
			@Override
			public String key() {
				return key;
			}

			@Override
			public Float asFloat() {
				return value;
			}

			@Override
			public Attribute modify(AttributeModification mod) {
				return this;
			}
		};
	}

	static Attribute of(PropertyContainer container, String key) {
		var pair = ContainerTargetPair.of(container);
		return of(pair, key);
	}

	static Attribute of(ContainerTargetPair pair, String key) {
		if (ContainsFeatureCache.check(PropertyTargetCacheKey.of(pair.container(), BROKEN_TYPE_KEY))) {
			return Attribute.of(0f, key);
		}
		return AttributeCache.computeIfAbsent(pair, () -> compute(pair, key), key);
	}

	static Attribute compute(ContainerTargetPair pair, String key) {
		Attribute attribute = new ComputedAttribute(key, pair);
		AttributeModificationRegistry.getModifications(key).forEach(attribute::modify);
		return attribute;
	}

	static Float apply(PropertyContainer container, String key) {
		return of(container, key).asFloat();
	}

	static Float apply(PropertyContainer container, String key, Matchable target) {
		var pair = ContainerTargetPair.of(container);
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

	Attribute modify(AttributeModification mod);

}
