package com.sigmundgranaas.forgero.fabric.gametest;

import com.sigmundgranaas.forgero.minecraft.common.handler.entity.TeleportHandler;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

		context.runAtTick(2, () -> { // Wait 2 ticks for physics to settle
			Vec3d newPos = player.getPos();
			Assertions.assertNotEquals(initialPos, newPos, "Player should have teleported");
			Assertions.assertTrue(newPos.distanceTo(initialPos) <= 7, "Teleportation distance should be within a reasonable range of max_distance");

			BlockPos blockBelowPlayer = player.getBlockPos().down();
			Assertions.assertFalse(context.getWorld().getBlockState(blockBelowPlayer).isAir(), "Player should be standing on a solid block after teleporting on ground.");
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

		context.runAtTick(2, () -> {
			Vec3d newPos = player.getPos();
			Assertions.assertNotEquals(initialPos, newPos, "Player should have teleported");
			Assertions.assertTrue(newPos.z > initialPos.z, "Player should have teleported in positive Z direction");

			BlockPos blockBelowPlayer = player.getBlockPos().down();
			Assertions.assertFalse(context.getWorld().getBlockState(blockBelowPlayer).isAir(), "Player should be standing on a solid block.");
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

		BlockPos initialPigPos = playerPos.absolute().north(4);
		LivingEntity pig = context.spawnEntity(EntityType.PIG, playerPos.relative().north(4));

		TeleportHandler teleportHandler = new TeleportHandler(true, true, 4, "minecraft:targeted_entity");
		teleportHandler.onHit(player, context.getWorld(), pig);
		Vec3d newPigPos = pig.getPos();
		context.assertTrue(Vec3d.ofCenter(initialPigPos).distanceTo(newPigPos) <= 6, "Teleportation distance should be within a reasonable range.");

		BlockPos blockBelowPig = pig.getBlockPos().down();
		context.assertFalse(context.getWorld().getBlockState(blockBelowPig).isAir(), "Pig should be standing on a solid block.");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testAirTeleport(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 5, 3), context);
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();

		Vec3d initialPos = player.getPos();
		TeleportHandler teleportHandler = new TeleportHandler(true, false, 5, "minecraft:self");
		teleportHandler.handle(player);

		context.runAtTick(2, () -> {
			Vec3d newPos = player.getPos();
			Assertions.assertNotEquals(initialPos, newPos, "Player should have moved");
			Assertions.assertTrue(initialPos.distanceTo(newPos) > 0.1, "Player should have moved a significant distance");
			context.complete();
		});
	}
}
