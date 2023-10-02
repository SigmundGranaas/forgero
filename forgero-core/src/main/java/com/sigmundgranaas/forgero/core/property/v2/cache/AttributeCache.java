package com.sigmundgranaas.forgero.core.property.v2.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import org.jetbrains.annotations.NotNull;

public class AttributeCache {
	public static final LoadingCache<PropertyTargetCacheKey, Attribute> attributeCache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				Attribute load(@NotNull PropertyTargetCacheKey stack) {
					return Attribute.of(1f, "UNDEFINED");
				}
			});

	public static Attribute computeIfAbsent(ContainerTargetPair pair, Callable<Attribute> compute, String key) {
		try {
			return attributeCache.get(new PropertyTargetCacheKey(pair, key), compute);
		} catch (Exception e) {
			return Attribute.of(1f, "UNDEFINED");
		}
	}

	public static Attribute computeIfAbsent(PropertyTargetCacheKey key, Callable<Attribute> compute) {
		try {
			return attributeCache.get(key, compute);
		} catch (Exception e) {
			return Attribute.of(1f, "UNDEFINED");
		}
	}
}
