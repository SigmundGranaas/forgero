package com.sigmundgranaas.forgero.core.property.v2.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

public class ContainsFeatureCache {
	public static final LoadingCache<PropertyTargetCacheKey, Boolean> containsFeatureCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				Boolean load(@NotNull PropertyTargetCacheKey key) {
					return key.pair().container().stream().features().anyMatch(type -> key.key().equals(type.type()));
				}
			});

	public static boolean check(PropertyTargetCacheKey key) {
		return containsFeatureCache.getUnchecked(key);
	}

	public static boolean check(Set<String> keys, Function<String, PropertyTargetCacheKey> keyMapper) {
		return keys.stream().map(keyMapper).anyMatch(ContainsFeatureCache::check);
	}
}
