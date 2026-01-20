package com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy;

import java.util.Collections;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.state.SimpleState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.DynamicBakedModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayeredCachedSingleStateStrategyTest {

	private State state;
	private MatchContext context;
	private BakedModelResult baseModelResult;
	private BakedModelResult fallbackModelResult;
	private LayeredCachedSingleStateStrategy strategy;

	@BeforeEach
	void setUp() {
		state = new SimpleState("forgero:test-state", Type.of("test"), Collections.emptyList());
		context = MatchContext.of();
		baseModelResult = new BakedModelResult(new ModelResult(), new DynamicBakedModel());
		fallbackModelResult = new BakedModelResult(new ModelResult(), new DynamicBakedModel());
		strategy = new LayeredCachedSingleStateStrategy();
	}

	@Test
	void testReturnsCachedModelIfValid() {
		strategy.then((s, ctx) -> Optional.of(baseModelResult)); // Setting fallback to always return a valid model.

		// Force cache the model first time
		strategy.getModel(state, context);

		// Change the fallback strategy to return a different model
		BakedModelResult newModelResult = new BakedModelResult(new ModelResult(), new DynamicBakedModel());
		strategy.then((s, ctx) -> Optional.of(newModelResult));

		Optional<BakedModelResult> result = strategy.getModel(state, context);
		Assertions.assertTrue(result.isPresent(), "Model should be retrieved from cache");
		Assertions.assertEquals(baseModelResult, result.get(), "Cached model should match the initially cached model");
	}

	@Test
	void testFallbackUsedWhenCacheMiss() {
		strategy.then((s, ctx) -> Optional.of(fallbackModelResult));

		// No prior caching, should fall back
		Optional<BakedModelResult> result = strategy.getModel(state, context);
		Assertions.assertTrue(result.isPresent(), "Model should be fetched from fallback");
		Assertions.assertEquals(fallbackModelResult, result.get(), "Fallback model should be returned");
	}
}
