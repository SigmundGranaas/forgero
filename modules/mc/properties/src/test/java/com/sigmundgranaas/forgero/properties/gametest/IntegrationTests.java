package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.block.PlacedBlockSelector;
import com.sigmundgranaas.forgero.effects.block.RadiusBlockSelector;
import com.sigmundgranaas.forgero.effects.entity.*;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityStateFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EnvironmentFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;

/**
 * Integration tests for combinations of handlers, filters, selectors, and events.
 * Tests edge cases and complex interactions between systems.
 */
public class IntegrationTests {

	// ========== Handler Combinations ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMultipleHandlersChained(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		// Non-flammable target: an undead mob exposed to daylight catches fire, and fire
		// zeroes frozen ticks, intermittently failing the freeze assertion below.
		LivingEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));

		// Apply multiple handlers in sequence
		FreezeHandler freezeHandler = new FreezeHandler(100, false);
		VelocityHandler velocityHandler = new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				1.5,
				VelocityHandler.VelocityMode.ADD,
				1.0
		);
		SoundHandler soundHandler = new SoundHandler(
				new Identifier("minecraft", "entity.player.breath"),
				1.0f,
				1.0f,
				SoundHandler.SoundTarget.TARGET
		);

		freezeHandler.apply(target);
		velocityHandler.apply(source, target);
		soundHandler.apply(source, target);

		context.waitAndRun(2, () -> {
			context.assertTrue(target.getFrozenTicks() > 0, "Target should be frozen");
			context.assertTrue(target.getVelocity().lengthSquared() > 0, "Target should have velocity");
			// Sound played (no crash)
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAOEWithMultipleEffects(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Spawn multiple entities around source. Use non-flammable pigs: a daylight-exposed
		// undead mob can randomly ignite, and igniting zeroes frozen ticks, intermittently
		// failing the freeze assertions below.
		LivingEntity target1 = context.spawnEntity(EntityType.PIG, new BlockPos(3, 1, 2));
		LivingEntity target2 = context.spawnEntity(EntityType.PIG, new BlockPos(1, 1, 2));
		LivingEntity target3 = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 3));

		// AOE selector
		AreaOfEffectSelector selector = new AreaOfEffectSelector(3, List.of());
		List<Entity> selected = selector.select(source, target1);

		// Apply freeze to all selected
		FreezeHandler freezeHandler = new FreezeHandler(80, false);
		for (Entity entity : selected) {
			if (entity instanceof LivingEntity living) {
				freezeHandler.apply(living);
			}
		}

		context.waitAndRun(1, () -> {
			context.assertTrue(target1.getFrozenTicks() > 0, "Target1 should be frozen");
			context.assertTrue(target2.getFrozenTicks() > 0, "Target2 should be frozen");
			context.assertTrue(target3.getFrozenTicks() > 0, "Target3 should be frozen");
			context.complete();
		});
	}

	// ========== Filter + Handler Combinations ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFilteredEntitySelection(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Create low-health and full-health targets. Pigs (non-flammable) so a random
		// daylight ignition can't zero the frozen ticks asserted below.
		LivingEntity lowHealthTarget = context.spawnEntity(EntityType.PIG, new BlockPos(3, 1, 2));
		LivingEntity fullHealthTarget = context.spawnEntity(EntityType.PIG, new BlockPos(4, 1, 2));

		lowHealthTarget.setHealth(lowHealthTarget.getMaxHealth() * 0.3f);

		// Selector with health filter
		EntityStateFilter lowHealthFilter = new EntityStateFilter(
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.of(0.5)
		);

		AreaOfEffectSelector selector = new AreaOfEffectSelector(5, List.of(lowHealthFilter));
		List<Entity> selected = selector.select(source, lowHealthTarget);

		// Apply effect only to filtered entities
		FreezeHandler freezeHandler = new FreezeHandler(100, false);
		for (Entity entity : selected) {
			if (entity instanceof LivingEntity living) {
				freezeHandler.apply(living);
			}
		}

		context.waitAndRun(1, () -> {
			context.assertTrue(lowHealthTarget.getFrozenTicks() > 0, "Low-health target should be frozen");
			context.assertTrue(fullHealthTarget.getFrozenTicks() == 0, "Full-health target should not be frozen");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEnvironmentFilterWithEffects(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));

		// Create COMPLETELY enclosed dark room (3x3x3)
		BlockPos darkCenter = new BlockPos(2, 1, 2);

		// Floor
		for (int x = 1; x <= 3; x++) {
			for (int z = 1; z <= 3; z++) {
				context.setBlockState(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}

		// Walls
		for (int y = 1; y <= 2; y++) {
			for (int x = 1; x <= 3; x++) {
				context.setBlockState(new BlockPos(x, y, 1), Blocks.STONE);
				context.setBlockState(new BlockPos(x, y, 3), Blocks.STONE);
			}
			for (int z = 1; z <= 3; z++) {
				context.setBlockState(new BlockPos(1, y, z), Blocks.STONE);
				context.setBlockState(new BlockPos(3, y, z), Blocks.STONE);
			}
		}

		// Ceiling
		for (int x = 1; x <= 3; x++) {
			for (int z = 1; z <= 3; z++) {
				context.setBlockState(new BlockPos(x, 3, z), Blocks.STONE);
			}
		}

		LivingEntity targetInDark = context.spawnEntity(EntityType.ZOMBIE, darkCenter);
		LivingEntity targetInLight = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));

		context.waitAndRun(5, () -> {
			// Dark filter
			EnvironmentFilter darkFilter = new EnvironmentFilter(
					Optional.empty(),
					Optional.of(0),
					Optional.of(7),
					EnvironmentFilter.LightSourceType.COMBINED,
					Optional.empty()
			);

			// Test filter and apply effect conditionally
			if (darkFilter.test(source, targetInDark)) {
				FreezeHandler freezeHandler = new FreezeHandler(100, false);
				freezeHandler.apply(targetInDark);
			}

			if (darkFilter.test(source, targetInLight)) {
				FreezeHandler freezeHandler = new FreezeHandler(100, false);
				freezeHandler.apply(targetInLight);
			}

			context.waitAndRun(1, () -> {
				context.assertTrue(targetInDark.getFrozenTicks() > 0, "Dark target should be frozen");
				context.assertTrue(targetInLight.getFrozenTicks() == 0, "Lit target should not be frozen");
				context.complete();
			});
		});
	}

	// ========== Block Selectors ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPlacedBlockSelector(TestContext context) {
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos placedPos = new BlockPos(2, 1, 2);

		PlacedBlockSelector selector = new PlacedBlockSelector();
		List<BlockPos> selected = selector.select(player, placedPos, Blocks.STONE.getDefaultState());

		context.assertTrue(selected.size() == 1, "Should select exactly 1 position");
		context.assertTrue(selected.get(0).equals(placedPos), "Should select the placed position");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testRadiusBlockSelector(TestContext context) {
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos centerPos = new BlockPos(5, 1, 5);

		RadiusBlockSelector selector = new RadiusBlockSelector(2, true);
		List<BlockPos> selected = selector.select(player, centerPos, Blocks.STONE.getDefaultState());

		// Radius 2 cube: (2*2+1)^3 = 5^3 = 125 blocks
		context.assertTrue(selected.size() == 125, "Should select 125 blocks in radius 2 cube, selected " + selected.size());
		context.assertTrue(selected.contains(centerPos), "Should include center position");
		context.assertTrue(selected.contains(centerPos.add(2, 0, 0)), "Should include position at edge");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testRadiusBlockSelectorExcludeCenter(TestContext context) {
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos centerPos = new BlockPos(5, 1, 5);

		RadiusBlockSelector selector = new RadiusBlockSelector(1, false);
		List<BlockPos> selected = selector.select(player, centerPos, Blocks.STONE.getDefaultState());

		// Radius 1 cube: 3^3 = 27 blocks, minus center = 26 blocks
		context.assertTrue(selected.size() == 26, "Should select 26 blocks (exclude center), selected " + selected.size());
		context.assertFalse(selected.contains(centerPos), "Should not include center position");
		context.complete();
	}

	// ========== Edge Cases ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testVelocityHandlerOnStationaryEntity(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		target.setVelocity(Vec3d.ZERO);

		// Apply velocity multiple times
		VelocityHandler handler = new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				0.5,
				VelocityHandler.VelocityMode.ADD,
				0.0
		);

		for (int i = 0; i < 5; i++) {
			handler.apply(source, target);
		}

		context.waitAndRun(1, () -> {
			// Velocity should accumulate with ADD mode
			context.assertTrue(target.getVelocity().lengthSquared() > 1.0, "Velocity should have accumulated");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMagnetHandlerWithNoItems(TestContext context) {
		MagnetHandler handler = new MagnetHandler(5.0, 0.5);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// No items in area - should not crash
		handler.apply(source);

		context.waitAndRun(1, () -> {
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpawnEntityHandlerMaxCount(TestContext context) {
		Identifier entityId = new Identifier("minecraft", "chicken");
		// Test with large count
		SpawnEntityHandler handler = new SpawnEntityHandler(entityId, 10, Vec3d.ZERO, true, false, true);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		handler.apply(source, target);

		context.waitAndRun(5, () -> {
			// Should have spawned 10 chickens (or close to it)
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testModifyBlockHandlerOnAir(TestContext context) {
		ModifyBlockHandler handler = new ModifyBlockHandler(
				ModifyBlockHandler.BlockAction.REPLACE,
				Optional.empty(),
				new Identifier("minecraft", "grass_block"),
				false,
				false
		);

		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Use RELATIVE coordinates - place a dirt block first
		BlockPos dirtPosRelative = new BlockPos(2, 1, 2);
		context.setBlockState(dirtPosRelative, Blocks.DIRT);

		// Spawn target on the dirt block
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, dirtPosRelative);

		// Verify it's dirt using RELATIVE position
		context.expectBlock(Blocks.DIRT, dirtPosRelative);

		// Apply handler to replace dirt with grass
		handler.apply(source, target);

		context.waitAndRun(1, () -> {
			// Dirt should be replaced with grass block (use RELATIVE position)
			context.expectBlock(Blocks.GRASS_BLOCK, dirtPosRelative);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFreezeHandlerDurationVariations(TestContext context) {
		// Pigs (non-flammable): a daylight-exposed undead mob can randomly ignite, and
		// igniting zeroes frozen ticks, which would fail the duration assertions below.
		LivingEntity target1 = context.spawnEntity(EntityType.PIG, new BlockPos(1, 1, 1));
		LivingEntity target2 = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));
		LivingEntity target3 = context.spawnEntity(EntityType.PIG, new BlockPos(3, 1, 3));

		FreezeHandler shortFreeze = new FreezeHandler(20, false); // 1 second
		FreezeHandler mediumFreeze = new FreezeHandler(100, false); // 5 seconds
		FreezeHandler longFreeze = new FreezeHandler(200, false); // 10 seconds

		shortFreeze.apply(target1);
		mediumFreeze.apply(target2);
		longFreeze.apply(target3);

		context.waitAndRun(1, () -> {
			context.assertTrue(target1.getFrozenTicks() >= 18 && target1.getFrozenTicks() <= 20, "Short freeze: " + target1.getFrozenTicks());
			context.assertTrue(target2.getFrozenTicks() >= 98 && target2.getFrozenTicks() <= 100, "Medium freeze: " + target2.getFrozenTicks());
			context.assertTrue(target3.getFrozenTicks() >= 198 && target3.getFrozenTicks() <= 200, "Long freeze: " + target3.getFrozenTicks());
			context.complete();
		});
	}

	// ========== Complex Scenarios ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCombatScenarioWithMultipleEffects(TestContext context) {
		// Scenario: Player attacks enemy, triggering freeze + knockback + particles
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		// Non-flammable pig: a daylight-exposed undead mob can randomly ignite, and igniting
		// zeroes frozen ticks, intermittently failing the freeze assertion below.
		LivingEntity enemy = context.spawnEntity(EntityType.PIG, new BlockPos(3, 1, 3));

		enemy.setVelocity(Vec3d.ZERO);

		// Simulate combat effects
		FreezeHandler freeze = new FreezeHandler(60, false);
		VelocityHandler knockback = new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				2.0,
				VelocityHandler.VelocityMode.AWAY_FROM_SOURCE,
				0.5
		);
		ParticleHandler particles = new ParticleHandler(
				new Identifier("minecraft", "crit"),
				30,
				0.2,
				0.5,
				ParticleHandler.ParticleTarget.TARGET
		);

		freeze.apply(enemy);
		knockback.apply(player, enemy);
		particles.apply(player, enemy);

		context.waitAndRun(2, () -> {
			context.assertTrue(enemy.getFrozenTicks() > 0, "Enemy should be frozen");
			context.assertTrue(enemy.getVelocity().lengthSquared() > 0.5, "Enemy should be knocked back");
			// Particles spawned (no crash)
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMagnetWithVelocityInteraction(TestContext context) {
		// Test interaction between magnet pulling items and velocity effects
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));

		// Spawn items
		ItemEntity item = new ItemEntity(context.getWorld(), 8.0, 1.5, 5.0, new ItemStack(Items.DIAMOND));
		context.getWorld().spawnEntity(item);

		// Give item initial velocity away from source
		item.setVelocity(new Vec3d(1.0, 0, 0));

		MagnetHandler magnet = new MagnetHandler(6.0, 1.0);

		// Apply magnet multiple times
		for (int i = 0; i < 10; i++) {
			magnet.apply(source);
		}

		Vec3d initialPos = item.getPos();

		context.waitAndRun(10, () -> {
			// Item should have been pulled toward source despite initial velocity
			double initialDist = initialPos.distanceTo(source.getPos());
			double currentDist = item.getPos().distanceTo(source.getPos());

			context.assertTrue(currentDist < initialDist, "Item should be pulled closer despite initial velocity away");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAllNewHandlersInSequence(TestContext context) {
		// Test all 7 new handlers in one scenario
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Use RELATIVE coordinates - place dirt at y=0 (ground level)
		BlockPos dirtPosRelative = new BlockPos(3, 0, 3);
		context.setBlockState(dirtPosRelative, Blocks.DIRT);

		// Spawn target ABOVE the dirt block at y=1, so its feet are in air above dirt.
		// Non-flammable pig: a daylight-exposed undead mob can randomly ignite, and igniting
		// zeroes frozen ticks, which would fail the "Freeze worked" assertion below.
		BlockPos spawnPosRelative = new BlockPos(3, 1, 3);
		LivingEntity target = context.spawnEntity(EntityType.PIG, spawnPosRelative);

		// 1. Freeze
		new FreezeHandler(80, false).apply(target);

		// 2. Sound
		new SoundHandler(
				new Identifier("minecraft", "entity.player.breath"),
				1.0f, 1.0f, SoundHandler.SoundTarget.TARGET
		).apply(player, target);

		// 3. Particle
		new ParticleHandler(
				new Identifier("minecraft", "smoke"),
				20, 0.1, 1.0, ParticleHandler.ParticleTarget.TARGET
		).apply(player, target);

		// 4. Velocity - small amount so target stays nearby
		new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				0.2, VelocityHandler.VelocityMode.ADD, 0.1
		).apply(player, target);

		context.waitAndRun(2, () -> {
			// 5. Magnet (spawn item using target's actual position)
			Vec3d targetPos = target.getPos();
			ItemEntity item = new ItemEntity(context.getWorld(), targetPos.x + 1, targetPos.y, targetPos.z, new ItemStack(Items.GOLD_INGOT));
			context.getWorld().spawnEntity(item);
			new MagnetHandler(3.0, 0.5).apply(target);

			// 6. Spawn Entity
			new SpawnEntityHandler(
					new Identifier("minecraft", "chicken"),
					1, Vec3d.ZERO, true, false, true
			).apply(player, target);

			// 7. Modify Block - spawn a zombie on top of the dirt, it will have getBlockPos() at y=1
			//    But we want to modify the dirt at y=0, so we need to target differently
			//    ModifyBlockHandler modifies the block at target.getBlockPos(), not below it
			//    Let's place a new dirt block where the new zombie will stand
			BlockPos modifyBlockPos = new BlockPos(5, 1, 3);
			context.setBlockState(modifyBlockPos, Blocks.DIRT);
			// Spawn zombie INSIDE the dirt block so getBlockPos returns the dirt position
			LivingEntity blockModTarget = context.spawnEntity(EntityType.ZOMBIE, modifyBlockPos);

			new ModifyBlockHandler(
					ModifyBlockHandler.BlockAction.REPLACE,
					Optional.empty(), // No filter - replace any block
					new Identifier("minecraft", "grass_block"),
					false,
					false
			).apply(player, blockModTarget);

			context.waitAndRun(2, () -> {
				// All effects applied successfully
				context.assertTrue(target.getFrozenTicks() > 0, "Freeze worked");
				// ModifyBlockHandler should have replaced the dirt at (5,1,3) with grass
				context.expectBlock(Blocks.GRASS_BLOCK, modifyBlockPos);
				context.expectEntity(EntityType.CHICKEN);
				context.complete();
			});
		});
	}
}
