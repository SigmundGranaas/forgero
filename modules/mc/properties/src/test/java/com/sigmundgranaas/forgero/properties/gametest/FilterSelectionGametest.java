package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.AndFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityTypeFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.HasEffectFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsBurningFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsHostileFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.NotFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.OrFilter;

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
}
