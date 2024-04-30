package com.sigmundgranaas.forgero.core.property.v2.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.Callable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import org.jetbrains.annotations.NotNull;

public class AttributeCache {
	public static final LoadingCache<AttributeContainerKey, ComputedAttribute> attributeCache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.of(10, ChronoUnit.SECONDS))
			.softValues()
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				ComputedAttribute load(@NotNull AttributeCache.AttributeContainerKey stack) {
					return ComputedAttribute.of(1f, "UNDEFINED");
				}
			});

	public static final LoadingCache<AttributeContainerKey, Boolean> containsAttributeCache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.softValues()
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				Boolean load(@NotNull AttributeCache.AttributeContainerKey stack) {
					return stack.container().stream().getAttributes().anyMatch(attr -> attr.type().equals(stack.key()));
				}
			});


	public static Boolean has(AttributeContainerKey pair) {
		try {
			return containsAttributeCache.get(pair);
		} catch (Exception e) {
			return false;
		}
	}

	public static ComputedAttribute computeIfAbsent(AttributeContainerKey key, Callable<ComputedAttribute> compute) {
		try {
			return attributeCache.get(key, compute);
		} catch (Exception e) {
			return ComputedAttribute.of(1f, "UNDEFINED");
		}
	}

	public record AttributeContainerKey(PropertyContainer container, String key) {
		public static AttributeContainerKey of(PropertyContainer container, String key) {
			return new AttributeContainerKey(container, key);
		}

		@Override
		public int hashCode() {
			return Objects.hash(container, key);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AttributeContainerKey that = (AttributeContainerKey) o;
			return Objects.equals(container, that.container) && Objects.equals(key, that.key);
		}

		@Override
		public String toString() {
			return String.format("%s-%s", container.hashCode(), key);
		}
	}
}
