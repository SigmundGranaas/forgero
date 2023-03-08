package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.Callable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

public class BlockHandlerCache {
	public static final LoadingCache<String, ToolBlockHandler> blockHandlerLoadingCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				ToolBlockHandler load(@NotNull String key) {
					return ToolBlockHandler.EMPTY;
				}
			});

	public static ToolBlockHandler computeIfAbsent(CacheAbleKey key, Callable<ToolBlockHandler> compute) {
		try {
			return blockHandlerLoadingCache.get(key.key(), compute);
		} catch (Exception e) {
			return ToolBlockHandler.EMPTY;
		}
	}

	public static void remove(CacheAbleKey key) {
		blockHandlerLoadingCache.invalidate(key.key());
	}

	public record BlockStateCacheKey(CacheAbleKey blockState, PropertyContainer container,
	                                 Direction[] directions) implements CacheAbleKey {

		@Override
		public String key() {
			return blockState.key() + container.hashCode() + Arrays.toString(directions);
		}
	}
}
