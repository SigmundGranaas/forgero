package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;

public class UnbreakableHandler {
    public static String TYPE = "UNBREAKABLE";

    public static boolean isUnbreakable(PropertyContainer container) {
        var key = PropertyTargetCacheKey.of(container, TYPE);
        return ContainsFeatureCache.check(key);
    }
}
