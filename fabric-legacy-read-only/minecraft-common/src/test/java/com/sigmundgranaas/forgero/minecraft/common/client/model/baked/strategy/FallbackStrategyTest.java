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

class FallbackStrategyTest {

	private ModelStrategy base;
	private ModelStrategy fallback;
	private State state;
	private MatchContext context;
	private BakedModelResult baseModelResult;
	private BakedModelResult fallbackModelResult;

	@BeforeEach
	void setUp() {
		state = new SimpleState("forgero:test-state", Type.of("test"), Collections.emptyList());
		context = MatchContext.of();
		baseModelResult = new BakedModelResult(new ModelResult(), new DynamicBakedModel());
		fallbackModelResult = new BakedModelResult(new ModelResult(), new DynamicBakedModel());
	}

	@Test
	void returnsBaseModelWhenBaseProvidesResult() {
		base = (s, c) -> Optional.of(baseModelResult);
		fallback = (s, c) -> Optional.of(fallbackModelResult);
		FallbackStrategy strategy = new FallbackStrategy(base, fallback);

		Optional<BakedModelResult> result = strategy.getModel(state, context);
		Assertions.assertTrue(result.isPresent(), "Result should be present when base provides a model");
		Assertions.assertEquals(baseModelResult, result.get(), "Returned model should be from the base strategy");
	}

	@Test
	void returnsFallbackModelWhenBaseFails() {
		base = (s, c) -> Optional.empty();
		fallback = (s, c) -> Optional.of(fallbackModelResult);
		FallbackStrategy strategy = new FallbackStrategy(base, fallback);

		Optional<BakedModelResult> result = strategy.getModel(state, context);
		Assertions.assertTrue(result.isPresent(), "Result should be present when fallback provides a model");
		Assertions.assertEquals(fallbackModelResult, result.get(), "Returned model should be from the fallback strategy");
	}

	@Test
	void returnsEmptyWhenBothStrategiesFail() {
		base = (s, c) -> Optional.empty();
		fallback = (s, c) -> Optional.empty();
		FallbackStrategy strategy = new FallbackStrategy(base, fallback);

		Optional<BakedModelResult> result = strategy.getModel(state, context);
		Assertions.assertFalse(result.isPresent(), "Result should be empty when both strategies fail");
	}
}
