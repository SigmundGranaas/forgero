package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.AndFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.DistanceFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityTypeFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.HasEffectFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.HasTagFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.HealthThresholdFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsAirborneFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsBurningFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsHostileFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsPlayerFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.NotFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.OrFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.RandomChanceFilter;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Proves entity filters actually narrow a REAL selection — asserting both who is hit and who is
 * spared in one real AOE selection — instead of calling {@code filter.test()} on a fabricated entity
 * (the gap the audit flagged for the whole filter suite). Membership is asserted for specific spawned
 * entities, so the tests are robust to other entities sharing the gametest world.
 */
public class FilterSelectionGametest implements FabricGameTest {

	private static ServerPlayerEntity sourceAt(TestContext context, int x, int y, int z) {
		ServerPlayerEntity source = context.createMockCreativeServerPlayerInWorld();
		source.refreshPositionAndAngles(context.getAbsolutePos(new BlockPos(x, y, z)), 0.0f, 0.0f);
		return source;
	}

	/** entity_type filter selects the matching type and spares the non-matching one. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void entity_type_filter_selects_matching_and_spares_others(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 2));

		List<Entity> selected = SelectionTestHelper.selectWithFilter(
				source, zombie, new EntityTypeFilter("minecraft:zombie"), 5);

		context.assertTrue(selected.contains(zombie), "entity_type=zombie must SELECT the zombie");
		context.assertFalse(selected.contains(cow), "entity_type=zombie must SPARE the cow");
		context.complete();
	}

	/** has_effect filter selects the afflicted entity and spares the un-afflicted one. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void has_effect_filter_selects_afflicted_and_spares_others(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity afflicted = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity healthy = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
		afflicted.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 0));

		List<Entity> selected = SelectionTestHelper.selectWithFilter(
				source, afflicted, new HasEffectFilter("minecraft:slowness"), 5);

		context.assertTrue(selected.contains(afflicted), "has_effect=slowness must SELECT the afflicted zombie");
		context.assertFalse(selected.contains(healthy), "has_effect=slowness must SPARE the un-afflicted zombie");
		context.complete();
	}

	/** is_burning filter selects the entity on fire and spares the calm one. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void is_burning_filter_selects_burning_and_spares_calm(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity burning = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity calm = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
		burning.setOnFireFor(5);
		calm.setFireTicks(0);

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, burning, new IsBurningFilter(), 5);

		context.assertTrue(selected.contains(burning), "is_burning must SELECT the burning zombie");
		context.assertFalse(selected.contains(calm), "is_burning must SPARE the calm zombie");
		context.complete();
	}

	/** is_hostile filter selects hostile mobs and spares passive ones. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void is_hostile_filter_selects_mobs_and_spares_passive(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 2));

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, zombie, new IsHostileFilter(), 5);

		context.assertTrue(selected.contains(zombie), "is_hostile must SELECT the zombie");
		context.assertFalse(selected.contains(cow), "is_hostile must SPARE the cow");
		context.complete();
	}

	/** NOT(entity_type=zombie) inverts: selects the non-zombie, spares the zombie. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void not_filter_inverts_the_selection(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 2));

		List<Entity> selected = SelectionTestHelper.selectWithFilter(
				source, zombie, new NotFilter(new EntityTypeFilter("minecraft:zombie")), 5);

		context.assertTrue(selected.contains(cow), "NOT(zombie) must SELECT the cow");
		context.assertFalse(selected.contains(zombie), "NOT(zombie) must SPARE the zombie");
		context.complete();
	}

	/** AND requires every sub-filter: only the hostile AND burning entity is selected. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void and_filter_requires_all_subfilters(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity burningZombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity calmZombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
		LivingEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(4, 2, 2));
		burningZombie.setOnFireFor(5);
		calmZombie.setFireTicks(0);

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, burningZombie,
				new AndFilter(List.of(new IsHostileFilter(), new IsBurningFilter())), 6);

		context.assertTrue(selected.contains(burningZombie), "AND(hostile,burning) must SELECT the burning zombie");
		context.assertFalse(selected.contains(calmZombie), "AND(hostile,burning) must SPARE the non-burning zombie");
		context.assertFalse(selected.contains(cow), "AND(hostile,burning) must SPARE the (non-hostile) cow");
		context.complete();
	}

	/** OR requires any sub-filter: the hostile entity matches; a neither-matching one is spared. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void or_filter_requires_any_subfilter(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 2));
		cow.setFireTicks(0);

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, zombie,
				new OrFilter(List.of(new IsHostileFilter(), new IsBurningFilter())), 5);

		context.assertTrue(selected.contains(zombie), "OR(hostile,burning) must SELECT the hostile zombie");
		context.assertFalse(selected.contains(cow), "OR(hostile,burning) must SPARE the calm, passive cow");
		context.complete();
	}

	/** distance filter selects entities within range of the source and spares far ones. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void distance_filter_selects_near_and_spares_far(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity near = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2)); // ~1.4 from source
		LivingEntity far = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 8));  // ~7 from source

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, near, new DistanceFilter(0.0f, 3.0f), 12);

		context.assertTrue(selected.contains(near), "distance<=3 must SELECT the near zombie");
		context.assertFalse(selected.contains(far), "distance<=3 must SPARE the far zombie");
		context.complete();
	}

	/** is_player filter selects players and spares mobs. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void is_player_filter_selects_player_and_spares_mob(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		ServerPlayerEntity player = sourceAt(context, 2, 2, 2);
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 2));

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, player, new IsPlayerFilter(), 5);

		context.assertTrue(selected.contains(player), "is_player must SELECT the player");
		context.assertFalse(selected.contains(zombie), "is_player must SPARE the zombie");
		context.complete();
	}

	/** is_airborne filter selects entities not on the ground and spares grounded ones. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void is_airborne_filter_selects_airborne_and_spares_grounded(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity airborne = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity grounded = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
		airborne.setOnGround(false);
		grounded.setOnGround(true);

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, airborne, new IsAirborneFilter(), 5);

		context.assertTrue(selected.contains(airborne), "is_airborne must SELECT the airborne zombie");
		context.assertFalse(selected.contains(grounded), "is_airborne must SPARE the grounded zombie");
		context.complete();
	}

	/** has_tag filter selects entities whose type is in the tag and spares others. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void has_tag_filter_selects_tagged_type_and_spares_others(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity skeleton = context.spawnEntity(EntityType.SKELETON, new BlockPos(2, 2, 2));
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 2));

		List<Entity> selected = SelectionTestHelper.selectWithFilter(
				source, skeleton, new HasTagFilter("minecraft:skeletons"), 5);

		context.assertTrue(selected.contains(skeleton), "has_tag=minecraft:skeletons must SELECT the skeleton");
		context.assertFalse(selected.contains(zombie), "has_tag=minecraft:skeletons must SPARE the zombie");
		context.complete();
	}

	/** health_threshold filter selects entities below the ratio and spares healthy ones. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void health_threshold_filter_selects_wounded_and_spares_healthy(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity wounded = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
		LivingEntity healthy = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
		wounded.setHealth(wounded.getMaxHealth() * 0.25f);  // 25% < 50%
		healthy.setHealth(healthy.getMaxHealth());           // 100% > 50%

		List<Entity> selected = SelectionTestHelper.selectWithFilter(source, wounded,
				new HealthThresholdFilter(0.5f, HealthThresholdFilter.Comparator.LESS_THAN), 5);

		context.assertTrue(selected.contains(wounded), "health<50% must SELECT the wounded zombie");
		context.assertFalse(selected.contains(healthy), "health<50% must SPARE the full-health zombie");
		context.complete();
	}

	/** random_chance is deterministic at the extremes: chance 1.0 selects all, chance 0.0 selects none. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void random_chance_filter_extremes_are_deterministic(TestContext context) {
		ServerPlayerEntity source = sourceAt(context, 1, 2, 1);
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 2, 2));

		List<Entity> always = SelectionTestHelper.selectWithFilter(source, zombie, new RandomChanceFilter(1.0f), 5);
		context.assertTrue(always.contains(zombie), "random_chance=1.0 must always SELECT");

		List<Entity> never = SelectionTestHelper.selectWithFilter(source, zombie, new RandomChanceFilter(0.0f), 5);
		context.assertFalse(never.contains(zombie), "random_chance=0.0 must never SELECT");
		context.complete();
	}
}
