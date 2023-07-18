package com.sigmundgranaas.forgero.minecraft.common.item;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Stability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;

public class ArrowProperties {
	private static final float MIN_STABILITY = 0;
	private static final float BASELINE_STABILITY = 20;

	private static final float MIN_WEIGHT = 0;
	private static final float BASELINE_WEIGHT = 3.1f;

	private final float stability;
	private final float weight;

	public ArrowProperties(float stability, float weight) {
		this.stability = stability;
		this.weight = weight;
	}


	public static ArrowProperties fromItemStack(ItemStack itemStack, StateService service) {
		Optional<State> optionalState = service.convert(itemStack);
		if (optionalState.isPresent()) {
			State state = optionalState.get();
			return new ArrowProperties(Stability.of(state).asFloat(), Weight.of(state).asFloat());
		} else {
			return new ArrowProperties(1.0F, 1.0F);  // Default values for vanilla arrows
		}
	}

	public float getStability() {
		return (stability - MIN_STABILITY) / (BASELINE_STABILITY - MIN_STABILITY);
	}

	public float getWeight() {
		return (weight - MIN_WEIGHT) / (BASELINE_WEIGHT - MIN_WEIGHT);
	}
}
