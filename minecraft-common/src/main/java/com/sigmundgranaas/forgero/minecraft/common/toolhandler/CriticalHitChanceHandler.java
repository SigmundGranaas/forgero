package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;

import java.util.Optional;

public class CriticalHitChanceHandler {
    public static String CRITICAL_HIT_CHANCE_TYPE = "CRITICAL_HIT_CHANCE";

    public static Optional<Attribute> of(PropertyContainer container) {
        var key = PropertyTargetCacheKey.of(container, CRITICAL_HIT_CHANCE_TYPE);
        boolean has = ContainsFeatureCache.check(key);
        if (has) {
            return Optional.of(AttributeCache.computeIfAbsent(key, () -> Attribute.of(computeCriticalHitChance(container), CRITICAL_HIT_CHANCE_TYPE)));
        }
        return Optional.empty();
    }

    public static float computeCriticalHitChance(PropertyContainer container) {
        return container.stream()
                .features()
                .filter(feature -> feature.type().equals(CRITICAL_HIT_CHANCE_TYPE))
                .map(PropertyData::getValue)
                .reduce(0f, Float::sum);
    }
}
