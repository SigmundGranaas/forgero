package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.entity.FunctionExecuteHandler;
import com.sigmundgranaas.forgero.effects.entity.TeleportHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;

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
 * Tests for handler functionality.
 * Focus: Do handlers actually trigger and apply their effects?
 * - TeleportHandler (random and directed teleportation)
 * - Combined handlers working together
 */
public class HandlerGametest {

	/**
	 * USE CASE: OnHit effect can teleport target entity randomly within range.
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
	 * USE CASE: OnHit effect can teleport player in look direction.
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
	 * USE CASE: Multiple handlers on same item all trigger correctly.
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
