package com.sigmundgranaas.forgero.core.property.v2.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.FloatBasedAttribute;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

public class AttributeCache {
    public static final LoadingCache<PropertyTargetCacheKey, Attribute> attributeCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Attribute load(@NotNull PropertyTargetCacheKey stack) {
                    return new FloatBasedAttribute(1f, "UNDEFINED");
                }
            });

    public static Attribute computeIfAbsent(ContainerTargetPair pair, Callable<Attribute> compute, String key) {
        try {
            return attributeCache.get(new PropertyTargetCacheKey(pair, key), compute);
        } catch (Exception e) {
            return new FloatBasedAttribute(1f, "UNDEFINED");
        }
    }
}
