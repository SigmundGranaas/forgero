package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;
import org.jetbrains.annotations.Nullable;


/**
 * A model strategy that lazily initializes and caches a model based on a hash code match with the state.
 * This class is designed to defer the creation of a model until it is actually required and then cache
 * it for subsequent requests, provided the state's hash code matches a specified code.
 * <p>
 * Usage of this class is appropriate when model creation is resource-intensive and should be delayed
 * or when models are frequently requested for certain states and can be effectively cached.
 *
 * @implSpec This implementation checks if the state's hash code matches a predefined code. If it does
 * and a model has already been created and cached, that model is returned. If no model is cached yet,
 * the model is created using a provided {@link Supplier<BakedModelResult>}, cached, and then returned.
 * If the state's hash code does not match the predefined code, an empty {@link Optional} is returned.
 */
public class LazyDefaultModelStrategy implements ModelStrategy {
	@Nullable
	private BakedModelResult result;

	private final int code;

	private final Supplier<BakedModelResult> model;

	public LazyDefaultModelStrategy(int code, Supplier<BakedModelResult> model) {
		this.code = code;
		this.model = model;
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		if (code == state.hashCode() && result != null) {
			return Optional.of(result);
		} else if (code == state.hashCode() && result == null) {
			this.result = model.get();
			return Optional.of(result);
		}

		return Optional.empty();
	}
}
