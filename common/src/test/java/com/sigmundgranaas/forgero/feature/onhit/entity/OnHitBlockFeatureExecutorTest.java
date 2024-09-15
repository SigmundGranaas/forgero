package com.sigmundgranaas.forgero.feature.onhit.entity;

import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.feature.onhit.block.OnHitBlockFeature;
import com.sigmundgranaas.forgero.feature.onhit.block.OnHitBlockFeatureExecutor;
import com.sigmundgranaas.forgero.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.handler.targeted.onHitBlock.BlockTargetHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

class OnHitBlockFeatureExecutorTest {
	@Test
	void featureIsExecutedOnce() {
		AtomicInteger executionCount = new AtomicInteger();

		BlockTargetHandler runnable = (Entity source, World world, BlockPos target) -> executionCount.addAndGet(1);

		OnHitBlockFeature feature = new OnHitBlockFeature(BasePredicateData.empty("minecraft:on_hit_block"), List.of(runnable), Collections.emptyList());

		OnHitBlockFeatureExecutor executor = new OnHitBlockFeatureExecutor(List.of(feature), null, null, null, null, null);

		executor.executeIfNotCoolingDown(MatchContext.of());

		Assertions.assertEquals(executionCount.get(), 1, "Feature is executed once");
	}

	@Test
	void featureMatchIsExecutedOnceAndStopsHandling() {
		AtomicInteger executionCount = new AtomicInteger();
		AtomicInteger matchableCount = new AtomicInteger();

		Matchable matchable = (Matchable target, MatchContext context) -> {
			matchableCount.addAndGet(1);
			return false;
		};

		BlockTargetHandler runnable = (Entity source, World world, BlockPos target) -> executionCount.addAndGet(1);

		BasePredicateData base = new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:on_hit_block", matchable, EMPTY_IDENTIFIER, Collections.emptyList());

		OnHitBlockFeature feature = new OnHitBlockFeature(base, List.of(runnable), Collections.emptyList());

		OnHitBlockFeatureExecutor executor = new OnHitBlockFeatureExecutor(List.of(feature), null, null, null, null, null);

		executor.executeIfNotCoolingDown(MatchContext.of());

		Assertions.assertEquals(1, matchableCount.get(), "The predicate is not executed");
		Assertions.assertEquals(0, executionCount.get(), "Feature is executed even though the predicate return false.");
	}

	@Test
	void executesAndFiltersMultipleFeatures() {
		AtomicInteger executionCount = new AtomicInteger();
		BlockTargetHandler runnable = (Entity source, World world, BlockPos target) -> executionCount.addAndGet(1);
		AtomicInteger matchableCount = new AtomicInteger();

		Matchable matchable = (Matchable target, MatchContext context) -> {
			matchableCount.addAndGet(1);
			return false;
		};

		AtomicInteger executionCount2 = new AtomicInteger();
		BlockTargetHandler runnable2 = (Entity source, World world, BlockPos target) -> executionCount2.addAndGet(1);
		AtomicInteger matchableCount2 = new AtomicInteger();

		Matchable matchable2 = (Matchable target, MatchContext context) -> {
			matchableCount2.addAndGet(1);
			return true;
		};

		BasePredicateData base = new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:on_hit_block", matchable, EMPTY_IDENTIFIER, Collections.emptyList());

		OnHitBlockFeature feature = new OnHitBlockFeature(base, List.of(runnable), Collections.emptyList());

		BasePredicateData base2 = new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:on_hit_block", matchable2, EMPTY_IDENTIFIER, Collections.emptyList());

		OnHitBlockFeature feature2 = new OnHitBlockFeature(base2, List.of(runnable2), Collections.emptyList());

		OnHitBlockFeatureExecutor executor = new OnHitBlockFeatureExecutor(List.of(feature, feature2), null, null, null, null, null);

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

		BlockTargetHandler runnable = (Entity source, World world, BlockPos target) -> executionCount.addAndGet(1);
		AfterUseHandler afterUse = (Entity source, ItemStack target, Hand hand) -> afterUseCount.addAndGet(1);

		BasePredicateData base = new BasePredicateData(EMPTY_IDENTIFIER, "minecraft:on_hit_block", Matchable.DEFAULT_TRUE, EMPTY_IDENTIFIER, Collections.emptyList());

		OnHitBlockFeature feature = new OnHitBlockFeature(base, List.of(runnable), List.of(afterUse));

		OnHitBlockFeatureExecutor executor = new OnHitBlockFeatureExecutor(List.of(feature), null, null, null, null, null);

		executor.executeIfNotCoolingDown(MatchContext.of());

		Assertions.assertEquals(afterUseCount.get(), executionCount.get(), "Execution and afterUse is not executed the same number of times.");
	}
}
