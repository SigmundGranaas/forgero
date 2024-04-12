package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.STACK;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;

import net.minecraft.item.ItemStack;

public class DefaultModelStrategy implements ModelStrategy {
	private final BakedModelResult result;
	private final int code;

	public DefaultModelStrategy(BakedModelResult result, int code) {
		this.result = result;
		this.code = code;
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		Optional<ItemStack> stack = context.get(STACK);
		if (stack.isPresent() && !stack.get().hasNbt()) {
			return Optional.of(result);
		} else if (code == state.hashCode()) {
			return Optional.of(result);
		} else {
			return Optional.empty();
		}
	}
}
