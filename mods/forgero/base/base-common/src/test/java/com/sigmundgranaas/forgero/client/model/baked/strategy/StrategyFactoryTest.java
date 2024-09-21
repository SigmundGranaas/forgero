package com.sigmundgranaas.forgero.client.model.baked.strategy;

import static com.sigmundgranaas.forgero.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collections;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.model.Strategy;
import com.sigmundgranaas.forgero.core.state.SimpleState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.client.model.baked.BakedModelResult;
import com.sigmundgranaas.forgero.client.model.baked.DynamicBakedModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StrategyFactoryTest {

	private ModelStrategy baker;
	private State state;
	private StrategyFactory factory;

	@BeforeEach
	void setUp() {
		baker = new SimpleBaker();
		state = new SimpleState("forgero:test-state", Type.of("test"), Collections.emptyList());
	}

	@Test
	void testFullyAsyncStrategy() throws InterruptedException {
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
		factory = new StrategyFactory(blockingModelProvider, Strategy.FULLY_ASYNC);
		ModelStrategy strategy = factory.build(state);

		// First return should be the fallback option
		Optional<BakedModelResult> result = strategy.getModel(state, null);
		Assertions.assertTrue(result.isPresent());
		Assertions.assertNotEquals(delayedModel, result.get());

		Thread.sleep(200); // simulate a delay to let the strategy compute the model

		Optional<BakedModelResult> future = strategy.getModel(state, MatchContext.of());

		// Ensure the proper model is present after async processing
		Assertions.assertTrue(future.isPresent());
		Assertions.assertEquals(result.get().model(), delayedModel.model(), "The nested model should match the expected model after async operation.");
	}

	@Test
	void testPreBakedStrategy() throws InterruptedException {
		BakedModelResult model = new BakedModelResult(ModelResult.EMPTY, EMPTY);

		factory = new StrategyFactory((s, ctx) -> Optional.of(model), Strategy.PRE_BAKED);
		ModelStrategy strategy = factory.build(state);

		Thread.sleep(200); // simulate a delay to let the strategy compute the model


		// First return should be the fallback option
		Optional<BakedModelResult> result = strategy.getModel(state, MatchContext.of());
		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(model, result.get());
	}

	@Test
	void testNoAsyncStrategy() {
		BakedModelResult model = new BakedModelResult(ModelResult.EMPTY, EMPTY);

		factory = new StrategyFactory((s, ctx) -> Optional.of(model), Strategy.NO_ASYNC);
		ModelStrategy strategy = factory.build(state);

		// First return should be the fallback option
		Optional<BakedModelResult> result = strategy.getModel(state, MatchContext.of());
		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(model, result.get());
	}

	@Test
	void testNoCacheStrategy() {
		factory = new StrategyFactory(baker, Strategy.NO_CACHE);
		ModelStrategy strategy = factory.build(state);

		// Check that the returned strategy is the basic baker without any caching
		assertSame(baker, strategy, "Should return the base baker for NO_CACHE");
	}

	static class SimpleBaker implements ModelStrategy {
		@Override
		public Optional<BakedModelResult> getModel(State state, MatchContext context) {
			return Optional.of(new BakedModelResult(new ModelResult(), new DynamicBakedModel()));
		}
	}
}
