package com.sigmundgranaas.forgero.minecraft.common.feature;

import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;

public class FeatureUtils {
	public static <T extends Feature> Stream<T> streamFeature(ItemStack stack, MatchContext context, ClassKey<T> key) {
		return StateService.INSTANCE.convert(stack)
				.map(state -> state.stream(Matchable.DEFAULT_TRUE, context)
						.features(key)).orElseGet(Stream::empty);
	}

	public static <T extends Feature> Stream<T> streamFeature(PropertyContainer container, MatchContext context, ClassKey<T> key) {
		return container.stream(Matchable.DEFAULT_TRUE, context)
				.features(key);
	}
}
