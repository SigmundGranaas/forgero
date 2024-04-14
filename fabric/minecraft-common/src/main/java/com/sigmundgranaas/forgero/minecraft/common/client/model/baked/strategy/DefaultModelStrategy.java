package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.STACK;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;

import net.minecraft.item.ItemStack;

/**
 * Provides a default model based on specific conditions related to the state and context.
 * This strategy returns a preconfigured model if the state's hash code matches a specified
 * code or if a certain item in the context lacks NBT data.
 * <p>
 * This class is ideal for scenarios where a model should be returned under certain common
 * conditions without recalculating or regenerating the model each time.
 */
public class DefaultModelStrategy implements ModelStrategy {
	private final BakedModelResult result;
	private final int code;

	public DefaultModelStrategy(BakedModelResult result, int code) {
		this.result = result;
		this.code = code;
	}

	/**
	 * Retrieves a model based on the provided state and context.
	 * Returns the preconfigured model if the state's hash code matches the specified code or
	 * if an item in the context is present and does not contain NBT data.
	 *
	 * @param state   The state potentially matching the specified code.
	 * @param context The context that may contain the relevant world, item or entity values.
	 * @return An Optional containing the model if conditions are met; otherwise, empty.
	 */
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
