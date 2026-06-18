package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.effects.entity.FireHandler;
import com.sigmundgranaas.forgero.effects.zone.CreateZoneHandler;
import com.sigmundgranaas.forgero.effects.zone.OwnerMode;
import com.sigmundgranaas.forgero.effects.zone.ZoneManager;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Phase 3: persistent zones. A zone re-applies its effects to entities inside its radius each
 * interval until it expires.
 */
public class ZoneGametest {

	/**
	 * Spatial selection is asserted directly (robust to the shared gametest world): only entities
	 * inside the radius are selected; foreign entities can only add, never remove our references.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testZoneSelectsOnlyEntitiesInside(TestContext context) {
		LivingEntity owner = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity center = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		LivingEntity inside = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
		LivingEntity outside = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(14, 1, 14));

		ZoneManager.Zone zone = new ZoneManager.Zone(
				context.getWorld().getRegistryKey().getValue().toString(),
				center.getPos(),
				4.0,
				context.getWorld().getTime() + 40,
				1,
				List.of(),
				List.of(),
				owner.getUuid(),
				OwnerMode.KEEP_OWNER);

		List<Entity> targets = zone.selectTargets(context.getWorld());
		context.assertTrue(targets.contains(inside), "Zone should select the entity inside its radius");
		context.assertFalse(targets.contains(outside), "Zone should not select the entity outside its radius");
		context.complete();
	}

	/**
	 * Integration: the world ticker drives a fire field and ignites an entity standing in it.
	 * Only the positive (inside is ignited) is asserted, since other tests sharing the world could
	 * independently ignite a distant entity.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testZoneTickerAppliesEffect(TestContext context) {
		LivingEntity owner = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity center = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		LivingEntity inside = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));

		new CreateZoneHandler(4, 40, 1, List.of(new FireHandler(2)), List.of(), OwnerMode.KEEP_OWNER)
				.apply(owner, center);

		context.waitAndRun(5, () -> {
			context.assertTrue(inside.isOnFire(), "Entity inside the fire zone should be ignited by the ticker");
			ZoneManager.clearAll();
			context.complete();
		});
	}
}
