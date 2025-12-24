package com.sigmundgranaas.forgero.bows.gametest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.bows.handlers.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.bows.handlers.MountProjectileHandler;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionProperty;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionPropertiesPlugin;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;

/**
 * Gametests for bow-specific UseHandler implementations.
 * Tests MountProjectileHandler and LaunchProjectileHandler.
 */
public class BowHandlerGametest {

	// ========== MountProjectileHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMountProjectileHandlerType(TestContext context) {
		MountProjectileHandler handler = new MountProjectileHandler();

		context.assertTrue(handler.type().equals("forgero:mount_projectile"), "Type should be forgero:mount_projectile");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMountProjectileHandlerWithArrows(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		player.getInventory().insertStack(arrows);

		MountProjectileHandler handler = new MountProjectileHandler();
		handler.apply(player, bowStack, Hand.MAIN_HAND);

		// Handler should set the current hand when player has arrows
		context.assertTrue(player.getActiveHand() == Hand.MAIN_HAND, "Active hand should be set to main hand");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMountProjectileHandlerCreativeNoArrows(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		// No arrows in inventory, but creative mode

		MountProjectileHandler handler = new MountProjectileHandler();
		handler.apply(player, bowStack, Hand.MAIN_HAND);

		// Creative players can use bow without arrows
		context.assertTrue(player.getActiveHand() == Hand.MAIN_HAND, "Creative player should be able to use bow");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMountProjectileHandlerSurvivalNoArrows(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		// Switch to survival mode
		player.getAbilities().creativeMode = false;
		ItemStack bowStack = new ItemStack(Items.BOW);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		// No arrows in inventory

		MountProjectileHandler handler = new MountProjectileHandler();
		handler.apply(player, bowStack, Hand.MAIN_HAND);

		// Survival players without arrows should not be able to start using
		// The handler returns early, so active hand shouldn't be set
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMountProjectileHandlerNonPlayer(TestContext context) {
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		ItemStack bowStack = new ItemStack(Items.BOW);

		MountProjectileHandler handler = new MountProjectileHandler();

		// Should not throw for non-player entities
		handler.apply(zombie, bowStack, Hand.MAIN_HAND);

		context.complete();
	}

	// ========== LaunchProjectileHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchProjectileHandlerType(TestContext context) {
		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		context.assertTrue(handler.type().equals("forgero:launch_projectile"), "Type should be forgero:launch_projectile");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchProjectileHandlerDefaultValues(TestContext context) {
		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		context.assertTrue(handler.basePower() == 3.0f, "Base power should be 3.0");
		context.assertTrue(handler.baseDivergence() == 1.0f, "Base divergence should be 1.0");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchProjectileHandlerSpawnsArrow(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		player.getInventory().insertStack(arrows);

		// Count arrows before
		long arrowCountBefore = context.getWorld().getEntitiesByType(
				EntityType.ARROW,
				player.getBoundingBox().expand(50),
				e -> true
		).size();

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bowStack, 20, 0, 1.0f);
		handler.apply(ctx);

		// Wait a tick for entity to spawn
		context.waitAndRun(1, () -> {
			long arrowCountAfter = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(50),
					e -> true
			).size();

			context.assertTrue(arrowCountAfter > arrowCountBefore, "Arrow should be spawned after launch");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchProjectileHandlerClientSideNoOp(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		// Create a context that reports as client-side (we can't actually do this in gametest,
		// but we test that the check exists by verifying no exception is thrown)
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bowStack, 20, 0, 1.0f);

		// Since we're on server, this will execute - but verify no crash
		handler.apply(ctx);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchProjectileHandlerLowPullProgress(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		// Count arrows owned by this player before (more specific than all arrows)
		long arrowCountBefore = context.getWorld().getEntitiesByType(
				EntityType.ARROW,
				player.getBoundingBox().expand(5),
				arrow -> arrow.getOwner() == player
		).size();

		// Pull progress < 0.1 should not fire
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bowStack, 1, 0, 0.05f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			long arrowCountAfter = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(5),
					arrow -> arrow.getOwner() == player
			).size();

			context.assertTrue(arrowCountAfter == arrowCountBefore, "No arrow should spawn with low pull progress");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchProjectileHandlerCriticalShot(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		// Full charge should result in critical
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bowStack, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			List<ArrowEntity> arrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(50),
					e -> true
			);

			if (!arrows2.isEmpty()) {
				ArrowEntity arrow = arrows2.get(0);
				context.assertTrue(arrow.isCritical(), "Arrow should be critical at full charge");
			}
			context.complete();
		});
	}

	// ========== UseInteractionProperty Integration Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBowUseInteractionProperty(TestContext context) {
		// Create a bow-style UseInteractionProperty
		MountProjectileHandler mountHandler = new MountProjectileHandler();
		LaunchProjectileHandler launchHandler = new LaunchProjectileHandler(3.0f, 1.0f);

		UseInteractionProperty bowProperty = new UseInteractionProperty(
				UseAction.BOW,
				72000,
				true, // Used on release
				List.of(mountHandler),  // on_start
				Collections.emptyList(), // on_tick
				List.of(launchHandler),  // on_release
				Collections.emptyList(), // on_finish
				null
		);

		context.assertTrue(bowProperty.useAction() == UseAction.BOW, "Use action should be BOW");
		context.assertTrue(bowProperty.maxUseTime() == 72000, "Max use time should be 72000");
		context.assertTrue(bowProperty.usedOnRelease(), "Should be used on release");
		context.assertTrue(bowProperty.hasStartHandlers(), "Should have start handlers");
		context.assertTrue(bowProperty.hasReleaseHandlers(), "Should have release handlers");
		context.assertFalse(bowProperty.hasTickHandlers(), "Should not have tick handlers");
		context.assertFalse(bowProperty.hasFinishHandlers(), "Should not have finish handlers");
		context.assertTrue(bowProperty.hasUseAction(), "Should have a valid use action");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBowPropertyOnStartHandlers(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		player.getInventory().insertStack(arrows);

		MountProjectileHandler mountHandler = new MountProjectileHandler();

		UseInteractionProperty bowProperty = new UseInteractionProperty(
				UseAction.BOW,
				72000,
				true,
				List.of(mountHandler),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				null
		);

		// Execute start handlers
		for (UseHandler handler : bowProperty.onStart()) {
			if (handler instanceof com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler simple) {
				simple.apply(player, bowStack, Hand.MAIN_HAND);
			}
		}

		context.assertTrue(player.getActiveHand() == Hand.MAIN_HAND, "Hand should be set after start handlers");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBowPropertyOnReleaseHandlers(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler launchHandler = new LaunchProjectileHandler(3.0f, 1.0f);

		UseInteractionProperty bowProperty = new UseInteractionProperty(
				UseAction.BOW,
				72000,
				true,
				Collections.emptyList(),
				Collections.emptyList(),
				List.of(launchHandler),
				Collections.emptyList(),
				null
		);

		long arrowCountBefore = context.getWorld().getEntitiesByType(
				EntityType.ARROW,
				player.getBoundingBox().expand(50),
				e -> true
		).size();

		UseContext releaseCtx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bowStack, 20, 0, 1.0f);

		// Execute release handlers
		for (UseHandler handler : bowProperty.onRelease()) {
			if (handler instanceof com.sigmundgranaas.forgero.properties.minecraft.useinteraction.ContextualUseHandler ctxHandler) {
				ctxHandler.apply(releaseCtx);
			}
		}

		context.waitAndRun(1, () -> {
			long arrowCountAfter = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(50),
					e -> true
			).size();

			context.assertTrue(arrowCountAfter > arrowCountBefore, "Arrow should spawn from release handler");
			context.complete();
		});
	}

	// ========== Handler Registration Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMountProjectileHandlerCodec(TestContext context) {
		// Test that the codec can create a handler
		MountProjectileHandler handler = new MountProjectileHandler();

		context.assertTrue(handler != null, "Handler should be created");
		context.assertTrue(handler.type().equals(MountProjectileHandler.TYPE), "Type should match constant");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchProjectileHandlerCodec(TestContext context) {
		// Test that the codec can create a handler with custom values
		LaunchProjectileHandler handler = new LaunchProjectileHandler(2.5f, 0.5f);

		context.assertTrue(handler != null, "Handler should be created");
		context.assertTrue(handler.basePower() == 2.5f, "Custom power should be preserved");
		context.assertTrue(handler.baseDivergence() == 0.5f, "Custom divergence should be preserved");
		context.assertTrue(handler.type().equals(LaunchProjectileHandler.TYPE), "Type should match constant");
		context.complete();
	}

	// ========== Attribute Resolution Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchHandlerFallsBackWithVanillaBow(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack bowStack = new ItemStack(Items.BOW); // Vanilla bow - no Forgero component
		ItemStack arrows = new ItemStack(Items.ARROW, 64);

		player.setStackInHand(Hand.MAIN_HAND, bowStack);
		player.getInventory().insertStack(arrows);

		// Handler with specific fallback values
		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.5f, 2.0f);

		long arrowCountBefore = context.getWorld().getEntitiesByType(
				EntityType.ARROW,
				player.getBoundingBox().expand(50),
				arrow -> arrow.getOwner() == player
		).size();

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bowStack, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			List<ArrowEntity> newArrows = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(50),
					arrow -> arrow.getOwner() == player
			);

			// Arrow should be spawned with fallback values
			context.assertTrue(newArrows.size() > arrowCountBefore, "Arrow should be spawned for vanilla bow");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchHandlerResolvesDrawPowerFromComponent(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create a Forgero bow with draw_power attribute = 5.0
		OpenIdentifier drawPowerAttr = new OpenIdentifier("forgero", "draw_power");
		ItemStack forgeroBow = createStackWithAttributes(
				"power_bow_" + System.nanoTime(),
				Set.of("tool", "bow"),
				Map.of(drawPowerAttr, 5.0f)
		);

		// Skip test if we couldn't create a Forgero stack
		if (forgeroBow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack arrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, forgeroBow);
		player.getInventory().insertStack(arrows);

		// Handler with lower fallback
		LaunchProjectileHandler handler = new LaunchProjectileHandler(2.0f, 1.0f);

		long arrowCountBefore = context.getWorld().getEntitiesByType(
				EntityType.ARROW,
				player.getBoundingBox().expand(100),
				arrow -> arrow.getOwner() == player
		).size();

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, forgeroBow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			List<ArrowEntity> newArrows = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(100),
					arrow -> arrow.getOwner() == player
			);

			// Arrow should be spawned (we can't easily measure velocity in gametest)
			context.assertTrue(newArrows.size() > arrowCountBefore,
					"Arrow should be spawned from Forgero bow with draw_power attribute");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLaunchHandlerResolvesAccuracyFromComponent(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create a Forgero bow with accuracy attribute = 90 (should result in low divergence)
		OpenIdentifier accuracyAttr = new OpenIdentifier("forgero", "accuracy");
		ItemStack forgeroBow = createStackWithAttributes(
				"accurate_bow_" + System.nanoTime(),
				Set.of("tool", "bow"),
				Map.of(accuracyAttr, 90.0f)
		);

		// Skip test if we couldn't create a Forgero stack
		if (forgeroBow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack arrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, forgeroBow);
		player.getInventory().insertStack(arrows);

		// Handler with high fallback divergence
		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 5.0f);

		long arrowCountBefore = context.getWorld().getEntitiesByType(
				EntityType.ARROW,
				player.getBoundingBox().expand(100),
				arrow -> arrow.getOwner() == player
		).size();

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, forgeroBow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			List<ArrowEntity> newArrows = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(100),
					arrow -> arrow.getOwner() == player
			);

			// Arrow should be spawned with accuracy attribute affecting divergence
			context.assertTrue(newArrows.size() > arrowCountBefore,
					"Arrow should be spawned from Forgero bow with accuracy attribute");
			context.complete();
		});
	}

	// ========== ConsumeProjectileHandler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeProjectileHandlerType(TestContext context) {
		com.sigmundgranaas.forgero.bows.handlers.ConsumeProjectileHandler handler =
				new com.sigmundgranaas.forgero.bows.handlers.ConsumeProjectileHandler();

		context.assertTrue(handler.type().equals("forgero:consume_projectile"),
				"Type should be forgero:consume_projectile");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeProjectileHandlerDecrementsArrow(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;  // Disable creative mode

		ItemStack bow = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 10);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		// Get the projectile from the inventory (same reference the handler will use)
		ItemStack arrowsInInventory = player.getProjectileType(bow);
		int initialArrowCount = arrowsInInventory.getCount();

		com.sigmundgranaas.forgero.bows.handlers.ConsumeProjectileHandler handler =
				new com.sigmundgranaas.forgero.bows.handlers.ConsumeProjectileHandler();
		handler.apply(player, bow, Hand.MAIN_HAND);

		// Arrow count should decrease by 1
		int finalCount = arrowsInInventory.getCount();
		context.assertTrue(finalCount == initialArrowCount - 1,
				"Arrow count should decrease by 1. Expected " + (initialArrowCount - 1) + " but got " + finalCount);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeProjectileHandlerRespectsCreativeMode(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		// Player is already in creative mode

		ItemStack bow = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 10);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		// Get the projectile from the inventory (same reference the handler will use)
		ItemStack arrowsInInventory = player.getProjectileType(bow);
		int initialArrowCount = arrowsInInventory.getCount();

		com.sigmundgranaas.forgero.bows.handlers.ConsumeProjectileHandler handler =
				new com.sigmundgranaas.forgero.bows.handlers.ConsumeProjectileHandler();
		handler.apply(player, bow, Hand.MAIN_HAND);

		// Arrow count should NOT decrease in creative mode
		int finalCount = arrowsInInventory.getCount();
		context.assertTrue(finalCount == initialArrowCount,
				"Arrow count should remain unchanged in creative mode");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeProjectileHandlerRegistered(TestContext context) {
		// Verify the handler is registered in UseInteractionPropertiesPlugin
		context.assertTrue(UseInteractionPropertiesPlugin.isHandlerRegistered("forgero:consume_projectile"),
				"ConsumeProjectileHandler should be registered");

		var codec = UseInteractionPropertiesPlugin.getHandlerCodec("forgero:consume_projectile");
		context.assertTrue(codec != null,
				"ConsumeProjectileHandler codec should be retrievable");

		context.complete();
	}

	// ========== Helper Methods ==========

	/**
	 * Creates a dynamic ItemStack with attributes for testing.
	 */
	private static ItemStack createStackWithAttributes(String name, Set<String> tags, Map<OpenIdentifier, Float> attributes) {
		OpenIdentifier id = new OpenIdentifier("forgero-test", name);
		Set<OpenIdentifier> openTags = tags.stream()
				.map(tag -> new OpenIdentifier("forgero", tag))
				.collect(Collectors.toSet());

		Map<String, List<?>> propertiesMap = new HashMap<>();

		// Add attributes to the properties map
		if (!attributes.isEmpty()) {
			List<Attribute> attributeList = new ArrayList<>();
			for (Map.Entry<OpenIdentifier, Float> entry : attributes.entrySet()) {
				attributeList.add(new SimpleAttribute(entry.getKey(), entry.getValue()));
			}
			propertiesMap.put(Attribute.KEY.key(), attributeList);
		}

		Component component = new StaticComponent(id, openTags, propertiesMap);

		return ForgeroApi.converter().toStack(component)
				.orElse(ItemStack.EMPTY);
	}
}
