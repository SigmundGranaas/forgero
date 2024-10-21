package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.feature.swinghand.SwingHandFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.swinghand.SwingHandFeatureExecutor;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.swing.EntityHandHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

class SwingHandFeatureExecutorTest {

	@Test
	void featureIsExecutedOnce() {
		AtomicInteger executionCount = new AtomicInteger();
		EntityHandHandler runnable = (Entity source, Hand hand) -> executionCount.addAndGet(1);

		SwingHandFeature feature = new SwingHandFeature(BasePredicateData.empty("minecraft:on_swing"), List.of(runnable), Collections.emptyList());

		SwingHandFeatureExecutor executor = new SwingHandFeatureExecutor(List.of(feature), null, null, null);

		executor.executeIfNotCoolingDown(MatchContext.of());

		Assertions.assertEquals(executionCount.get(), 1, "Feature is executed once");
	}

	@Test
	void featureMatchIsExecutedOnceAndStopsHandling() {
		AtomicInteger executionCount = new AtomicInteger();
		EntityHandHandler runnable = (Entity source, Hand hand) -> executionCount.addAndGet(1);
		AtomicInteger matchableCount = new AtomicInteger();

		Matchable matchable = (Matchable target, MatchContext context) -> {
			matchableCount.addAndGet(1);
			return false;
		};

		SwingHandFeature feature = new SwingHandFeature(new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:on_swing", matchable, EMPTY_IDENTIFIER, Collections.emptyList()), List.of(runnable), Collections.emptyList());

		SwingHandFeatureExecutor executor = new SwingHandFeatureExecutor(List.of(feature), null, null, null);

		executor.executeIfNotCoolingDown(MatchContext.of());

		Assertions.assertEquals(1, matchableCount.get(), "The predicate is not executed");
		Assertions.assertEquals(0, executionCount.get(), "Feature is executed even though the predicate return false.");
	}
	@Test
	void executesAndFiltersMultipleFeatures() {
		AtomicInteger executionCount = new AtomicInteger();
		EntityHandHandler runnable = (Entity source, Hand hand) -> executionCount.addAndGet(1);
		AtomicInteger matchableCount = new AtomicInteger();

		Matchable matchable = (Matchable target, MatchContext context) -> {
			matchableCount.addAndGet(1);
			return false;
		};

		AtomicInteger executionCount2 = new AtomicInteger();
		EntityHandHandler runnable2 = (Entity source, Hand hand) -> executionCount2.addAndGet(1);
		AtomicInteger matchableCount2 = new AtomicInteger();

		Matchable matchable2 = (Matchable target, MatchContext context) -> {
			matchableCount2.addAndGet(1);
			return true;
		};

		SwingHandFeature feature = new SwingHandFeature(new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:on_swing", matchable, EMPTY_IDENTIFIER, Collections.emptyList()), List.of(runnable), Collections.emptyList());
		SwingHandFeature feature2 = new SwingHandFeature(new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:on_swing", matchable2, EMPTY_IDENTIFIER, Collections.emptyList()), List.of(runnable2), Collections.emptyList());

		SwingHandFeatureExecutor executor = new SwingHandFeatureExecutor(List.of(feature, feature2), null, null, null);

		executor.executeIfNotCoolingDown(MatchContext.of());

		Assertions.assertEquals(1, matchableCount.get(), "The predicate is not executed");
		Assertions.assertEquals(1, matchableCount2.get(), "The predicate is not executed");

		Assertions.assertEquals(0, executionCount.get(), "Feature is executed even though the predicate return false.");
		Assertions.assertEquals(1, executionCount2.get(), "Feature is not executed even though the predicate return true. Fails to handle multiple features.");
	}

	@Test
	void afterUseIsExecuted() {
		AtomicInteger executionCount = new AtomicInteger();
		AtomicInteger afterUseCount = new AtomicInteger();

		EntityHandHandler runnable = (Entity source, Hand hand) -> executionCount.addAndGet(1);
		AfterUseHandler afterUse = (Entity source, ItemStack target, Hand hand) -> afterUseCount.addAndGet(1);

		SwingHandFeature feature = new SwingHandFeature(BasePredicateData.empty("minecraft:on_swing"), List.of(runnable), List.of(afterUse));

		SwingHandFeatureExecutor executor = new SwingHandFeatureExecutor(List.of(feature), null, null, null);

		executor.executeIfNotCoolingDown(MatchContext.of());

		Assertions.assertEquals(afterUseCount.get(), executionCount.get(), "Execution and afterUse is not executed the same number of times.");
	}
}
