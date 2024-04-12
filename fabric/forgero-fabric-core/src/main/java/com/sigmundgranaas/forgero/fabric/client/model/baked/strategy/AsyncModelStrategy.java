package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;
import com.sigmundgranaas.forgero.minecraft.common.client.model.DynamicBakedModel;

public class AsyncModelStrategy implements ModelStrategy {
	private final ModelStrategy modelProvider;
	private final Map<Integer, BakedModelResult> currentlyBuilding;
	private final ModelStrategy baseModelProvider;

	public static Supplier<ModelStrategy> of(ModelStrategy modelProvider) {
		return () -> new AsyncModelStrategy(modelProvider);
	}

	public static Supplier<ModelStrategy> of(ModelStrategy modelProvider, Function<Integer, Optional<BakedModelResult>> optionalGetter) {
		return () -> new AsyncModelStrategy(modelProvider, optionalGetter);
	}

	public AsyncModelStrategy(ModelStrategy modelProvider) {
		this.modelProvider = modelProvider;
		this.currentlyBuilding = new ConcurrentHashMap<>();
		this.baseModelProvider = (state, context) -> Optional.of(this.emptyBaseModel());
	}

	public AsyncModelStrategy(ModelStrategy modelProvider, Function<Integer, Optional<BakedModelResult>> optionalGetter) {
		this.modelProvider = modelProvider;
		this.currentlyBuilding = new ConcurrentHashMap<>();
		this.baseModelProvider = (state, context) -> optionalGetter.apply(state.hashCode());
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		int key = state.hashCode();
		if (currentlyBuilding.containsKey(key)) {
			return Optional.ofNullable(currentlyBuilding.get(key));
		}

		return triggerAsyncRebuild(state, context);
	}

	private Optional<BakedModelResult> triggerAsyncRebuild(State state, MatchContext context) {
		int key = state.hashCode();
		BakedModelResult base = baseModelProvider.getModel(state, context).orElseGet(this::emptyBaseModel);
		currentlyBuilding.put(key, base);
		CompletableFuture
				.supplyAsync(() -> modelProvider.getModel(state, context).get())
				.thenAccept((m) -> {
					BakedModelResult builtModel = currentlyBuilding.get(key);
					builtModel.result(m.result());
					builtModel.model(m.model());

					currentlyBuilding.remove(key);
				});
		return Optional.of(base);
	}

	private BakedModelResult emptyBaseModel() {
		DynamicBakedModel dynamicModel = new DynamicBakedModel();
		ModelResult result = new ModelResult();
		return new BakedModelResult(result, dynamicModel);
	}
}
