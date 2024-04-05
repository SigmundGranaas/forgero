package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.fabric.gametest.AttributeApplicationTest.createFloor;

import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.FireHandler;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class HitHandlerTests {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "FireHandlerTest", tickLimit = 120)
	public void testFireHandlerOnEntity(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();


		PigEntity target = context.spawnEntity(EntityType.PIG, playerPos.offset(new BlockPos(1, 0, 1)).relative());

		// Apply FireHandler effects on the target entity
		FireHandler fireHandler = fireHandler();
		fireHandler.onHit(player, context.getWorld(), target);

		// After 1 tick, check if the target entity is on fire
		context.runAtTick(1, () -> {
			// Verify that the target is on fire
			context.assertTrue(target.getFireTicks() > 1, "Target entity is not on fire.");

			context.assertTrue(target.getHealth() < target.getMaxHealth(), "Target entity has not taken any damage.");

			// Verify the target is on fire for the expected duration
			context.assertTrue(target.getFireTicks() == (fireHandler.duration() * 20) - 1, "Target entity is not on fire for the expected duration.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "FireHandlerTest", tickLimit = 120)
	public void testFireHandlerOnBlock(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		// Place a burnable block next to the player
		TestPos targetBlockPos = TestPos.of(new BlockPos(1, 2, 1), context);
		context.setBlockState(targetBlockPos.relative(), Blocks.OAK_PLANKS);

		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();


		FireHandler fireHandler = fireBlockHandler();
		fireHandler.onHit(player, context.getWorld(), targetBlockPos.absolute());

		// After 1 tick, check if the block at targetBlockPos is set on fire
		context.runAtTick(1, () -> {
			BlockState stateAtTarget = context.getWorld().getBlockState(targetBlockPos.absolute().up());
			// Verify the block is replaced with fire
			context.assertTrue(stateAtTarget.getBlock() == Blocks.FIRE, "Target block is not set on fire as expected.");

			context.complete();
		});
	}

	public static FireHandler fireHandler() {
		String handler = """
				{
					"type": "minecraft:fire",
				    "duration": 5
				    }
								
				""";
		return Utils.handlerFromString(handler, FireHandler.BUILDER);
	}

	public static FireHandler fireBlockHandler() {
		String handler = """
				{
				        "type": "minecraft:fire",
				        "duration": 5
				    }
				   
				""";
		return Utils.handlerFromString(handler, FireHandler.BUILDER);
	}
}
