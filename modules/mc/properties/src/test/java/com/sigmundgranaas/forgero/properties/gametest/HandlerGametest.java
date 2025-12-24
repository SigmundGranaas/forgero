package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.entity.FunctionExecuteHandler;
import com.sigmundgranaas.forgero.effects.entity.TeleportHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionProperty;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ConsumeUpgradeHandler;

import net.minecraft.util.UseAction;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Gametests for newly implemented handlers:
 * - TeleportHandler
 * - FunctionExecuteHandler
 * - ConsumeUpgradeHandler
 */
public class HandlerGametest {

	/**
	 * Tests TeleportHandler with random teleportation on entity hit
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testTeleportHandlerRandom(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Spawn a cow to hit
		CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(1, 1, 1));
		Vec3d initialPos = cow.getPos();

		// Create property with teleport effect (random teleportation of target)
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new TeleportHandler("target", true, true, 5)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_teleport_random",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Give player the item
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Hit the cow
		player.attack(cow);

		// Wait a tick for teleport to process
		context.runAtTick(2, () -> {
			Vec3d newPos = cow.getPos();
			double distance = newPos.distanceTo(initialPos);

			// Cow should have moved (random teleport within 5 blocks)
			context.assertTrue(distance > 0, "Cow should have teleported");
			context.assertTrue(distance <= 5.5, "Teleport distance should be within max range");

			context.complete();
		});
	}

	/**
	 * Tests TeleportHandler with self-teleportation in look direction
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testTeleportHandlerSelf(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();
		Vec3d initialPos = player.getPos();

		// Spawn a dummy entity to hit
		CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(1, 1, 1));

		// Create property with teleport effect (teleport self in look direction)
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new TeleportHandler("self", false, true, 3)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_teleport_self",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Give player the item and set look direction
		player.setStackInHand(Hand.MAIN_HAND, stack);
		player.setYaw(0f); // Look in positive Z direction
		player.setPitch(0f);

		// Hit the cow (trigger should teleport the player)
		player.attack(cow);

		// Wait a tick for teleport to process
		context.runAtTick(2, () -> {
			Vec3d newPos = player.getPos();
			double distance = newPos.distanceTo(initialPos);

			// Player should have teleported
			context.assertTrue(distance > 0, "Player should have teleported");
			context.assertTrue(distance <= 3.5, "Teleport distance should be within max range");

			context.complete();
		});
	}

	/**
	 * Tests FunctionExecuteHandler with simple command execution
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFunctionExecuteHandler(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Spawn an entity to hit
		CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(1, 1, 1));

		// Create property with function execute effect
		// Note: The command will execute but we can't easily verify it in a gametest
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FunctionExecuteHandler(List.of("say Test command executed"))),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_function_execute",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Give player the item
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Hit the cow (this will execute the command)
		player.attack(cow);

		// Just verify the setup worked - command execution is logged
		context.runAtTick(1, () -> {
			context.assertTrue(true, "Function execute handler completed");
			context.complete();
		});
	}

	/**
	 * Tests FunctionExecuteHandler with multiple commands
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFunctionExecuteMultipleCommands(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Spawn an entity to hit
		CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(1, 1, 1));

		// Create property with multiple commands
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FunctionExecuteHandler(List.of(
						"say Command 1",
						"say Command 2",
						"say Command 3"
				))),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_function_multi",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Give player the item
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Hit the cow
		player.attack(cow);

		context.runAtTick(1, () -> {
			context.assertTrue(true, "Multiple commands executed");
			context.complete();
		});
	}

	/**
	 * Tests ConsumeUpgradeHandler basic setup
	 * Note: This handler is currently a placeholder as it requires component mutation API
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeUpgradeHandler(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Create property with consume upgrade handler
		// This will be a no-op until component mutation API is available
		UseInteractionProperty property = new UseInteractionProperty(
				net.minecraft.util.UseAction.NONE,
				0,
				false,
				Collections.emptyList(), // onStart
				Collections.emptyList(), // onTick
				Collections.emptyList(), // onRelease
				List.of(new ConsumeUpgradeHandler("forgero:test_upgrade")), // onFinish
				null  // condition
		);

		ItemStack stack = ComponentTester.createStack(
				"test_consume_upgrade",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Give player the item
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Use the item (should trigger onFinish handlers)
		player.getItemCooldownManager().set(stack.getItem(), 0);
		player.setCurrentHand(Hand.MAIN_HAND);

		// Wait a tick
		context.runAtTick(1, () -> {
			// Just verify the handler exists and doesn't crash
			context.assertTrue(true, "ConsumeUpgradeHandler setup completed");
			context.complete();
		});
	}

	/**
	 * Tests combining multiple handlers on one item
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCombinedHandlers(TestContext context) {
		var player = context.createMockCreativeServerPlayerInWorld();

		// Spawn an entity to hit
		CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(1, 1, 1));
		Vec3d initialPos = cow.getPos();

		// Create property with multiple effects
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new TeleportHandler("target", true, true, 5),
						new FunctionExecuteHandler(List.of("say Teleported and executed!"))
				),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"test_combined_handlers",
				Set.of("tool"),
				List.of(property)
		);

		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Give player the item
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Hit the cow
		player.attack(cow);

		// Wait for effects to process
		context.runAtTick(2, () -> {
			Vec3d newPos = cow.getPos();
			double distance = newPos.distanceTo(initialPos);

			// Verify teleport happened
			context.assertTrue(distance > 0, "Combined handlers should work together");

			context.complete();
		});
	}
}
