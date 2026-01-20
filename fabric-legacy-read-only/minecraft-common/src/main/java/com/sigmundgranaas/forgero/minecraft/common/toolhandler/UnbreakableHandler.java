package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureContainerKey;

import static com.sigmundgranaas.forgero.core.condition.Conditions.UNBREAKABLE_KEY;

public class UnbreakableHandler {

	public static boolean isUnbreakable(PropertyContainer container) {
		var key = FeatureContainerKey.of(container, UNBREAKABLE_KEY);
		return FeatureCache.check(key);
	}
}
