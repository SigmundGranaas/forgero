package com.sigmundgranaas.forgero.fabric.gametest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.handler.targeted.ExplosionHandler;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import static com.sigmundgranaas.forgero.fabric.gametest.AttributeApplicationTest.createFloor;

public class ExplosionHandlerTests {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "ExplosionHandlerTest", tickLimit = 120)
	public void testSimpleExplosion(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);
		TestPos targetPos = playerPos.offset(1, 0, 1);
		LivingEntity target = context.spawnEntity(EntityType.PIG, targetPos.relative());

		ExplosionHandler explosionHandler = createExplosionHandler(ExplosionHandler.SIMPLE_EXPLOSION_EXAMPLE);

		context.setBlockState(targetPos.relative().up(), Blocks.STONE.getDefaultState());

		explosionHandler.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			verifyExplosion(context, targetPos.absolute().down());
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
		context.setBlockState(targetPos.relative().up(), Blocks.STONE.getDefaultState());


		context.runAtTick(20, () -> {
			explosionHandler.onHit(player, context.getWorld(), target);
		});

		context.runAtTick(100, () -> {
			verifyExplosion(context, targetPos.absolute().down());
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "ExplosionHandlerTest", tickLimit = 120)
	public void testAttackerExplosion(TestContext context) {
		createFloor(context);
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		ServerPlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.gameMode(GameMode.SURVIVAL)
				.build()
				.createPlayer();

		for (int i = 0; i < 100; i++) {
			player.tick();
		}

		TestPos targetPos = playerPos.offset(1, 0, 1);

		LivingEntity target = context.spawnEntity(EntityType.PIG, targetPos.relative());

		float initialPlayerHealth = player.getHealth();
		float initialTargetHealth = target.getHealth();

		context.setBlockState(targetPos.relative().up(), Blocks.STONE.getDefaultState());

		ExplosionHandler explosionHandler = createExplosionHandler(ExplosionHandler.ATTACKER_EXPLOSION_EXAMPLE);

		context.waitAndRun(100, () -> explosionHandler.onHit(player, context.getWorld(), target));

		context.waitAndRun(110, () -> {
			verifyExplosion(context, targetPos.absolute().up());
			verifyEntityDamage(context, player, initialPlayerHealth);
			verifyEntityDamage(context, target, initialTargetHealth);
			context.complete();
		});
	}


	private ExplosionHandler createExplosionHandler(String jsonString) {
		JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
		return ExplosionHandler.fromJson(json);
	}

	private void verifyExplosion(TestContext context, BlockPos pos) {
		World world = context.getWorld();
		context.assertTrue(world.getBlockState(pos).isAir(), "No explosion effects detected");
	}

	private void verifyEntityDamage(TestContext context, LivingEntity entity, float initialHealth) {
		context.assertTrue(entity.getHealth() < initialHealth,
				"Did not take damage as expected. Initial health: " + initialHealth + ", Current health: " + entity.getHealth());
	}
}
