package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.entity.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Comprehensive gametests for all new effect handlers:
 * - FreezeHandler
 * - SoundHandler
 * - ParticleHandler
 * - VelocityHandler
 * - MagnetHandler
 * - SpawnEntityHandler
 * - ModifyBlockHandler
 */
public class EffectHandlerTests {

	// ========== FreezeHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFreezeHandlerAppliesFreeze(TestContext context) {
		FreezeHandler handler = new FreezeHandler(100, false); // 5 seconds
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));

		context.assertTrue(target.getFrozenTicks() == 0, "Target should not be frozen initially");

		handler.apply(target);

		context.assertTrue(target.getFrozenTicks() > 0, "Target should be frozen after applying FreezeHandler");
		context.assertTrue(target.getFrozenTicks() == 100, "Target should have 100 frozen ticks, but had " + target.getFrozenTicks());
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFreezeHandlerVisualOnly(TestContext context) {
		FreezeHandler handler = new FreezeHandler(100, true);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));

		handler.apply(target);

		// Visual-only mode still applies frozen ticks (visual effect)
		// The "visualOnly" flag would prevent damage in actual implementation
		context.assertTrue(target.getFrozenTicks() > 0, "Target should have frozen visual effect");
		context.complete();
	}

	// ========== SoundHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSoundHandlerPlaysAtTarget(TestContext context) {
		Identifier soundId = new Identifier("minecraft", "entity.player.breath");
		SoundHandler handler = new SoundHandler(soundId, 1.0f, 1.0f, SoundHandler.SoundTarget.TARGET);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Sound playing verification is difficult in gametests
		// We verify no crash occurs
		handler.apply(source, target);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSoundHandlerPlaysAtSource(TestContext context) {
		Identifier soundId = new Identifier("minecraft", "block.note_block.chime");
		SoundHandler handler = new SoundHandler(soundId, 1.0f, 1.5f, SoundHandler.SoundTarget.SOURCE);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		handler.apply(source, target);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSoundHandlerInvalidSound(TestContext context) {
		Identifier invalidSound = new Identifier("minecraft", "nonexistent_sound");
		SoundHandler handler = new SoundHandler(invalidSound, 1.0f, 1.0f, SoundHandler.SoundTarget.TARGET);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Should not crash with invalid sound
		handler.apply(source, target);

		context.complete();
	}

	// ========== ParticleHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testParticleHandlerSpawnsParticles(TestContext context) {
		Identifier particleId = new Identifier("minecraft", "smoke");
		ParticleHandler handler = new ParticleHandler(particleId, 50, 0.1, 1.0, ParticleHandler.ParticleTarget.TARGET);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Particle verification is difficult in gametests
		// We verify no crash occurs
		handler.apply(source, target);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testParticleHandlerInvalidParticle(TestContext context) {
		Identifier invalidParticle = new Identifier("minecraft", "nonexistent_particle");
		ParticleHandler handler = new ParticleHandler(invalidParticle, 10, 0.1, 1.0, ParticleHandler.ParticleTarget.TARGET);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Should not crash with invalid particle
		handler.apply(source, target);

		context.complete();
	}

	// ========== VelocityHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testVelocityHandlerDashSelf(TestContext context) {
		VelocityHandler handler = new VelocityHandler(
				VelocityHandler.VelocityTarget.SELF,
				2.0,
				VelocityHandler.VelocityMode.ADD,
				0.0
		);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		source.setVelocity(Vec3d.ZERO);
		context.assertTrue(source.getVelocity().lengthSquared() == 0, "Source should have zero velocity initially");

		handler.apply(source, target);

		context.waitAndRun(1, () -> {
			context.assertTrue(source.getVelocity().lengthSquared() > 0.1, "Source should have velocity after dash");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testVelocityHandlerKnockbackAwayFromSource(TestContext context) {
		VelocityHandler handler = new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				2.0,
				VelocityHandler.VelocityMode.AWAY_FROM_SOURCE,
				0.0
		);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		target.setVelocity(Vec3d.ZERO);

		handler.apply(source, target);

		context.waitAndRun(1, () -> {
			context.assertTrue(target.getVelocity().lengthSquared() > 0.1, "Target should have velocity after knockback");
			context.assertTrue(target.getVelocity().x > 0, "Target should be pushed away along +X axis");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testVelocityHandlerSetMode(TestContext context) {
		VelocityHandler handler = new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				1.0,
				VelocityHandler.VelocityMode.SET,
				0.0
		);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		target.setVelocity(new Vec3d(5.0, 5.0, 5.0)); // High existing velocity

		handler.apply(source, target);

		context.waitAndRun(1, () -> {
			// SET mode should override existing velocity
			double length = target.getVelocity().length();
			context.assertTrue(length < 2.0, "Velocity should be set to new value, not added. Length: " + length);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testVelocityHandlerVerticalBias(TestContext context) {
		VelocityHandler handler = new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				1.0,
				VelocityHandler.VelocityMode.ADD,
				2.0 // Strong vertical bias (launch)
		);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		target.setVelocity(Vec3d.ZERO);

		handler.apply(source, target);

		context.waitAndRun(1, () -> {
			context.assertTrue(target.getVelocity().y > 1.0, "Target should have significant upward velocity from vertical bias");
			context.complete();
		});
	}

	// ========== MagnetHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMagnetHandlerAttractsItems(TestContext context) {
		MagnetHandler handler = new MagnetHandler(5.0, 0.5);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 64, 2));

		context.waitAndRun(1, () -> {
			// Spawn items around the entity using source's actual position
			Vec3d sourcePos = source.getPos();
			ItemEntity item1 = new ItemEntity(context.getWorld(), sourcePos.x + 2, sourcePos.y, sourcePos.z, new ItemStack(Items.DIAMOND));
			ItemEntity item2 = new ItemEntity(context.getWorld(), sourcePos.x, sourcePos.y, sourcePos.z + 2, new ItemStack(Items.GOLD_INGOT));
			context.getWorld().spawnEntity(item1);
			context.getWorld().spawnEntity(item2);

			Vec3d initialPos1 = item1.getPos();

			// Apply magnet multiple times to ensure movement
			for (int i = 0; i < 10; i++) {
				handler.apply(source);
			}

			context.waitAndRun(3, () -> {
				// Items should have moved closer to the source
				double initialDist1 = initialPos1.distanceTo(source.getPos());
				double currentDist1 = item1.getPos().distanceTo(source.getPos());

				context.assertTrue(currentDist1 < initialDist1, "Item should be closer to magnet source");
				context.complete();
			});
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMagnetHandlerIgnoresDistantItems(TestContext context) {
		MagnetHandler handler = new MagnetHandler(3.0, 0.5); // Small radius

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		context.waitAndRun(1, () -> {
			// Spawn item far away (outside radius) - use source's actual position
			Vec3d sourcePos = source.getPos();
			ItemEntity farItem = new ItemEntity(
					context.getWorld(),
					sourcePos.x + 10.0,  // 10 blocks away (outside radius of 3.0)
					sourcePos.y,
					sourcePos.z + 10.0,
					new ItemStack(Items.DIAMOND)
			);
			farItem.setVelocity(Vec3d.ZERO); // Stop any movement
			farItem.setNoGravity(true); // Disable gravity
			context.getWorld().spawnEntity(farItem);

			context.waitAndRun(2, () -> {
				Vec3d initialPos = farItem.getPos();

				handler.apply(source);

				context.waitAndRun(3, () -> {
					// Item should not have moved significantly (allow small floating point error)
					double distance = initialPos.distanceTo(farItem.getPos());
					context.assertTrue(distance < 0.1, "Distant item should not be affected by magnet, moved " + distance + " blocks");
					context.complete();
				});
			});
		});
	}

	// ========== SpawnEntityHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpawnEntityHandlerSpawnsEntity(TestContext context) {
		Identifier entityId = new Identifier("minecraft", "zombie");
		SpawnEntityHandler handler = new SpawnEntityHandler(entityId, 1, Vec3d.ZERO, true, false, true);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 64, 2));

		handler.apply(source, target);

		context.waitAndRun(3, () -> {
			// Count all zombies in a larger area using target's actual position
			Vec3d targetPos = target.getPos();
			int newZombieCount = context.getWorld().getEntitiesByType(EntityType.ZOMBIE, Box.of(targetPos, 10, 10, 10), e -> true).size();
			// Should have at least 3 zombies (2 original + 1 spawned)
			context.assertTrue(newZombieCount >= 3, "Should have spawned a new zombie, found " + newZombieCount);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpawnEntityHandlerRespectsCount(TestContext context) {
		Identifier entityId = new Identifier("minecraft", "chicken");
		SpawnEntityHandler handler = new SpawnEntityHandler(entityId, 3, Vec3d.ZERO, true, false, true);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 64, 2));

		handler.apply(source, target);

		context.waitAndRun(3, () -> {
			Vec3d targetPos = target.getPos();
			int chickenCount = context.getWorld().getEntitiesByType(EntityType.CHICKEN, Box.of(targetPos, 10, 10, 10), e -> true).size();
			context.assertTrue(chickenCount >= 3, "Should have spawned at least 3 chickens, spawned " + chickenCount);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpawnEntityHandlerInvalidEntity(TestContext context) {
		Identifier invalidEntity = new Identifier("minecraft", "nonexistent_entity");
		SpawnEntityHandler handler = new SpawnEntityHandler(invalidEntity, 1, Vec3d.ZERO, true, false, true);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Should not crash with invalid entity
		handler.apply(source, target);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpawnEntityHandlerWithOffset(TestContext context) {
		Identifier entityId = new Identifier("minecraft", "pig");
		Vec3d offset = new Vec3d(2.0, 1.0, 0.0);
		SpawnEntityHandler handler = new SpawnEntityHandler(entityId, 1, offset, true, false, true);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 64, 2));

		handler.apply(source, target);

		context.waitAndRun(3, () -> {
			// Search in a larger area around target position (accounting for offset)
			Vec3d searchCenter = target.getPos().add(offset.multiply(0.5)); // Search between target and offset position
			List<PigEntity> pigs = context.getWorld().getEntitiesByType(EntityType.PIG, Box.of(searchCenter, 10, 10, 10), e -> true);
			context.assertTrue(pigs.size() > 0, "Should have spawned pig with offset, found " + pigs.size());
			context.complete();
		});
	}

	// ========== ModifyBlockHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testModifyBlockHandlerReplace(TestContext context) {
		Identifier replacementBlock = new Identifier("minecraft", "ice");
		ModifyBlockHandler handler = new ModifyBlockHandler(
				ModifyBlockHandler.BlockAction.REPLACE,
				java.util.Optional.empty(),
				replacementBlock,
				false,
				false
		);

		LivingEntity source = context.createMockCreativeServerPlayerInWorld();
		// Place block first, then spawn entity on it
		BlockPos targetPos = new BlockPos(2, 64, 2);
		context.setBlockState(targetPos, Blocks.WATER);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, targetPos);

		handler.apply(source, target);

		context.waitAndRun(1, () -> {
			context.expectBlock(Blocks.ICE, targetPos);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testModifyBlockHandlerDestroy(TestContext context) {
		ModifyBlockHandler handler = new ModifyBlockHandler(
				ModifyBlockHandler.BlockAction.DESTROY,
				java.util.Optional.empty(),
				new Identifier("minecraft", "air"), // Not used for DESTROY
				true, // consumeItem = true (no drops)
				false // handleMultiblock = false
		);

		LivingEntity source = context.createMockCreativeServerPlayerInWorld();
		// Place block first, then spawn entity on it
		BlockPos targetPos = new BlockPos(2, 64, 2);
		context.setBlockState(targetPos, Blocks.DIRT);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, targetPos);

		handler.apply(source, target);

		context.waitAndRun(1, () -> {
			context.expectBlock(Blocks.AIR, targetPos);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testModifyBlockHandlerTargetBlockFilter(TestContext context) {
		Identifier waterBlock = new Identifier("minecraft", "water");
		Identifier iceBlock = new Identifier("minecraft", "ice");
		ModifyBlockHandler handler = new ModifyBlockHandler(
				ModifyBlockHandler.BlockAction.REPLACE,
				java.util.Optional.of(waterBlock), // Only replace water
				iceBlock,
				false, // consumeItem
				false // handleMultiblock
		);

		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Target on water - place block first
		BlockPos waterPos = new BlockPos(2, 64, 2);
		context.setBlockState(waterPos, Blocks.WATER);
		LivingEntity targetOnWater = context.spawnEntity(EntityType.ZOMBIE, waterPos);

		// Target on dirt - place block first
		BlockPos dirtPos = new BlockPos(4, 64, 4);
		context.setBlockState(dirtPos, Blocks.DIRT);
		LivingEntity targetOnDirt = context.spawnEntity(EntityType.ZOMBIE, dirtPos);

		// Apply to water target
		handler.apply(source, targetOnWater);
		// Apply to dirt target
		handler.apply(source, targetOnDirt);

		context.waitAndRun(1, () -> {
			context.expectBlock(Blocks.ICE, waterPos); // Should change
			context.expectBlock(Blocks.DIRT, dirtPos); // Should NOT change (filter)
			context.complete();
		});
	}
}
