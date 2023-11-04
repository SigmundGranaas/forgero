package com.sigmundgranaas.forgero.minecraft.common.feature;

import static com.sigmundgranaas.forgero.minecraft.common.utils.PropertyUtils.container;

import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class FeatureUtils {
	public static <T extends Feature> Stream<T> streamFeature(ItemStack stack, MatchContext context, ClassKey<T> key) {
		var state = StateService.INSTANCE.convert(stack);
		if (state.isEmpty() || !FeatureCache.check(key, state.get())) {
			return Stream.empty();
		}

		return state.get().stream(Matchable.DEFAULT_TRUE, context)
				.features(key);
	}

	public static <T extends Feature> Stream<T> streamFeature(PropertyContainer container, MatchContext context, ClassKey<T> key) {
		return container.stream(Matchable.DEFAULT_TRUE, context)
				.features(key);
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
