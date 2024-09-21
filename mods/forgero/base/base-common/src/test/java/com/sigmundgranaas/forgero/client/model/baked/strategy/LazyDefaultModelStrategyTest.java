package com.sigmundgranaas.forgero.client.model.baked.strategy;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.state.SimpleState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.client.model.baked.BakedModelResult;
import com.sigmundgranaas.forgero.client.model.baked.DynamicBakedModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LazyDefaultModelStrategyTest {
	private State state;
	private State matchingState;

	private MatchContext context;
	private BakedModelResult modelResult;
	private AtomicInteger wasSupplierCalled;

	@BeforeEach
	void setUp() {
		state = new SimpleState("forgero:test-state", Type.of("test"), Collections.emptyList());
		matchingState = new SimpleState("forgero:matching-test-state", Type.of("test"), Collections.emptyList());
		context = MatchContext.of();
		modelResult = new BakedModelResult(new ModelResult(), new DynamicBakedModel());
		wasSupplierCalled = new AtomicInteger(0);
	}

	Supplier<BakedModelResult> createSupplier() {
		return () -> {
			wasSupplierCalled.set(wasSupplierCalled.get() + 1);
			return modelResult;
		};
	}

	@Test
	void testModelIsLazilyLoadedAndCached() {
		LazyDefaultModelStrategy strategy = new LazyDefaultModelStrategy(matchingState.hashCode(), createSupplier());

		// Initial call should load the model
		Optional<BakedModelResult> firstResult = strategy.getModel(matchingState, context);
		Assertions.assertTrue(firstResult.isPresent(), "Model should be loaded when called first time");
		Assertions.assertEquals(modelResult, firstResult.get(), "The returned model should match the expected model");
		Assertions.assertEquals(1, wasSupplierCalled.get(), "Supplier should have been called on first access");

		// Second call should use cached result, no supplier call
		Optional<BakedModelResult> secondResult = strategy.getModel(matchingState, context);
		Assertions.assertTrue(secondResult.isPresent(), "Model should be available on subsequent calls");
		Assertions.assertEquals(modelResult, secondResult.get(), "The cached model should be returned on subsequent calls");
		Assertions.assertEquals(1, wasSupplierCalled.get(), "Supplier should not be called again for cached data");
	}

	@Test
	void testModelNotReturnedForNonMatchingCode() {
		LazyDefaultModelStrategy strategy = new LazyDefaultModelStrategy(matchingState.hashCode(), createSupplier());

		// Non-matching code should not load or return a model
		Optional<BakedModelResult> result = strategy.getModel(state, context);
		Assertions.assertTrue(result.isEmpty(), "No model should be returned for non-matching code");
		Assertions.assertEquals(0, wasSupplierCalled.get(), "Supplier should not be called for non-matching state code");
	}
}
