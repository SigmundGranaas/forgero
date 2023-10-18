package com.sigmundgranaas.forgero.core.property.v2.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import org.jetbrains.annotations.NotNull;

public class AttributeCache {
	public static final LoadingCache<PropertyTargetCacheKey, ComputedAttribute> attributeCache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				ComputedAttribute load(@NotNull PropertyTargetCacheKey stack) {
					return ComputedAttribute.of(1f, "UNDEFINED");
				}
			});

	public static ComputedAttribute computeIfAbsent(ContainerTargetPair pair, Callable<ComputedAttribute> compute, String key) {
		try {
			return attributeCache.get(new PropertyTargetCacheKey(pair, key), compute);
		} catch (Exception e) {
			return ComputedAttribute.of(1f, "UNDEFINED");
		}
	}

	public static ComputedAttribute computeIfAbsent(PropertyTargetCacheKey key, Callable<ComputedAttribute> compute) {
		try {
			return attributeCache.get(key, compute);
		} catch (Exception e) {
			return ComputedAttribute.of(1f, "UNDEFINED");
		}
	}
}
