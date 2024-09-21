package com.sigmundgranaas.forgero.feature;

import static com.sigmundgranaas.forgero.utils.PropertyUtils.container;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class FeatureUtils {
	public static <T extends Feature> List<T> cachedRootFeatures(ItemStack stack, ClassKey<T> key) {
		var state = StateService.INSTANCE.convert(stack);
		if (state.isEmpty() || !FeatureCache.check(key, state.get())) {
			return Collections.emptyList();
		}

		return FeatureCache.getRootFeatures(key, state.get());
	}

	public static <T extends Feature> List<T> appliedCachedFeatures(ItemStack stack, MatchContext context, ClassKey<T> key) {
		var state = StateService.INSTANCE.convert(stack);
		if (state.isEmpty() || !FeatureCache.check(key, state.get())) {
			return Collections.emptyList();
		}

		return FeatureCache.apply(key, state.get(), context);
	}

	public static <T extends Feature> List<T> cachedFilteredFeatures(ItemStack stack, ClassKey<T> key, MatchContext context) {
		return appliedCachedFeatures(stack, context, key);
	}


	public static <T extends Feature> Optional<T> cachedFeature(ItemStack stack, ClassKey<T> key) {
		var state = StateService.INSTANCE.convert(stack);
		if (state.isEmpty() || !FeatureCache.check(key, state.get())) {
			return Optional.empty();
		}

		return FeatureCache.getRootFeatures(key, state.get())
				.stream()
				.findFirst();
	}

	public static <T extends Feature> Optional<T> cachedFilteredFeature(ItemStack stack, ClassKey<T> key, MatchContext context) {
		return cachedFeature(stack, key)
				.stream()
				.filter(feat -> feat.applyCondition(Matchable.DEFAULT_TRUE, context))
				.findFirst();
	}

	public static Attribute of(JsonObject value, String jsonKey, String key, float defaultValue) {
		if (value.has(jsonKey)) {
			return BaseAttribute.of(value.get(jsonKey).getAsFloat(), key);
		} else {
			return BaseAttribute.of(defaultValue, key);
		}
	}

	public static Attribute of(JsonObject value, String jsonKey, String key, int defaultValue) {
		return of(value, jsonKey, key, (float) defaultValue);
	}

	public static ComputedAttribute compute(Attribute base, Entity source) {
		return ComputedAttributeBuilder.of(base.type())
				.addSource(container(source))
				.addSource(base)
				.build();
	}
}
