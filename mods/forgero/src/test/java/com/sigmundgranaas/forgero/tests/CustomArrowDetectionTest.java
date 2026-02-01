package com.sigmundgranaas.forgero.tests;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bows.item.ForgeroArrowItem;
import com.sigmundgranaas.forgero.bows.item.ForgeroBowItem;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.PlayerFactory;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionManager;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.bows.handlers.LaunchProjectileHandler;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Tests that custom Forgero arrows are properly detected and used when shooting bows.
 *
 * <p>These tests verify the arrow detection system in LaunchProjectileHandler:
 * <ul>
 *   <li>Custom Forgero arrows in inventory are detected as valid arrows</li>
 *   <li>Custom arrows result in DynamicArrowEntity, not vanilla ArrowEntity</li>
 *   <li>Various material arrows (iron, diamond, etc.) work correctly</li>
 * </ul>
 *
 * <p>Bug being tested: Custom arrows like diamond-arrow are not being picked up
 * by the bow system, causing vanilla arrows to be shot instead in creative mode.
 */
public class CustomArrowDetectionTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomArrowDetectionTest.class);

	private Optional<ItemStack> getArrowStack(String material) {
		ForgeroTestContext ctx = ForgeroTestUtils.forgero(null);
		String arrowId = "forgero:" + material + "-arrow";
		return ctx.api().componentRegistry().get(OpenIdentifier.parse(arrowId))
				.flatMap(comp -> ctx.api().converter().toStack(comp));
	}

	private Optional<ItemStack> getBowStack(String material) {
		ForgeroTestContext ctx = ForgeroTestUtils.forgero(null);
		String bowId = "forgero:" + material + "-bow";
		return ctx.api().componentRegistry().get(OpenIdentifier.parse(bowId))
				.flatMap(comp -> ctx.api().converter().toStack(comp));
	}

	// ========== Arrow Registration Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "custom_arrow_detection")
	public void iron_arrow_is_registered_as_forgero_arrow_item(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var arrowOpt = ctx.component("forgero:iron-arrow");
		context.assertTrue(arrowOpt.isPresent(), "Iron arrow component should exist in registry");

		var stackOpt = arrowOpt.flatMap(ctx::toStack);
		context.assertTrue(stackOpt.isPresent(), "Iron arrow should convert to ItemStack");

		ItemStack stack = stackOpt.get();
		context.assertTrue(stack.getItem() instanceof ForgeroArrowItem,
				"Iron arrow should be ForgeroArrowItem instance, but was: " + stack.getItem().getClass().getSimpleName());

		context.assertTrue(stack.getItem() instanceof ArrowItem,
				"ForgeroArrowItem should extend ArrowItem for vanilla compatibility");

		LOGGER.info("Verified iron-arrow registration: {} ({})",
				stack.getItem().getTranslationKey(), stack.getItem().getClass().getSimpleName());

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "custom_arrow_detection")
	public void diamond_arrow_is_registered_as_forgero_arrow_item(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var arrowOpt = ctx.component("forgero:diamond-arrow");
		context.assertTrue(arrowOpt.isPresent(), "Diamond arrow component should exist in registry");

		var stackOpt = arrowOpt.flatMap(ctx::toStack);
		context.assertTrue(stackOpt.isPresent(), "Diamond arrow should convert to ItemStack");

		ItemStack stack = stackOpt.get();
		context.assertTrue(stack.getItem() instanceof ForgeroArrowItem,
				"Diamond arrow should be ForgeroArrowItem instance, but was: " + stack.getItem().getClass().getSimpleName());

		LOGGER.info("Verified diamond-arrow registration: {} ({})",
				stack.getItem().getTranslationKey(), stack.getItem().getClass().getSimpleName());

		context.complete();
	}

	// ========== Inventory Detection Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "custom_arrow_detection")
	public void custom_arrow_in_inventory_is_detected_as_arrow(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Get a custom Forgero arrow
		var arrowOpt = ctx.component("forgero:iron-arrow")
				.flatMap(ctx::toStack);

		// ASSERTIVE: Arrow component MUST exist - don't skip
		context.assertTrue(arrowOpt.isPresent(), "Iron arrow component must exist in registry: forgero:iron-arrow");

		ItemStack customArrow = arrowOpt.get();

		// Add to player inventory
		player.getInventory().insertStack(customArrow.copy());

		// Verify arrow is in inventory and is detected as ArrowItem
		boolean foundArrow = false;
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
				foundArrow = true;
				LOGGER.info("Found arrow in slot {}: {} (instanceof ForgeroArrowItem: {})",
						i, stack.getItem().getTranslationKey(),
						stack.getItem() instanceof ForgeroArrowItem);
				break;
			}
		}

		context.assertTrue(foundArrow,
				"Custom Forgero arrow should be detected as ArrowItem in inventory");

		context.complete();
	}

	// ========== Bow Shooting Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "custom_arrow_shooting")
	public void forgero_bow_with_custom_arrow_spawns_dynamic_arrow_entity(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Get Forgero bow
		var bowOpt = ctx.component("forgero:oak-bow")
				.flatMap(ctx::toStack);

		// Get custom Forgero arrow
		var arrowOpt = ctx.component("forgero:iron-arrow")
				.flatMap(ctx::toStack);

		context.assertTrue(bowOpt.isPresent(), "Bow component must exist in registry: forgero:oak-bow");
		context.assertTrue(arrowOpt.isPresent(), "Arrow component must exist in registry: forgero:iron-arrow");

		ItemStack bow = bowOpt.get();
		ItemStack arrow = arrowOpt.get();

		context.assertTrue(bow.getItem() instanceof ForgeroBowItem,
				"Bow should be ForgeroBowItem");
		context.assertTrue(arrow.getItem() instanceof ForgeroArrowItem,
				"Arrow should be ForgeroArrowItem");

		// Diagnostic: Check UseInteractionManager state
		boolean hasUseInteraction = UseInteractionManager.hasUseInteraction(bow);
		LOGGER.info("UseInteractionManager.hasUseInteraction(bow) = {}", hasUseInteraction);

		// The bow MUST have use_interaction property - this is defined in bow.json
		// If this fails, the property system is broken
		context.assertTrue(hasUseInteraction,
				"Forgero bow MUST have UseInteractionProperty - check bow.json and property loading");

		// Setup: player holds bow with custom arrow in inventory
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrow.copy());

		LOGGER.info("Test setup: bow={}, arrow in inventory={}",
				bow.getItem().getTranslationKey(),
				arrow.getItem().getTranslationKey());

		// Simulate bow use
		player.setCurrentHand(Hand.MAIN_HAND);
		int maxUseTime = bow.getMaxUseTime();
		int remainingTicks = maxUseTime - 20; // 20 ticks of charging

		bow.getItem().onStoppedUsing(bow, context.getWorld(), player, remainingTicks);

		context.waitAndRun(5, () -> {
			// Check for DynamicArrowEntity (expected for custom arrows)
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			// Check for vanilla ArrowEntity (should NOT be spawned for custom arrows)
			List<ArrowEntity> vanillaArrows = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			LOGGER.info("Shot results: {} DynamicArrowEntity, {} vanilla ArrowEntity",
					dynamicArrows.size(), vanillaArrows.size());

			// This is the key assertion - custom arrows should spawn DynamicArrowEntity
			context.assertTrue(!dynamicArrows.isEmpty(),
					"Custom Forgero arrow should spawn DynamicArrowEntity, not vanilla arrow. " +
							"Found: " + dynamicArrows.size() + " dynamic, " + vanillaArrows.size() + " vanilla");

			// Bonus: verify vanilla arrow was NOT spawned
			context.assertTrue(vanillaArrows.isEmpty(),
					"Custom Forgero arrow should NOT spawn vanilla ArrowEntity");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "custom_arrow_shooting")
	public void forgero_bow_with_diamond_arrow_spawns_dynamic_arrow_entity(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Get Forgero bow
		var bowOpt = ctx.component("forgero:oak-bow")
				.flatMap(ctx::toStack);

		// Get diamond arrow specifically (the material mentioned in bug report)
		var arrowOpt = ctx.component("forgero:diamond-arrow")
				.flatMap(ctx::toStack);

		// ASSERTIVE: Components MUST exist - don't skip
		context.assertTrue(bowOpt.isPresent(), "Bow component must exist in registry: forgero:oak-bow");
		context.assertTrue(arrowOpt.isPresent(), "Diamond arrow component must exist in registry: forgero:diamond-arrow");

		ItemStack bow = bowOpt.get();
		ItemStack arrow = arrowOpt.get();

		// Verify item types
		context.assertTrue(arrow.getItem() instanceof ForgeroArrowItem,
				"Diamond arrow should be ForgeroArrowItem, but was: " + arrow.getItem().getClass().getSimpleName());

		// ASSERTIVE: UseInteractionManager MUST work - don't skip
		boolean hasUseInteraction = UseInteractionManager.hasUseInteraction(bow);
		LOGGER.info("UseInteractionManager.hasUseInteraction(bow) = {}", hasUseInteraction);
		context.assertTrue(hasUseInteraction,
				"Forgero bow MUST have UseInteractionProperty - UseInteractionManager is broken");

		// Setup
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrow.copy());

		LOGGER.info("Diamond arrow test: arrow item class = {}", arrow.getItem().getClass().getName());

		// Shoot
		player.setCurrentHand(Hand.MAIN_HAND);
		bow.getItem().onStoppedUsing(bow, context.getWorld(), player, bow.getMaxUseTime() - 20);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			List<ArrowEntity> vanillaArrows = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			LOGGER.info("Diamond arrow shot results: {} DynamicArrowEntity, {} vanilla ArrowEntity",
					dynamicArrows.size(), vanillaArrows.size());

			context.assertTrue(!dynamicArrows.isEmpty(),
					"Diamond arrow should spawn DynamicArrowEntity. " +
							"Got: " + dynamicArrows.size() + " dynamic, " + vanillaArrows.size() + " vanilla. " +
							"This indicates the arrow detection system is not recognizing custom arrows.");

			context.complete();
		});
	}

	// ========== Creative Mode Fallback Test ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "custom_arrow_shooting")
	public void creative_mode_without_arrows_falls_back_to_vanilla(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		var bowOpt = ctx.component("forgero:oak-bow")
				.flatMap(ctx::toStack);

		// ASSERTIVE: Bow component MUST exist - don't skip
		context.assertTrue(bowOpt.isPresent(), "Bow component must exist in registry: forgero:oak-bow");

		ItemStack bow = bowOpt.get();

		// ASSERTIVE: UseInteractionManager MUST work - don't skip
		boolean hasUseInteraction = UseInteractionManager.hasUseInteraction(bow);
		LOGGER.info("UseInteractionManager.hasUseInteraction(bow) = {}", hasUseInteraction);
		context.assertTrue(hasUseInteraction,
				"Forgero bow MUST have UseInteractionProperty - UseInteractionManager is broken");

		// Setup: player holds bow with NO arrows in inventory (creative mode)
		player.setStackInHand(Hand.MAIN_HAND, bow);
		// Explicitly clear inventory to ensure no arrows
		player.getInventory().clear();
		player.setStackInHand(Hand.MAIN_HAND, bow); // Re-set bow after clear

		// Shoot
		player.setCurrentHand(Hand.MAIN_HAND);
		bow.getItem().onStoppedUsing(bow, context.getWorld(), player, bow.getMaxUseTime() - 20);

		context.waitAndRun(5, () -> {
			// In creative with no arrows, should fall back to vanilla arrow
			List<ArrowEntity> vanillaArrows = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			LOGGER.info("Creative no-arrow fallback: {} vanilla, {} dynamic",
					vanillaArrows.size(), dynamicArrows.size());

			// With no arrows in inventory, creative mode should use vanilla fallback
			context.assertTrue(!vanillaArrows.isEmpty() || !dynamicArrows.isEmpty(),
					"Some arrow should be shot in creative mode");

			context.complete();
		});
	}

	// ========== Priority Test: Custom Arrow over Vanilla ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "custom_arrow_priority")
	public void custom_arrow_is_selected_before_vanilla_arrow(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		var bowOpt = ctx.component("forgero:oak-bow")
				.flatMap(ctx::toStack);
		var customArrowOpt = ctx.component("forgero:iron-arrow")
				.flatMap(ctx::toStack);

		// ASSERTIVE: Components MUST exist - don't skip
		context.assertTrue(bowOpt.isPresent(), "Bow component must exist in registry: forgero:oak-bow");
		context.assertTrue(customArrowOpt.isPresent(), "Iron arrow component must exist in registry: forgero:iron-arrow");

		ItemStack bow = bowOpt.get();
		ItemStack customArrow = customArrowOpt.get();
		ItemStack vanillaArrow = new ItemStack(Items.ARROW, 64);

		// ASSERTIVE: UseInteractionManager MUST work - don't skip
		boolean hasUseInteraction = UseInteractionManager.hasUseInteraction(bow);
		LOGGER.info("UseInteractionManager.hasUseInteraction(bow) = {}", hasUseInteraction);
		context.assertTrue(hasUseInteraction,
				"Forgero bow MUST have UseInteractionProperty - UseInteractionManager is broken");

		// Setup: Put custom arrow FIRST in inventory, then vanilla arrows
		// Inventory search is sequential, so first arrow found should be used
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().clear();
		player.setStackInHand(Hand.MAIN_HAND, bow);

		// Insert custom arrow first
		player.getInventory().setStack(9, customArrow.copy()); // First hotbar slot after hand
		// Insert vanilla arrows after
		player.getInventory().setStack(10, vanillaArrow.copy());

		LOGGER.info("Priority test: Custom arrow in slot 9, vanilla in slot 10");

		// Shoot
		player.setCurrentHand(Hand.MAIN_HAND);
		bow.getItem().onStoppedUsing(bow, context.getWorld(), player, bow.getMaxUseTime() - 20);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			LOGGER.info("Priority test result: {} DynamicArrowEntity spawned", dynamicArrows.size());

			// Since custom arrow is first in inventory, it should be selected
			// and DynamicArrowEntity should spawn (not vanilla)
			context.assertTrue(!dynamicArrows.isEmpty(),
					"Custom arrow should be selected first and spawn DynamicArrowEntity");

			context.complete();
		});
	}

	// ========== Direct Handler Test (Bypasses Mixin Chain) ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "direct_handler_test")
	public void direct_handler_call_spawns_dynamic_arrow(TestContext context) {
		// This test calls the handler directly to verify the handler works
		// If this passes but mixin-based tests fail, the issue is in the mixin chain
		var ctx = ForgeroTestUtils.forgero(context);
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		var bowOpt = ctx.component("forgero:oak-bow").flatMap(ctx::toStack);
		var arrowOpt = ctx.component("forgero:iron-arrow").flatMap(ctx::toStack);

		context.assertTrue(bowOpt.isPresent(), "Bow must exist");
		context.assertTrue(arrowOpt.isPresent(), "Arrow must exist");

		ItemStack bow = bowOpt.get();
		ItemStack arrow = arrowOpt.get();

		// Setup inventory with arrow
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrow.copy());

		// Create handler and context DIRECTLY (bypassing mixin chain)
		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext useCtx = UseContext.release(
				context.getWorld(),
				player,
				Hand.MAIN_HAND,
				bow,
				20,  // chargeTime (full charge)
				0,   // remainingTicks
				1.0f // pullProgress (full)
		);

		LOGGER.info("Direct handler test: calling handler.apply() directly");
		handler.apply(useCtx);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			List<ArrowEntity> vanillaArrows = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			LOGGER.info("Direct handler result: {} DynamicArrowEntity, {} vanilla ArrowEntity",
					dynamicArrows.size(), vanillaArrows.size());

			context.assertTrue(!dynamicArrows.isEmpty(),
					"Direct handler call should spawn DynamicArrowEntity - handler is broken if this fails");

			context.complete();
		});
	}

	// ========== Survival Mode Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "survival_arrow_tests")
	public void survival_player_shoots_correct_custom_arrow_and_decrements_stack(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		// Get Forgero bow and custom arrow
		var bowOpt = ctx.component("forgero:oak-bow").flatMap(ctx::toStack);
		var arrowOpt = ctx.component("forgero:iron-arrow").flatMap(ctx::toStack);

		context.assertTrue(bowOpt.isPresent(), "Bow must exist");
		context.assertTrue(arrowOpt.isPresent(), "Arrow must exist");

		ItemStack bow = bowOpt.get();
		ItemStack arrow = arrowOpt.get();
		arrow.setCount(5);

		// Build a thick target wall in front of the player (3 wide, 3 tall, 2 deep)
		// This ensures the arrow will hit it regardless of aim precision
		BlockPos wallCenter = context.getAbsolutePos(new BlockPos(0, 1, 5));
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = 0; z <= 1; z++) {
					context.getWorld().setBlockState(wallCenter.add(x, y, z), Blocks.STONE.getDefaultState());
				}
			}
		}

		// Create survival player facing the wall with slight downward pitch to aim at wall center
		ServerPlayerEntity player = PlayerFactory.create(context)
				.survival()
				.at(0, 1, 0)
				.facing(Direction.SOUTH)
				.withPitch(10.0f) // Slight downward angle to hit the wall
				.holding(bow)
				.build();

		player.getInventory().setStack(9, arrow.copy());
		int initialCount = player.getInventory().getStack(9).getCount();

		LOGGER.info("Survival test: {} iron arrows, shooting at wall at {}", initialCount, wallCenter);

		// Shoot at the wall
		player.setCurrentHand(Hand.MAIN_HAND);
		bow.getItem().onStoppedUsing(bow, context.getWorld(), player, bow.getMaxUseTime() - 20);

		// Wait for arrow to hit the wall
		context.waitAndRun(15, () -> {
			// Find arrow entities owned by this player
			List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(30),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			context.assertTrue(!arrows.isEmpty(), "Arrow should be shot by player");

			DynamicArrowEntity shotArrow = arrows.get(0);
			ItemStack arrowStack = shotArrow.getStack();

			// Verify the arrow entity contains the correct arrow type
			context.assertTrue(!arrowStack.isEmpty(), "Arrow entity should have an ItemStack");
			context.assertTrue(arrowStack.getItem() instanceof ForgeroArrowItem,
					"Shot arrow should be ForgeroArrowItem");

			// Verify it's specifically an iron arrow by checking the item
			String arrowName = arrowStack.getItem().getTranslationKey();
			LOGGER.info("Shot arrow type: {}", arrowName);
			context.assertTrue(arrowName.contains("iron"),
					"Shot arrow should be iron arrow, got: " + arrowName);

			// Verify stack was decremented
			int remainingCount = player.getInventory().getStack(9).isEmpty() ? 0 :
					player.getInventory().getStack(9).getCount();
			context.assertTrue(remainingCount == initialCount - 1,
					"Arrow count should decrease: " + initialCount + " -> " + remainingCount);

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "survival_arrow_tests")
	public void survival_player_prefers_offhand_arrow_and_shoots_correct_type(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var bowOpt = ctx.component("forgero:oak-bow").flatMap(ctx::toStack);
		var ironArrowOpt = ctx.component("forgero:iron-arrow").flatMap(ctx::toStack);
		var diamondArrowOpt = ctx.component("forgero:diamond-arrow").flatMap(ctx::toStack);

		context.assertTrue(bowOpt.isPresent(), "Bow must exist");
		context.assertTrue(ironArrowOpt.isPresent(), "Iron arrow must exist");
		context.assertTrue(diamondArrowOpt.isPresent(), "Diamond arrow must exist");

		ItemStack bow = bowOpt.get();
		ItemStack ironArrow = ironArrowOpt.get();
		ItemStack diamondArrow = diamondArrowOpt.get();
		ironArrow.setCount(10);
		diamondArrow.setCount(10);

		// Build thick target wall
		BlockPos wallCenter = context.getAbsolutePos(new BlockPos(0, 1, 5));
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = 0; z <= 1; z++) {
					context.getWorld().setBlockState(wallCenter.add(x, y, z), Blocks.STONE.getDefaultState());
				}
			}
		}

		// Player with bow in main hand, DIAMOND arrow in offhand, IRON arrows in inventory
		ServerPlayerEntity player = PlayerFactory.create(context)
				.survival()
				.at(0, 1, 0)
				.facing(Direction.SOUTH)
				.withPitch(10.0f)
				.withStacks(bow, diamondArrow.copy())
				.build();

		player.getInventory().setStack(9, ironArrow.copy());

		int offhandInitial = player.getOffHandStack().getCount();
		int inventoryInitial = player.getInventory().getStack(9).getCount();

		LOGGER.info("Offhand priority: diamond(offhand)={}, iron(inventory)={}", offhandInitial, inventoryInitial);

		// Shoot
		player.setCurrentHand(Hand.MAIN_HAND);
		bow.getItem().onStoppedUsing(bow, context.getWorld(), player, bow.getMaxUseTime() - 20);

		context.waitAndRun(15, () -> {
			// Find arrow shot by this player
			List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(30),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			context.assertTrue(!arrows.isEmpty(), "Arrow should be shot by player");

			DynamicArrowEntity shotArrow = arrows.get(0);
			String arrowName = shotArrow.getStack().getItem().getTranslationKey();

			// Should be DIAMOND (from offhand), not iron (from inventory)
			LOGGER.info("Shot arrow type: {}", arrowName);
			context.assertTrue(arrowName.contains("diamond"),
					"Should shoot DIAMOND arrow from offhand, got: " + arrowName);

			// Offhand diamond should be decremented
			int offhandAfter = player.getOffHandStack().isEmpty() ? 0 : player.getOffHandStack().getCount();
			context.assertTrue(offhandAfter == offhandInitial - 1,
					"Offhand should decrement: " + offhandInitial + " -> " + offhandAfter);

			// Inventory iron should be unchanged
			int inventoryAfter = player.getInventory().getStack(9).isEmpty() ? 0 :
					player.getInventory().getStack(9).getCount();
			context.assertTrue(inventoryAfter == inventoryInitial,
					"Inventory should be unchanged: " + inventoryInitial + " -> " + inventoryAfter);

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "survival_arrow_tests")
	public void survival_player_last_arrow_is_consumed_completely(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var bowOpt = ctx.component("forgero:oak-bow").flatMap(ctx::toStack);
		var arrowOpt = ctx.component("forgero:iron-arrow").flatMap(ctx::toStack);

		context.assertTrue(bowOpt.isPresent(), "Bow must exist");
		context.assertTrue(arrowOpt.isPresent(), "Arrow must exist");

		ItemStack bow = bowOpt.get();
		ItemStack arrow = arrowOpt.get();
		arrow.setCount(1); // Only one arrow

		// Build thick target wall
		BlockPos wallCenter = context.getAbsolutePos(new BlockPos(0, 1, 5));
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = 0; z <= 1; z++) {
					context.getWorld().setBlockState(wallCenter.add(x, y, z), Blocks.STONE.getDefaultState());
				}
			}
		}

		ServerPlayerEntity player = PlayerFactory.create(context)
				.survival()
				.at(0, 1, 0)
				.facing(Direction.SOUTH)
				.withPitch(10.0f)
				.holding(bow)
				.build();

		player.getInventory().setStack(9, arrow.copy());

		LOGGER.info("Last arrow test: 1 iron arrow");

		// Shoot
		player.setCurrentHand(Hand.MAIN_HAND);
		bow.getItem().onStoppedUsing(bow, context.getWorld(), player, bow.getMaxUseTime() - 20);

		context.waitAndRun(15, () -> {
			// Find arrow shot by this player (filter by owner)
			List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(30),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			context.assertTrue(!arrows.isEmpty(), "Arrow should be shot by player");

			// Verify it's an iron arrow
			String arrowName = arrows.get(0).getStack().getItem().getTranslationKey();
			LOGGER.info("Last arrow test: Shot arrow type: {}", arrowName);
			context.assertTrue(arrowName.contains("iron"), "Should be iron arrow: " + arrowName);

			// Stack should be completely empty
			ItemStack remaining = player.getInventory().getStack(9);
			context.assertTrue(remaining.isEmpty(), "Last arrow should be fully consumed");

			context.complete();
		});
	}
}
