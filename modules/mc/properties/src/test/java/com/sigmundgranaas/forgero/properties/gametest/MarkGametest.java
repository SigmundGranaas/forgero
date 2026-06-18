package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.effects.mark.ClearMarkHandler;
import com.sigmundgranaas.forgero.effects.mark.MarkHandler;
import com.sigmundgranaas.forgero.effects.mark.MarkStore;
import com.sigmundgranaas.forgero.properties.minecraft.condition.TargetHasMarkCondition;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.HasMarkFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Phase 0 + 1: NBT-backed entity marks, and the mark/clear effects, has_mark filter, and
 * target_has_mark condition that build on them.
 */
public class MarkGametest {

	private static final Identifier MARK = new Identifier("forgero", "test_mark");

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMarkStoreBasics(TestContext context) {
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));

		context.assertFalse(MarkStore.hasMark(entity, MARK), "Entity should start unmarked");
		MarkStore.mark(entity, MARK, 100);
		context.assertTrue(MarkStore.hasMark(entity, MARK), "Entity should be marked after mark()");
		MarkStore.clearMark(entity, MARK);
		context.assertFalse(MarkStore.hasMark(entity, MARK), "clearMark() should remove the mark");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMarkExpires(TestContext context) {
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		MarkStore.mark(entity, MARK, 5);
		context.assertTrue(MarkStore.hasMark(entity, MARK), "Mark should be present immediately");

		// World game-time advances each tick; past expiry the mark reads as absent (and is pruned).
		context.waitAndRun(10, () -> {
			context.assertFalse(MarkStore.hasMark(entity, MARK), "Mark should expire after its duration");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMarkAndClearHandlers(TestContext context) {
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));

		new MarkHandler(MARK, 100).apply(entity);
		context.assertTrue(MarkStore.hasMark(entity, MARK), "MarkHandler should apply the mark");

		new ClearMarkHandler(MARK).apply(entity);
		context.assertFalse(MarkStore.hasMark(entity, MARK), "ClearMarkHandler should remove the mark");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testHasMarkFilterDetonation(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity marked = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		LivingEntity unmarked = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));

		MarkStore.mark(marked, MARK, 100);

		AreaOfEffectSelector selector = new AreaOfEffectSelector(6, List.of(new HasMarkFilter(MARK)));
		List<Entity> selected = selector.select(source, marked);

		context.assertTrue(selected.contains(marked), "has_mark filter should keep the marked entity");
		context.assertFalse(selected.contains(unmarked), "has_mark filter should drop the unmarked entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testTargetHasMarkCondition(TestContext context) {
		LivingEntity marked = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		LivingEntity unmarked = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 1));
		MarkStore.mark(marked, MARK, 100);

		TargetHasMarkCondition condition = new TargetHasMarkCondition(MARK);

		DynamicContext markedCtx = new DynamicContext.Builder()
				.put(MinecraftContextKeys.TARGET_ENTITY, marked).build();
		DynamicContext unmarkedCtx = new DynamicContext.Builder()
				.put(MinecraftContextKeys.TARGET_ENTITY, unmarked).build();

		context.assertTrue(condition.test(markedCtx), "Condition should pass for a marked target");
		context.assertFalse(condition.test(unmarkedCtx), "Condition should fail for an unmarked target");
		context.complete();
	}
}
