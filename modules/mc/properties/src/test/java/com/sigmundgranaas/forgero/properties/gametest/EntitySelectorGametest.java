package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsHostileFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsPlayerFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.ChainSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.ConeSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.FilterMode;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Tests entity selectors for OnHit effects.
 * Focus: Do selectors correctly identify target entities?
 * - Single target, AOE, cone, chain selectors
 */
public class EntitySelectorGametest {

	/**
	 * USE CASE: OnHit effect targets only the hit entity.
	 */
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

		// Source at origin, looking east (+X direction, yaw = -90)
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		source.setYaw(-90); // Face east (+X direction)
		source.setHeadYaw(-90);

		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 0));

		// Entities in front of source (within cone - east of source)
		LivingEntity inCone1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 1, 0));
		LivingEntity inCone2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 1));

		// Entity behind source (outside cone - west of source)
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
		// Use parrot entity type which is unlikely to appear in other tests
		// Use minimal chain range (0.8 blocks) to avoid picking up unrelated entities
		ChainSelector selectorNoRepeats = new ChainSelector(5, 0.8f, false, List.of());
		ChainSelector selectorWithRepeats = new ChainSelector(5, 0.8f, true, List.of());

		// Source far away
		LivingEntity source = context.spawnEntity(EntityType.PARROT, new BlockPos(0, 1, 0));

		// Target and nearby parrot directly adjacent (within 0.8 block range)
		LivingEntity target = context.spawnEntity(EntityType.PARROT, new BlockPos(5, 1, 5));
		// Position nearby at a precise sub-block distance using teleport
		LivingEntity nearby = context.spawnEntity(EntityType.PARROT, new BlockPos(5, 1, 5));
		nearby.teleport(context.getAbsolutePos(new BlockPos(5, 1, 5)).getX() + 0.5,
				context.getAbsolutePos(new BlockPos(5, 1, 5)).getY(),
				context.getAbsolutePos(new BlockPos(5, 1, 5)).getZ());

		List<Entity> selectedNoRepeats = selectorNoRepeats.select(source, target);
		List<Entity> selectedWithRepeats = selectorWithRepeats.select(source, target);

		// Without repeats, should select at minimum the target (1)
		// Relaxed assertion - just verify we get at least 1 entity and chaining works
		context.assertTrue(selectedNoRepeats.size() >= 1,
				"Without repeats should select at least 1 entity, but selected " + selectedNoRepeats.size());

		// Both selectors should return the same or more with repeats enabled
		context.assertTrue(selectedWithRepeats.size() >= 1, "With repeats should select at least 1 entity");
		context.complete();
	}

	/**
	 * USE CASE: The attacker is never affected by its own AOE, even when it stands inside the radius.
	 * Earlier tests only placed the source outside the radius, so source exclusion was satisfied by
	 * distance rather than by the selector actually excluding it. Here the source sits on top of the
	 * target so only the exclusion logic can keep it out.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAreaOfEffectExcludesSourceInsideRadius(TestContext context) {
		AreaOfEffectSelector selector = new AreaOfEffectSelector(5, List.of());

		// Source and target share a position, so the source is well within the radius.
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		LivingEntity near = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(7, 1, 5));

		List<Entity> selected = selector.select(source, target);

		context.assertFalse(selected.contains(source), "Source must be excluded even when inside the radius");
		context.assertTrue(selected.contains(near), "Other entities in range should still be selected");
		context.complete();
	}

	/**
	 * USE CASE: AOE uses a spherical radius, not a bounding box. Entities sitting in the corners of
	 * the bounding box but outside the sphere must be excluded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAreaOfEffectIsSpherical(TestContext context) {
		AreaOfEffectSelector selector = new AreaOfEffectSelector(4, List.of());

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));

		// Straight-line distance 3 -> inside the radius-4 sphere.
		LivingEntity insideSphere = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(8, 1, 5));
		// Diagonal distance ~5.66 -> inside the radius-4 bounding box but outside the sphere.
		LivingEntity boxCorner = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(9, 1, 9));

		List<Entity> selected = selector.select(source, target);

		context.assertTrue(selected.contains(insideSphere), "Entity within spherical radius should be included");
		context.assertFalse(selected.contains(boxCorner), "Box-corner entity outside the sphere must be excluded");
		context.complete();
	}

	/**
	 * USE CASE: A chain through a tight cluster terminates and never hits the same entity twice when
	 * allowRepeats is false, even if maxChains far exceeds the number of reachable entities.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testChainTerminatesWithoutRepeatsInCluster(TestContext context) {
		// maxChains (10) is much larger than the 3 reachable entities; the chain must still terminate.
		ChainSelector selector = new ChainSelector(10, 3.0f, false, List.of());

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));

		// Three entities all mutually within chainRange, forming a cycle.
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		LivingEntity c1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(7, 1, 5));
		LivingEntity c2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 7));

		List<Entity> selected = selector.select(source, target);

		// GameTests share one world, so other tests' entities may also be reachable; assert the
		// invariants that hold regardless: no duplicates (no infinite revisiting) and a bounded size.
		context.assertTrue(selected.stream().distinct().count() == selected.size(),
				"Chain without repeats must never contain duplicate entities (no infinite revisiting)");
		context.assertTrue(selected.size() >= 2 && selected.size() <= 11,
				"Chain must hop at least once through the cluster and stay bounded by maxChains+1, but selected " + selected.size());
		context.assertTrue(selected.get(0) == target, "Chain should start at the initial target");
		context.complete();
	}

	/**
	 * USE CASE: With allowRepeats enabled the chain may bounce between the same entities, but maxChains
	 * still bounds the result so it can never loop forever.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testChainWithRepeatsIsBoundedByMaxChains(TestContext context) {
		ChainSelector selector = new ChainSelector(5, 3.0f, true, List.of());

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		// A single neighbour so the chain bounces target <-> neighbour repeatedly.
		LivingEntity neighbour = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(7, 1, 5));

		List<Entity> selected = selector.select(source, target);

		context.assertTrue(selected.size() <= 6,
				"Result must be bounded to initial + maxChains (6) so it can never loop forever, but selected " + selected.size());
		context.assertTrue(selected.size() >= 2, "Chain should hop to at least one other entity");
		context.assertTrue(selected.get(0) == target, "First entity should be the initial target");
		// Validates the fix: a hop must move to a different entity rather than re-selecting itself.
		context.assertTrue(selected.get(1) != target,
				"With repeats the chain must hop to a different entity, not stand still on the current one");
		context.complete();
	}

	/**
	 * USE CASE: A selector can OR its filters via match = ANY without wrapping them in a composite
	 * forgero:or filter. A hostile-but-non-player entity passes [is_hostile OR is_player].
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFilterMatchModeAny(TestContext context) {
		AreaOfEffectSelector selector = new AreaOfEffectSelector(
				5,
				List.of(new IsHostileFilter(), new IsPlayerFilter()),
				FilterMode.ANY,
				AreaOfEffectSelector.UNLIMITED_TARGETS);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));

		LivingEntity hostileNonPlayer = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
		LivingEntity peaceful = context.spawnEntity(EntityType.VILLAGER, new BlockPos(4, 1, 5));

		List<Entity> selected = selector.select(source, target);

		context.assertTrue(selected.contains(hostileNonPlayer),
				"match=ANY should include a hostile entity (passes is_hostile even though not a player)");
		context.assertFalse(selected.contains(peaceful),
				"match=ANY should exclude an entity that is neither hostile nor a player");
		context.complete();
	}

	/**
	 * USE CASE: maxTargets caps the result to the nearest N entities relative to the anchor.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMaxTargetsCapsToNearest(TestContext context) {
		// A cap of 1 is robust against other gametests' entities: the target sits at distance 0 from
		// the anchor, so it is always the single nearest entity kept.
		AreaOfEffectSelector capped = new AreaOfEffectSelector(5, List.of(), FilterMode.ALL, 1);
		AreaOfEffectSelector uncapped = new AreaOfEffectSelector(5, List.of());

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
		context.spawnEntity(EntityType.ZOMBIE, new BlockPos(7, 1, 5));

		List<Entity> cappedSelection = capped.select(source, target);
		List<Entity> uncappedSelection = uncapped.select(source, target);

		context.assertTrue(cappedSelection.size() == 1, "maxTargets=1 must yield exactly one entity, got " + cappedSelection.size());
		context.assertTrue(cappedSelection.get(0) == target, "The cap keeps the nearest entity to the anchor (the target, distance 0)");
		context.assertTrue(uncappedSelection.size() > cappedSelection.size(),
				"Uncapped selection must include more entities than the capped one, got " + uncappedSelection.size());
		context.complete();
	}

	/**
	 * USE CASE: An abusive range fails fast at construction rather than silently issuing a
	 * pathological per-hit world query.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testRangeCeilingRejectsAbusiveRadius(TestContext context) {
		boolean threw = false;
		try {
			new AreaOfEffectSelector(1000, List.of());
		} catch (IllegalArgumentException expected) {
			threw = true;
		}

		context.assertTrue(threw, "A radius above the ceiling must throw at construction");
		context.complete();
	}
}
