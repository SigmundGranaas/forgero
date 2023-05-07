package com.sigmundgranaas.forgero.core.property.v2.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.property.v2.RunnableHandler;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

public class RunnableHandlerCache {
	public static final LoadingCache<PropertyTargetCacheKey, RunnableHandler> runnableHandlerCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				RunnableHandler load(@NotNull PropertyTargetCacheKey key) {
					return RunnableHandler.EMPTY;
				}
			});

	public static RunnableHandler computeIfAbsent(PropertyTargetCacheKey key, Callable<RunnableHandler> compute) {
		try {
			return runnableHandlerCache.get(key, compute);
		} catch (Exception e) {
			return RunnableHandler.EMPTY;
		}
	}
}
