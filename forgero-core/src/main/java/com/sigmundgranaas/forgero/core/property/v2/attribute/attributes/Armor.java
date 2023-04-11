package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;

public class Armor implements Attribute {

	public static final String KEY = "ARMOR";
	private final float value;

	public Armor(ContainerTargetPair pair) {
		this.value = pair.container().stream().applyAttribute(pair.target(), KEY);
	}

	public static Attribute of(PropertyContainer container) {
		var pair = ContainerTargetPair.of(container);
		if (ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BROKEN_TYPE_KEY))) {
			return Attribute.of(0f, KEY);
		}
		return AttributeCache.computeIfAbsent(pair, () -> new Armor(pair), KEY);
	}

	public static Float apply(PropertyContainer container) {
		return of(container).asFloat();
	}

	public static Float apply(PropertyContainer container, Target target) {
		return of(container, target).asFloat();
	}

	public static Attribute of(PropertyContainer container, Target target) {
		var pair = new ContainerTargetPair(container, target);
		if (ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BROKEN_TYPE_KEY))) {
			return Attribute.of(0f, KEY);
		}
		return AttributeCache.computeIfAbsent(pair, () -> new Armor(pair), KEY);
	}

	@Override
	public String key() {
		return KEY;
	}

	@Override
	public Float asFloat() {
		return value;
	}
}
