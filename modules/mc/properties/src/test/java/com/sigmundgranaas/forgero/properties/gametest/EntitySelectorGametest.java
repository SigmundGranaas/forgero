package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsHostileFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.ChainSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.ConeSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class EntitySelectorGametest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSingleTargetSelector(TestContext context) {
		SingleTargetSelector selector = new SingleTargetSelector(List.of());

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));

		List<Entity> selected = selector.select(source, target);

		context.assertTrue(selected.size() == 1, "Should select exactly 1 entity, but selected " + selected.size());
		context.assertTrue(selected.get(0) == target, "Should select the initial target");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAreaOfEffectSelector(TestContext context) {
		AreaOfEffectSelector selector = new AreaOfEffectSelector(3, List.of());

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));

		// Spawn entities within radius
		LivingEntity near1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 5));
		LivingEntity near2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));

		// Spawn entity outside radius
		LivingEntity far = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(10, 1, 10));

		List<Entity> selected = selector.select(source, target);

		context.assertTrue(selected.size() >= 2, "Should select at least 2 nearby entities (excluding source), but selected " + selected.size());
		context.assertTrue(selected.contains(near1), "Should include near1");
		context.assertTrue(selected.contains(near2), "Should include near2");
		context.assertFalse(selected.contains(far), "Should not include far entity");
		context.assertFalse(selected.contains(source), "Should not include source entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAreaOfEffectSelectorWithFilter(TestContext context) {
		// Test AOE selector with hostile filter
		AreaOfEffectSelector selector = new AreaOfEffectSelector(5, List.of(new IsHostileFilter()));

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));

		// Spawn hostile and peaceful entities within radius
		LivingEntity hostileNear = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 5));
		LivingEntity peacefulNear = context.spawnEntity(EntityType.VILLAGER, new BlockPos(6, 1, 5));

		List<Entity> selected = selector.select(source, target);

		context.assertTrue(selected.contains(hostileNear), "Should include hostile entity");
		context.assertFalse(selected.contains(peacefulNear), "Should not include peaceful entity due to filter");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConeSelector(TestContext context) {
		ConeSelector selector = new ConeSelector(90.0f, 10.0f, List.of()); // 90 degree cone, 10 blocks range

		// Source at origin, looking east (+X direction)
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 0));

		// Entities in front of source (within cone)
		LivingEntity inCone1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 1, 0));
		LivingEntity inCone2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 1));

		// Entity behind source (outside cone)
		LivingEntity behind = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(-3, 1, 0));

		// Entity too far (outside range)
		LivingEntity tooFar = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(15, 1, 0));

		List<Entity> selected = selector.select(source, target);

		// The cone selector should select entities in the direction the source is facing
		context.assertTrue(selected.size() >= 1, "Should select at least some entities in cone, but selected " + selected.size());
		context.assertFalse(selected.contains(behind), "Should not include entity behind source");
		context.assertFalse(selected.contains(tooFar), "Should not include entity too far away");
		context.assertFalse(selected.contains(source), "Should not include source entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testChainSelector(TestContext context) {
		ChainSelector selector = new ChainSelector(3, 5.0f, false, List.of()); // Max 3 chains, 5 block range

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		// Create a chain of entities
		LivingEntity chain1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 0));
		LivingEntity chain2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 0));
		LivingEntity chain3 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(8, 1, 0));

		// Entity too far from chain
		LivingEntity isolated = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(20, 1, 0));

		List<Entity> selected = selector.select(source, target);

		context.assertTrue(selected.size() >= 1, "Should select at least the initial target, but selected " + selected.size());
		context.assertTrue(selected.size() <= 4, "Should select at most 4 entities (initial + 3 chains), but selected " + selected.size());
		context.assertTrue(selected.get(0) == target, "First entity should be the initial target");
		context.assertFalse(selected.contains(source), "Should not include source entity");
		context.assertFalse(selected.contains(isolated), "Should not chain to isolated entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testChainSelectorWithRepeats(TestContext context) {
		ChainSelector selectorNoRepeats = new ChainSelector(5, 3.0f, false, List.of());
		ChainSelector selectorWithRepeats = new ChainSelector(5, 3.0f, true, List.of());

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		// Only spawn one additional entity close by
		LivingEntity nearby = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 0));

		List<Entity> selectedNoRepeats = selectorNoRepeats.select(source, target);
		List<Entity> selectedWithRepeats = selectorWithRepeats.select(source, target);

		// Without repeats, should stop after hitting each entity once (max 2 in this case)
		context.assertTrue(selectedNoRepeats.size() <= 2, "Without repeats should select at most 2 entities, but selected " + selectedNoRepeats.size());

		// With repeats allowed, could potentially chain back and forth more times
		context.assertTrue(selectedWithRepeats.size() >= selectedNoRepeats.size(), "With repeats should select same or more entities");
		context.complete();
	}
}
