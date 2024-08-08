package com.sigmundgranaas.forgero.fabric.gametest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.ExplosionHandler;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class ExplosionHandlerTests {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "ExplosionHandlerTest", tickLimit = 120)
	public void testSimpleExplosion(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);
		TestPos targetPos = playerPos.offset(1, 0, 1);
		LivingEntity target = context.spawnEntity(EntityType.PIG, targetPos.relative());

		ExplosionHandler explosionHandler = createExplosionHandler(ExplosionHandler.SIMPLE_EXPLOSION_EXAMPLE);

		explosionHandler.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			verifyExplosion(context, targetPos.absolute().up(), 2.0f);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "ExplosionHandlerTest", tickLimit = 120)
	public void testPowerfulExplosion(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);
		TestPos targetPos = playerPos.offset(1, 0, 1);
		LivingEntity target = context.spawnEntity(EntityType.PIG, targetPos.relative());

		ExplosionHandler explosionHandler = createExplosionHandler(ExplosionHandler.POWERFUL_EXPLOSION_EXAMPLE);

		explosionHandler.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			verifyExplosion(context, targetPos.absolute().up(), 2.0f);
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "ExplosionHandlerTest", tickLimit = 120)
	public void testAttackerExplosion(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);
		TestPos targetPos = playerPos.offset(1, 0, 1);
		LivingEntity target = context.spawnEntity(EntityType.PIG, targetPos.relative());

		float initialPlayerHealth = player.getHealth();
		float initialTargetHealth = target.getHealth();


		ExplosionHandler explosionHandler = createExplosionHandler(ExplosionHandler.ATTACKER_EXPLOSION_EXAMPLE);

		explosionHandler.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			verifyExplosion(context, targetPos.absolute().up(), 2.0f);
			verifyEntityDamage(context, player, initialPlayerHealth);
			verifyEntityDamage(context, target, initialTargetHealth);
			context.complete();
		});
	}


	private void createFloor(TestContext context) {
		for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				context.setBlockState(x, 0, z, Blocks.STONE);
			}
		}
	}

	private ExplosionHandler createExplosionHandler(String jsonString) {
		JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
		return ExplosionHandler.fromJson(json);
	}

	private void verifyExplosion(TestContext context, BlockPos center, float power) {
		World world = context.getWorld();
		int radius = (int) Math.ceil(power);

		boolean explosionOccurred = false;
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = center.add(x, y, z);
					if (world.getBlockState(pos).isAir()) {
						explosionOccurred = true;
						break;
					}
				}
				if (explosionOccurred) break;
			}
			if (explosionOccurred) break;
		}

		context.assertTrue(explosionOccurred, "No explosion effects detected");
	}


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "ExplosionHandlerTest", tickLimit = 120)
	public void testNoExplosion(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);
		LivingEntity target = context.spawnEntity(EntityType.PIG, playerPos.offset(1, 0, 1).relative());

		float initialPlayerHealth = player.getHealth();
		float initialTargetHealth = target.getHealth();

		ExplosionHandler dummyHandler = new ExplosionHandler(0, "minecraft:targeted_entity") {
			@Override
			public void onHit(Entity source, World world, Entity targetEntity) {
				// Do nothing
			}
		};

		dummyHandler.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			verifyNoExplosion(context, target.getBlockPos(), 5.0f);
			verifyNoDamage(context, player, initialPlayerHealth);
			verifyNoDamage(context, target, initialTargetHealth);
			context.complete();
		});
	}

	private void verifyEntityDamage(TestContext context, LivingEntity entity, float initialHealth) {
		context.assertTrue(entity.getHealth() < initialHealth,
				"Did not take damage as expected. Initial health: " + initialHealth + ", Current health: " + entity.getHealth());
	}

	private void verifyNoDamage(TestContext context, LivingEntity entity, float initialHealth) {
		context.assertTrue(entity.getHealth() == initialHealth,
				"Took unexpected damage. Initial health: " + initialHealth + ", Current health: " + entity.getHealth());
	}

	private void verifyNoExplosion(TestContext context, BlockPos center, float power) {
		World world = context.getWorld();
		int radius = (int) Math.ceil(power);

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = center.add(x, y, z);
					context.assertFalse(world.getBlockState(pos).isAir(),
							"Unexpected air block found at " + pos + ". False positive explosion detected.");
				}
			}
		}
	}
}
