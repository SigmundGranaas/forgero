package com.sigmundgranaas.forgero.fabric.gametest;

import com.sigmundgranaas.forgero.minecraft.common.handler.entity.TeleportHandler;

import com.sigmundgranaas.forgero.testutil.PlayerFactory;

import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.math.Vec3d;

import org.junit.jupiter.api.Assertions;

import static com.sigmundgranaas.forgero.fabric.gametest.AttributeApplicationTest.createFloor;
import static net.fabricmc.fabric.api.gametest.v1.FabricGameTest.EMPTY_STRUCTURE;

public class TeleportHandlerTest {
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testRandomTeleport(TestContext context) {
		createFloor(context);
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();
		Vec3d initialPos = player.getPos();

		TeleportHandler teleportHandler = new TeleportHandler(true, true, 5, "minecraft:self");
		teleportHandler.handle(player);

		context.runAtTick(1, () -> {
			Vec3d newPos = player.getPos();
			Assertions.assertNotEquals(initialPos, newPos, "Player should have teleported");
			Assertions.assertTrue(newPos.distanceTo(initialPos) <= 5, "Teleportation distance should be within 5 blocks");
			Assertions.assertEquals(initialPos.y, newPos.y, 0.01, "Player should be on the ground");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testLookDirectionTeleport(TestContext context) {
		createFloor(context);
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();
		player.setYaw(0); // Look towards positive Z
		player.setPitch(0);
		Vec3d initialPos = player.getPos();

		TeleportHandler teleportHandler = new TeleportHandler(false, true, 5, "minecraft:self");
		teleportHandler.handle(player);

		context.runAtTick(1, () -> {
			Vec3d newPos = player.getPos();
			Assertions.assertNotEquals(initialPos, newPos, "Player should have teleported");
			Assertions.assertTrue(newPos.z > initialPos.z, "Player should have teleported in positive Z direction");
			Assertions.assertEquals(initialPos.y, newPos.y, 0.01, "Player should be on the ground");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testTargetedEntityTeleport(TestContext context) {
		createFloor(context);
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();
		BlockPos initialPlayerPos = player.getBlockPos();

		BlockPos initialPigPos = player.getBlockPos().north(2);
		EntityType.PIG.spawn(context.getWorld(), null, null, initialPigPos, null, false, false);

		TeleportHandler teleportHandler = new TeleportHandler(true, true, 5, "minecraft:targeted_entity");
		teleportHandler.onHit(player, context.getWorld(), context.getWorld().getEntitiesByType(EntityType.PIG, player.getBoundingBox().expand(5), e -> true).get(0));

		context.runAtTick(1, () -> {
			BlockPos newPigPos = context.getWorld().getEntitiesByType(EntityType.PIG, player.getBoundingBox().expand(10), e -> true).get(0).getBlockPos();
			Assertions.assertNotEquals(initialPlayerPos.north(2), newPigPos, "Pig should have teleported");
			Assertions.assertTrue(initialPigPos.toCenterPos().distanceTo(newPigPos.toCenterPos()) <= 7, "Teleportation distance should be within 5 blocks");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testAirTeleport(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();

		Vec3d initialPos = player.getPos();
		TeleportHandler teleportHandler = new TeleportHandler(true, false, 5, "minecraft:self");
		teleportHandler.handle(player);

		context.runAtTick(1, () -> {
			Vec3d newPos = player.getPos();
			Assertions.assertNotEquals(initialPos, newPos, "Player should have moved");
			context.complete();
		});
	}
}
