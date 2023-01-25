package com.sigmundgranaas.forgero.core.property.v2.cache;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

public record PropertyTargetCacheKey(ContainerTargetPair pair, String key) {
    public static PropertyTargetCacheKey of(PropertyContainer container, String key) {
        return new PropertyTargetCacheKey(ContainerTargetPair.of(container), key);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s-%s", pair.hashCode(), key);
    }
}
