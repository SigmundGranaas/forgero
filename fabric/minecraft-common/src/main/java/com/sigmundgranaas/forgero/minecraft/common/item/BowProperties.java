package com.sigmundgranaas.forgero.minecraft.common.item;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Flexibility;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Force;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;

public class BowProperties {
	private static final float MIN_FLEXIBILITY = 0;

	private static final float BASELINE_FLEXIBILITY = 50;

	private static final float MIN_FORCE = 0;

	private static final float BASELINE_FORCE = 50;

	private final float flexibility;
	private final float force;

	public BowProperties(float flexibility, float force) {
		this.flexibility = flexibility;
		this.force = force;
	}

	public static Optional<BowProperties> fromItemStack(ItemStack itemStack, StateService service) {
		Optional<State> optionalState = service.convert(itemStack);
		return optionalState.map(state -> new BowProperties(Force.of(state).asFloat(), Flexibility.of(state).asFloat()));
	}

	public float getFlexibility() {
		return (flexibility - MIN_FLEXIBILITY) / (BASELINE_FLEXIBILITY - MIN_FLEXIBILITY);
	}

	public float getForce() {
		return (force - MIN_FORCE) / (BASELINE_FORCE - MIN_FORCE);
	}


}
