package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.feature.tick.EntityTickFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.tick.EntityTickFeatureExecutor;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.EntityBasedHandler;

import net.minecraft.entity.Entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class EntityTickExecutorTest {
	@Test
	void featureIsExecutedOnce() {
		AtomicInteger executionCount = new AtomicInteger();
		EntityBasedHandler runnable = (Entity source) -> executionCount.addAndGet(1);

		EntityTickFeature feature = new EntityTickFeature(new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:entity_tick", Matchable.DEFAULT_TRUE, EMPTY_IDENTIFIER, Collections.emptyList()), List.of(runnable));

		EntityTickFeatureExecutor executor = new EntityTickFeatureExecutor(List.of(feature), null, null, null);

		executor.execute(MatchContext.of());

		Assertions.assertEquals(executionCount.get(), 1, "Feature is executed once");
	}

	@Test
	void featureMatchIsExecutedOnceAndStopsHandling() {
		AtomicInteger executionCount = new AtomicInteger();
		EntityBasedHandler runnable = (Entity source) -> executionCount.addAndGet(1);
		AtomicInteger matchableCount = new AtomicInteger();

		Matchable matchable = (Matchable target, MatchContext context) -> {
			matchableCount.addAndGet(1);
			return false;
		};

		EntityTickFeature feature = new EntityTickFeature(new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:entity_tick", matchable, EMPTY_IDENTIFIER, Collections.emptyList()), List.of(runnable));

		EntityTickFeatureExecutor executor = new EntityTickFeatureExecutor(List.of(feature), null, null, null);

		executor.execute(MatchContext.of());

		Assertions.assertEquals(1, matchableCount.get(), "The predicate is not executed");
		Assertions.assertEquals(0, executionCount.get(), "Feature is executed even though the predicate return false.");
	}
	@Test
	void executesAndFiltersMultipleFeatures() {
		AtomicInteger executionCount = new AtomicInteger();
		EntityBasedHandler runnable = (Entity source) -> executionCount.addAndGet(1);
		AtomicInteger matchableCount = new AtomicInteger();

		Matchable matchable = (Matchable target, MatchContext context) -> {
			matchableCount.addAndGet(1);
			return false;
		};

		AtomicInteger executionCount2 = new AtomicInteger();
		EntityBasedHandler runnable2 = (Entity source) -> executionCount2.addAndGet(1);
		AtomicInteger matchableCount2 = new AtomicInteger();

		Matchable matchable2 = (Matchable target, MatchContext context) -> {
			matchableCount2.addAndGet(1);
			return true;
		};

		EntityTickFeature feature = new EntityTickFeature(new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:entity_tick", matchable, EMPTY_IDENTIFIER, Collections.emptyList()), List.of(runnable));
		EntityTickFeature feature2 = new EntityTickFeature(new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:entity_tick", matchable2, EMPTY_IDENTIFIER, Collections.emptyList()), List.of(runnable2));

		EntityTickFeatureExecutor executor = new EntityTickFeatureExecutor(List.of(feature, feature2), null, null, null);

		executor.execute(MatchContext.of());

		Assertions.assertEquals(1, matchableCount.get(), "The predicate is not executed");
		Assertions.assertEquals(1, matchableCount2.get(), "The predicate is not executed");

		Assertions.assertEquals(0, executionCount.get(), "Feature is executed even though the predicate return false.");
		Assertions.assertEquals(1, executionCount2.get(), "Feature is not executed even though the predicate return true. Fails to handle multiple features.");
	}
}
