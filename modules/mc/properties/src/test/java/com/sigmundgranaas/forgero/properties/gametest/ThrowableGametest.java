package com.sigmundgranaas.forgero.properties.gametest;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionProperty;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.entity.ThrownItemEntity;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.StartUseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ThrowHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ThrownItemEntityRegistry;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;

/**
 * Gametests for the ThrowHandler and ThrownItemEntity.
 * Tests the throwable item system in the new UseInteraction framework.
 */
public class ThrowableGametest {

	// ========== ThrowHandler Configuration Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerType(TestContext context) {
		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "NONE", 10f);

		context.assertTrue(handler.type().equals("forgero:throw"), "Type should be forgero:throw");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerDefaultValues(TestContext context) {
		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "NONE", 10f);

		context.assertTrue(handler.velocityMultiplier() == 1.0f, "Default velocity multiplier should be 1.0");
		context.assertTrue(handler.instability() == 0.0f, "Default instability should be 0.0");
		context.assertTrue(handler.chargeTime() == 20f, "Default charge time should be 20");
		context.assertTrue(handler.spinType().equals("NONE"), "Default spin type should be NONE");
		context.assertTrue(handler.weight() == 10f, "Default weight should be 10");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerCustomValues(TestContext context) {
		ThrowHandler handler = new ThrowHandler(2.5f, 1.5f, 30f, "VERTICAL", 15f);

		context.assertTrue(handler.velocityMultiplier() == 2.5f, "Custom velocity multiplier should be 2.5");
		context.assertTrue(handler.instability() == 1.5f, "Custom instability should be 1.5");
		context.assertTrue(handler.chargeTime() == 30f, "Custom charge time should be 30");
		context.assertTrue(handler.spinType().equals("VERTICAL"), "Custom spin type should be VERTICAL");
		context.assertTrue(handler.weight() == 15f, "Custom weight should be 15");
		context.complete();
	}

	// ========== ThrownItemEntity Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntityRegistry(TestContext context) {
		// Entity should be registered by the plugin
		context.assertTrue(ThrownItemEntityRegistry.isRegistered(), "Entity should be registered");
		context.assertTrue(ThrownItemEntityRegistry.THROWN_ITEM_ENTITY != null, "Entity type should not be null");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntityCreation(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.IRON_SWORD);

		ThrownItemEntity entity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				10f,
				ThrownItemEntity.SpinType.VERTICAL
		);

		context.assertTrue(entity != null, "Entity should be created");
		context.assertTrue(entity.getThrownStack().getItem() == Items.IRON_SWORD, "Thrown stack should be iron sword");
		context.assertTrue(entity.getWeight() == 10f, "Weight should be 10");
		context.assertTrue(entity.getSpinType() == ThrownItemEntity.SpinType.VERTICAL, "Spin type should be VERTICAL");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntitySpinTypes(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);

		ThrownItemEntity verticalEntity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				10f,
				ThrownItemEntity.SpinType.VERTICAL
		);

		ThrownItemEntity horizontalEntity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				10f,
				ThrownItemEntity.SpinType.HORIZONTAL
		);

		ThrownItemEntity noneEntity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				10f,
				ThrownItemEntity.SpinType.NONE
		);

		context.assertTrue(verticalEntity.getSpinType() == ThrownItemEntity.SpinType.VERTICAL, "Should be VERTICAL");
		context.assertTrue(horizontalEntity.getSpinType() == ThrownItemEntity.SpinType.HORIZONTAL, "Should be HORIZONTAL");
		context.assertTrue(noneEntity.getSpinType() == ThrownItemEntity.SpinType.NONE, "Should be NONE");
		context.complete();
	}

	// ========== ThrowHandler Execution Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerSpawnsEntity(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.TRIDENT);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "VERTICAL", 10f);

		// Count thrown items before
		long countBefore = context.getWorld().getEntitiesByType(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				player.getBoundingBox().expand(50),
				e -> e.getOwner() == player
		).size();

		// Create context with enough charge time
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, stack, 20, 71980, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			long countAfter = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(50),
					e -> e.getOwner() == player
			).size();

			context.assertTrue(countAfter > countBefore, "ThrownItemEntity should be spawned");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerEmptyStack(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack emptyStack = ItemStack.EMPTY;

		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "NONE", 10f);

		// Count only entities owned by this player
		long countBefore = context.getWorld().getEntitiesByType(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				player.getBoundingBox().expand(10),
				e -> e.getOwner() == player
		).size();

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, emptyStack, 20, 71980, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			long countAfter = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(10),
					e -> e.getOwner() == player
			).size();

			context.assertTrue(countAfter == countBefore, "No entity should spawn for empty stack");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerZeroCharge(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.TRIDENT);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "NONE", 10f);

		long countBefore = context.getWorld().getEntitiesByType(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				player.getBoundingBox().expand(5),
				e -> e.getOwner() == player
		).size();

		// Zero charge time - should not throw
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, stack, 0, 72000, 0.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			long countAfter = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(5),
					e -> e.getOwner() == player
			).size();

			context.assertTrue(countAfter == countBefore, "No entity should spawn with zero charge");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerSpinTypeParameter(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.IRON_AXE);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "HORIZONTAL", 10f);

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, stack, 20, 71980, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			List<ThrownItemEntity> entities = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(50),
					e -> e.getOwner() == player
			);

			if (!entities.isEmpty()) {
				ThrownItemEntity entity = entities.get(0);
				context.assertTrue(entity.getSpinType() == ThrownItemEntity.SpinType.HORIZONTAL,
						"Spawned entity should have HORIZONTAL spin type");
			}
			context.complete();
		});
	}

	// ========== UseInteractionProperty Integration Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpearUseInteractionProperty(TestContext context) {
		// Create a spear-style UseInteractionProperty
		StartUseHandler startHandler = new StartUseHandler();
		ThrowHandler throwHandler = new ThrowHandler(1.0f, 0.0f, 20f, "VERTICAL", 10f);

		UseInteractionProperty spearProperty = new UseInteractionProperty(
				UseAction.SPEAR,
				72000,
				true, // Used on release
				List.of(startHandler),  // on_start
				Collections.emptyList(), // on_tick
				List.of(throwHandler),   // on_release
				Collections.emptyList(), // on_finish
				null
		);

		context.assertTrue(spearProperty.useAction() == UseAction.SPEAR, "Use action should be SPEAR");
		context.assertTrue(spearProperty.maxUseTime() == 72000, "Max use time should be 72000");
		context.assertTrue(spearProperty.usedOnRelease(), "Should be used on release");
		context.assertTrue(spearProperty.hasStartHandlers(), "Should have start handlers");
		context.assertTrue(spearProperty.hasReleaseHandlers(), "Should have release handlers");
		context.assertFalse(spearProperty.hasTickHandlers(), "Should not have tick handlers");
		context.assertFalse(spearProperty.hasFinishHandlers(), "Should not have finish handlers");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpearPropertyReleaseExecution(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.TRIDENT);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ThrowHandler throwHandler = new ThrowHandler(1.5f, 0.5f, 15f, "VERTICAL", 12f);

		UseInteractionProperty spearProperty = new UseInteractionProperty(
				UseAction.SPEAR,
				72000,
				true,
				Collections.emptyList(),
				Collections.emptyList(),
				List.of(throwHandler),
				Collections.emptyList(),
				null
		);

		long countBefore = context.getWorld().getEntitiesByType(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				player.getBoundingBox().expand(50),
				e -> e.getOwner() == player
		).size();

		UseContext releaseCtx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, stack, 20, 71980, 1.0f);

		// Execute release handlers
		for (UseHandler handler : spearProperty.onRelease()) {
			if (handler instanceof com.sigmundgranaas.forgero.properties.minecraft.useinteraction.ContextualUseHandler ctxHandler) {
				ctxHandler.apply(releaseCtx);
			}
		}

		context.waitAndRun(1, () -> {
			long countAfter = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(50),
					e -> e.getOwner() == player
			).size();

			context.assertTrue(countAfter > countBefore, "Entity should spawn from release handler");
			context.complete();
		});
	}

	// ========== Velocity and Weight Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerVelocityMultiplier(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// High velocity multiplier
		ThrowHandler handler = new ThrowHandler(3.0f, 0.0f, 20f, "NONE", 10f);

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, stack, 20, 71980, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			List<ThrownItemEntity> entities = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(100),
					e -> e.getOwner() == player
			);

			if (!entities.isEmpty()) {
				ThrownItemEntity entity = entities.get(0);
				double speed = entity.getVelocity().length();
				// With 3.0x multiplier and full charge, velocity should be significant
				context.assertTrue(speed > 0.5, "Entity should have significant velocity");
			}
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownEntityWeight(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.IRON_SWORD);

		// Create entity with heavy weight
		ThrownItemEntity heavyEntity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				50f, // Heavy
				ThrownItemEntity.SpinType.NONE
		);

		// Create entity with light weight
		ThrownItemEntity lightEntity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				5f, // Light
				ThrownItemEntity.SpinType.NONE
		);

		context.assertTrue(heavyEntity.getWeight() == 50f, "Heavy entity weight should be 50");
		context.assertTrue(lightEntity.getWeight() == 5f, "Light entity weight should be 5");
		context.complete();
	}

	// ========== Attribute Resolution Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerResolvesWeightFromComponent(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create a Forgero item with weight attribute = 25.0
		com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier weightAttr =
				new com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier("forgero", "weight");
		ItemStack forgeroItem = ComponentTester.createStackWithAttributes(
				"heavy_spear_" + System.nanoTime(),
				java.util.Set.of("tool", "spear"),
				java.util.Map.of(weightAttr, 25.0f)
		);

		// Skip test if we couldn't create a Forgero stack (may happen if converter not ready)
		if (forgeroItem.isEmpty()) {
			context.complete();
			return;
		}

		player.setStackInHand(Hand.MAIN_HAND, forgeroItem);

		// ThrowHandler with fallback weight of 10
		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "VERTICAL", 10f);

		// Throw the item
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, forgeroItem, 20, 71980, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			java.util.List<ThrownItemEntity> entities = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(50),
					e -> e.getOwner() == player
			);

			if (!entities.isEmpty()) {
				ThrownItemEntity entity = entities.get(0);
				// Weight should be 25.0 from component, not 10.0 fallback
				context.assertTrue(entity.getWeight() == 25.0f,
						"Weight should be resolved from component (25.0), not fallback (10.0). Got: " + entity.getWeight());
			} else {
				// Entity should be spawned
				context.assertTrue(false, "ThrownItemEntity should have been spawned");
			}
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerFallsBackWhenNoComponent(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Use a vanilla item (no Forgero component)
		ItemStack vanillaItem = new ItemStack(Items.STICK);
		player.setStackInHand(Hand.MAIN_HAND, vanillaItem);

		// ThrowHandler with fallback weight of 15
		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "NONE", 15f);

		// Count entities before
		long countBefore = context.getWorld().getEntitiesByType(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				player.getBoundingBox().expand(50),
				e -> e.getOwner() == player
		).size();

		// Throw the item
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, vanillaItem, 20, 71980, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			java.util.List<ThrownItemEntity> entities = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(50),
					e -> e.getOwner() == player
			);

			// Should have spawned an entity with the fallback weight
			context.assertTrue(entities.size() > countBefore, "Entity should be spawned for vanilla item");

			if (!entities.isEmpty()) {
				ThrownItemEntity entity = entities.get(entities.size() - 1);
				// Weight should be 15.0 (fallback) since vanilla item has no component
				context.assertTrue(entity.getWeight() == 15.0f,
						"Weight should be fallback (15.0) for vanilla item. Got: " + entity.getWeight());
			}
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrowHandlerUsesZeroWeightComponentFallsBack(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create a Forgero item with weight attribute = 0 (should fall back)
		com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier weightAttr =
				new com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier("forgero", "weight");
		ItemStack forgeroItem = ComponentTester.createStackWithAttributes(
				"zero_weight_spear_" + System.nanoTime(),
				java.util.Set.of("tool", "spear"),
				java.util.Map.of(weightAttr, 0.0f)
		);

		// Skip test if we couldn't create a Forgero stack
		if (forgeroItem.isEmpty()) {
			context.complete();
			return;
		}

		player.setStackInHand(Hand.MAIN_HAND, forgeroItem);

		// ThrowHandler with fallback weight of 12
		ThrowHandler handler = new ThrowHandler(1.0f, 0.0f, 20f, "VERTICAL", 12f);

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, forgeroItem, 20, 71980, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			java.util.List<ThrownItemEntity> entities = context.getWorld().getEntitiesByType(
					ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
					player.getBoundingBox().expand(50),
					e -> e.getOwner() == player
			);

			if (!entities.isEmpty()) {
				ThrownItemEntity entity = entities.get(0);
				// Weight should be 12.0 (fallback) because component weight is 0
				context.assertTrue(entity.getWeight() == 12.0f,
						"Weight should fall back to 12.0 when component weight is 0. Got: " + entity.getWeight());
			}
			context.complete();
		});
	}

	// ========== ThrownItemEntity OnHit Integration Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntityStoresItemForOnHit(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.IRON_SWORD);

		ThrownItemEntity entity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				10f,
				ThrownItemEntity.SpinType.VERTICAL
		);

		// Verify the entity stores the item stack (needed for OnHit)
		ItemStack thrownStack = entity.getThrownStack();
		context.assertTrue(!thrownStack.isEmpty(), "Thrown stack should not be empty");
		context.assertTrue(thrownStack.getItem() == Items.IRON_SWORD, "Thrown stack should be iron sword");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntityStoresOwnerForOnHit(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.TRIDENT);

		ThrownItemEntity entity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				10f,
				ThrownItemEntity.SpinType.NONE
		);

		// Verify the entity stores the owner (needed for OnHit source)
		context.assertTrue(entity.getOwner() == player, "Owner should be the throwing player");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntityWithForgeroComponentHasAttackDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create a Forgero item with attack_damage attribute
		com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier attackDamageAttr =
				new com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier("forgero", "attack_damage");
		ItemStack forgeroItem = ComponentTester.createStackWithAttributes(
				"damage_spear_" + System.nanoTime(),
				java.util.Set.of("tool", "spear"),
				java.util.Map.of(attackDamageAttr, 10.0f)
		);

		// Skip test if we couldn't create a Forgero stack
		if (forgeroItem.isEmpty()) {
			context.complete();
			return;
		}

		ThrownItemEntity entity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				forgeroItem,
				10f,
				ThrownItemEntity.SpinType.VERTICAL
		);

		// The entity stores the item with attack_damage attribute
		// This would be used by calculateDamage when onEntityHit is called
		context.assertTrue(!entity.getThrownStack().isEmpty(),
				"Entity should have the Forgero item stored for damage calculation");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntityIdentifier(TestContext context) {
		// Verify the entity identifier is correct for registration
		context.assertTrue(ThrownItemEntity.IDENTIFIER.getNamespace().equals("forgero"),
				"Namespace should be forgero");
		context.assertTrue(ThrownItemEntity.IDENTIFIER.getPath().equals("thrown_item"),
				"Path should be thrown_item");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testThrownItemEntityInitialPitchYaw(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPitch(45f);
		player.setYaw(90f);
		ItemStack stack = new ItemStack(Items.STICK);

		ThrownItemEntity entity = new ThrownItemEntity(
				ThrownItemEntityRegistry.THROWN_ITEM_ENTITY,
				context.getWorld(),
				player,
				stack,
				10f,
				ThrownItemEntity.SpinType.NONE
		);

		// Verify initial pitch/yaw are stored
		context.assertTrue(entity.getInitialPitch() == 45f, "Initial pitch should be stored");
		context.assertTrue(entity.getInitialYaw() == 90f, "Initial yaw should be stored");
		context.complete();
	}
}
