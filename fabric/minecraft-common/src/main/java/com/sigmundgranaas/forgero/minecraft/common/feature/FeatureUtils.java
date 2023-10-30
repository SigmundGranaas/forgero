package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import net.minecraft.item.ItemStack;

import java.util.stream.Stream;

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
}
