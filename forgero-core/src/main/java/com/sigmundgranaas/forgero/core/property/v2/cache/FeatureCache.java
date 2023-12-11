package com.sigmundgranaas.forgero.core.property.v2.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import org.jetbrains.annotations.NotNull;

public class FeatureCache {
	public static final LoadingCache<FeatureContainerKey, Boolean> containsFeatureCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.softValues()
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				Boolean load(@NotNull FeatureContainerKey key) {
					return key.pair().container().stream().features(key.key()).findFirst().isPresent();
				}
			});

	public static final LoadingCache<FeatureContainerKey, List<Feature>> featureCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.softValues()
			.build(new CacheLoader<>() {
				@Override
				public @NotNull List<Feature> load(@NotNull FeatureContainerKey key) {
					return key.pair().container()
							.stream().features(key.key())
							.filter(feat -> feat.applyCondition(key.pair().target(), key.pair().context()))
							.collect(Collectors.toList());
				}
			});

	public static boolean check(ClassKey<? extends Feature> key, PropertyContainer container) {
		return check(FeatureContainerKey.of(container, key));
	}

	public static <T extends Feature> List<T> get(ClassKey<T> key, PropertyContainer container) {
		try {
			return (List<T>) featureCache.get(new FeatureContainerKey(ContainerTargetPair.of(container), key));
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public static boolean check(FeatureContainerKey key) {
		try {
			return containsFeatureCache.get(key);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean check(Set<String> keys, Function<String, FeatureContainerKey> keyMapper) {
		return keys.stream().map(keyMapper).anyMatch(FeatureCache::check);
	}
}
