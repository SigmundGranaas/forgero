package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.state.SimpleState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AsyncModelStrategyTest {
	private final State testState = new SimpleState("forgero:test-state", Type.of("test"), Collections.emptyList());
	
	@Test
	void getModel() throws InterruptedException {
		// Set up the expected model result
		BakedModelResult delayedModel = new BakedModelResult(ModelResult.EMPTY, EMPTY);

		ModelStrategy blockingModelProvider = (state, context) -> {
			try {
				Thread.sleep(100); // simulate delay in synchronous operation
				return Optional.of(delayedModel);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Optional.empty();
			}
		};

		AsyncModelStrategy strategy = new AsyncModelStrategy(blockingModelProvider);

		// First return should be a default option
		Optional<BakedModelResult> result = strategy.getModel(testState, null);
		Assertions.assertTrue(result.isPresent());
		Assertions.assertNotEquals(delayedModel, result.get());

		Thread.sleep(200); // simulate a delay to let the strategy compute the model

		Optional<BakedModelResult> future = strategy.getModel(testState, MatchContext.of());

		// Ensure the proper model is present after async processing
		Assertions.assertTrue(future.isPresent());
		Assertions.assertEquals(result.get().model(), delayedModel.model(), "The nested model should match the expected model after async operation.");
	}

	@Test
	void getModelWithFallback() throws InterruptedException {
		// Set up the expected model result
		BakedModelResult delayedModel = new BakedModelResult(ModelResult.EMPTY, EMPTY);
		BakedModelResult fallbackModel = new BakedModelResult(ModelResult.EMPTY, EMPTY);


		ModelStrategy blockingModelProvider = (state, context) -> {
			try {
				Thread.sleep(100); // simulate delay in synchronous operation
				return Optional.of(delayedModel);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Optional.empty();
			}
		};


		Function<Integer, Optional<BakedModelResult>> fallback = (i) -> Optional.of(fallbackModel);

		AsyncModelStrategy strategy = new AsyncModelStrategy(blockingModelProvider, fallback);

		// First return should be the fallback option
		Optional<BakedModelResult> result = strategy.getModel(testState, null);
		Assertions.assertTrue(result.isPresent());
		Assertions.assertNotEquals(delayedModel, result.get());
		Assertions.assertEquals(fallbackModel, result.get());


		Thread.sleep(200); // simulate a delay to let the strategy compute the model

		Optional<BakedModelResult> future = strategy.getModel(testState, MatchContext.of());

		// Ensure the proper model is present after async processing
		Assertions.assertTrue(future.isPresent());
		Assertions.assertEquals(result.get().model(), delayedModel.model(), "The nested model should match the expected model after the async operation.");
	}
}
