package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;

import java.util.Optional;

public class LumaHandler {
    public static String EMISSIVE_TYPE = "EMISSIVE";

    public static Optional<Attribute> of(PropertyContainer container) {
        var key = PropertyTargetCacheKey.of(container, EMISSIVE_TYPE);
        boolean has = ContainsFeatureCache.check(key);
        if (has) {
            return Optional.of(AttributeCache.computeIfAbsent(key, () -> Attribute.of(computeLuma(container), EMISSIVE_TYPE)));
        }
        return Optional.empty();
    }

    public static int computeLuma(PropertyContainer container) {
        int value = container.stream()
                .features()
                .filter(feature -> feature.type().equals(EMISSIVE_TYPE))
                .map(PropertyData::getValue)
                .reduce(0f, Float::sum)
                .intValue();
        return Math.min(value, 16);
    }
}
