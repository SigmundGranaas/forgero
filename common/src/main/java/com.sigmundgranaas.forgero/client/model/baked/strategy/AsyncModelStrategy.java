package com.sigmundgranaas.forgero.client.model.baked.strategy;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.client.model.baked.BakedModelResult;
import com.sigmundgranaas.forgero.client.model.baked.DynamicBakedModel;

/**
 * A model strategy that handles model provisioning asynchronously, allowing non-blocking
 * computations in environments where model preparation is resource-intensive.
 * This class maintains an internal cache of ongoing model constructions to avoid duplicate work.
 * <p>
 * Models are built asynchronously using either the common ForkJoinPool or a provided custom Executor,
 * making this strategy adaptable to various concurrency requirements.
 */
public class AsyncModelStrategy implements ModelStrategy {
	private final ModelStrategy modelProvider;
	private final Map<Integer, BakedModelResult> currentlyBuilding;
	private final ModelStrategy baseModelProvider;
	private final Executor executor;

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
		this.executor = ForkJoinPool.commonPool();
	}

	public AsyncModelStrategy(ModelStrategy modelProvider, Function<Integer, Optional<BakedModelResult>> optionalGetter) {
		this.modelProvider = modelProvider;
		this.currentlyBuilding = new ConcurrentHashMap<>();
		this.baseModelProvider = (state, context) -> optionalGetter.apply(state.hashCode());
		this.executor = ForkJoinPool.commonPool();
	}

	public AsyncModelStrategy(ModelStrategy modelProvider, Function<Integer, Optional<BakedModelResult>> optionalGetter, Executor executor) {
		this.modelProvider = modelProvider;
		this.currentlyBuilding = new ConcurrentHashMap<>();
		this.baseModelProvider = (state, context) -> optionalGetter.apply(state.hashCode());
		this.executor = executor;
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
				.supplyAsync(() -> modelProvider.getModel(state, context).get(), executor)
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
