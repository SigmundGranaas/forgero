package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.fabric.gametest.AttributeApplicationTest.createFloor;

import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.DisarmHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.FireHandler;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

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

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "DisarmHandlerTest", tickLimit = 120)
	public void testDisarmHandler(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		createFloor(context);

		// Spawn a player that will act as the source of the attack
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();

		// Spawn a target entity (e.g., a zombie) and give it an item to hold
		ZombieEntity target = context.spawnEntity(EntityType.ZOMBIE, playerPos.relative());
		target.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

		// Create an instance of DisarmHandler from JSON
		DisarmHandler disarmHandler = disarmHandler();

		// Trigger the onHit method simulating the player hitting the target
		disarmHandler.onHit(player, context.getWorld(), target);

		// After a brief delay, check if the target has been disarmed
		context.runAtTick(1, () -> {
			// Ensure the target's main hand is now empty
			context.assertTrue(target.getMainHandStack().isEmpty(), "Target entity's main hand is not empty after being disarmed.");

			// Check for a dropped item entity to ensure the item was dropped
			boolean itemDropped = context.getWorld().getEntitiesByClass(ItemEntity.class, new Box(TestPos.of(new BlockPos(0, 0, 0), context).absolute(), TestPos.of(new BlockPos(7, 7, 7), context).absolute()), entity -> entity.getStack().getItem() == Items.DIAMOND_SWORD).size() == 1;
			context.assertTrue(itemDropped, "The disarmed item was not dropped in the world.");

			context.complete();
		});
	}

	public static DisarmHandler disarmHandler() {
		String json = """
				{
				    "type": "forgero:disarm",
				    "target": "minecraft:targeted_entity"
				}
				""";
		return Utils.handlerFromString(json, DisarmHandler.BUILDER);
	}
}
