package com.sigmundgranaas.forgero.minecraft.common.item;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Accuracy;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import lombok.Getter;

import net.minecraft.item.ItemStack;

@Getter
public class ArrowProperties {
	private static final float MIN_STABILITY = 0;
	private static final float BASELINE_STABILITY = 20;

	private static final float MIN_WEIGHT = 0;
	private static final float BASELINE_WEIGHT = 3.1f;

	private final float accuracy;
	private final float weight;

	public ArrowProperties(float accuracy, float weight) {
		this.accuracy = accuracy;
		this.weight = weight;
	}

	public static ArrowProperties fromItemStack(ItemStack itemStack, StateService service) {
		Optional<State> optionalState = service.convert(itemStack);
		if (optionalState.isPresent()) {
			State state = optionalState.get();
			return new ArrowProperties(ComputedAttribute.of(state, Accuracy.KEY).asFloat(), ComputedAttribute.of(state, Weight.KEY).asFloat());
		} else {
			return new ArrowProperties(50f, 25f);  // Default values for vanilla arrows
		}
	}

}
