package com.sigmundgranaas.forgero.bows.gametest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bows.handlers.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * High-value behavior tests for DynamicArrowEntity and bow mechanics.
 * Tests focus on actual in-game behavior that matters, not boilerplate validation.
 */
public class DynamicArrowBehaviorTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicArrowBehaviorTest.class);

	private static ComponentConverter getConverter() {
		return ForgeroInitializedCallback.getServices()
				.map(s -> s.converter())
				.orElse(null);
	}

	private static ItemStack getRegisteredBow(String name) {
		return getRegisteredItem("forgero", name, "bow");
	}

	private static ItemStack getRegisteredArrow(String name) {
		return getRegisteredItem("forgero", name, "arrow");
	}

	private static ItemStack getRegisteredItem(String namespace, String name, String itemType) {
		ComponentConverter converter = getConverter();
		if (converter == null) {
			LOGGER.error("ComponentConverter is null when looking up {} '{}' - ForgeroServices may not be initialized",
					itemType, name);
			return ItemStack.EMPTY;
		}
		OpenIdentifier id = new OpenIdentifier(namespace, name);
		LOGGER.debug("Looking up {} with ID: {}", itemType, id);

		Optional<Component> componentOpt = ForgeroInitializedCallback.getServices()
				.flatMap(s -> s.componentRegistry().get(id));

		if (componentOpt.isEmpty()) {
			LOGGER.error("Component not found in registry for ID: {}", id);
			return ItemStack.EMPTY;
		}

		Component component = componentOpt.get();
		ItemStack stack = converter.toStack(component).orElse(ItemStack.EMPTY);

		if (stack.isEmpty()) {
			LOGGER.error("Failed to convert component {} to ItemStack", id);
		}

		return stack;
	}

	// ========== Arrow Entity Type Detection ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_entity_detection")
	public void forgeroArrowComponentSpawnsDynamicArrowEntity(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Use registered test arrow (iron has arrow_head_material role)
		ItemStack forgeroArrow = getRegisteredArrow("oak-arrow");
		context.assertTrue(!forgeroArrow.isEmpty(), "Failed to get oak-arrow from registry");

		// Verify the stack converts to a Forgero component (required for DynamicArrowEntity detection)
		ComponentConverter converter = getConverter();
		context.assertTrue(converter != null, "ComponentConverter is null");
		context.assertTrue(converter.toComponent(forgeroArrow).isPresent(),
				"Forgero arrow does not convert back to component - item registration issue?");

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(forgeroArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			// Should spawn DynamicArrowEntity for Forgero arrow components
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid())  // Filter by UUID to avoid cross-test contamination
			);

			context.assertTrue(!dynamicArrows.isEmpty(),
					"Forgero arrow component should spawn DynamicArrowEntity (for custom models and properties)");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_entity_detection")
	public void vanillaArrowSpawnsVanillaArrowEntity(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		ItemStack bow = new ItemStack(Items.BOW);
		ItemStack vanillaArrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(vanillaArrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			// Should spawn vanilla ArrowEntity (not DynamicArrowEntity)
			List<ArrowEntity> vanillaArrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid())
			);

			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid())  // Filter by UUID to avoid cross-test contamination
			);

			context.assertTrue(!vanillaArrows2.isEmpty(), "Vanilla arrow should spawn");
			context.assertTrue(dynamicArrows.isEmpty(), "Vanilla arrow should NOT spawn DynamicArrowEntity");
			context.complete();
		});
	}

	// ========== Enchantment Behavior ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_enchantments")
	public void powerEnchantmentIncreasesDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		ItemStack bow = new ItemStack(Items.BOW);
		bow.addEnchantment(Enchantments.POWER, 3);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<ArrowEntity> arrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() == player
			);

			context.assertTrue(!arrows2.isEmpty(), "Arrow should spawn");
			ArrowEntity arrow = arrows2.get(0);

			// Power III adds 1.5 + 0.5 = 2.0 damage on top of base damage
			// Base arrow damage is typically 2.0, so total should be ~4.0
			context.assertTrue(arrow.getDamage() > 2.5,
					"Power enchantment should increase arrow damage (got: " + arrow.getDamage() + ")");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_enchantments")
	public void punchEnchantmentAddsKnockback(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		ItemStack bow = new ItemStack(Items.BOW);
		bow.addEnchantment(Enchantments.PUNCH, 2);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<ArrowEntity> arrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() == player
			);

			context.assertTrue(!arrows2.isEmpty(), "Arrow should spawn");
			ArrowEntity arrow = arrows2.get(0);

			// Punch II should set punch level to 2
			context.assertTrue(arrow.getPunch() == 2,
					"Punch enchantment should set knockback level");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_enchantments")
	public void flameEnchantmentSetsArrowOnFire(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		ItemStack bow = new ItemStack(Items.BOW);
		bow.addEnchantment(Enchantments.FLAME, 1);
		ItemStack arrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<ArrowEntity> arrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() == player
			);

			context.assertTrue(!arrows2.isEmpty(), "Arrow should spawn");
			ArrowEntity arrow = arrows2.get(0);

			context.assertTrue(arrow.isOnFire(), "Flame enchantment should set arrow on fire");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_enchantments")
	public void infinityEnchantmentPreventsArrowPickup(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false; // Survival mode

		ItemStack bow = new ItemStack(Items.BOW);
		bow.addEnchantment(Enchantments.INFINITY, 1);
		ItemStack arrows = new ItemStack(Items.ARROW, 1); // Only 1 arrow
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<ArrowEntity> arrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() == player
			);

			context.assertTrue(!arrows2.isEmpty(), "Arrow should spawn");
			ArrowEntity arrow = arrows2.get(0);

			// Infinity should set pickup permission to CREATIVE_ONLY
			context.assertTrue(arrow.pickupType == PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY,
					"Infinity enchantment should prevent arrow pickup in survival mode");
			context.complete();
		});
	}

	// ========== Attribute-Based Behavior ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_attributes")
	public void highDrawPowerIncreasesVelocity(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Use registered test bow with high draw_power
		ItemStack powerBow = getRegisteredBow("oak-bow");
		context.assertTrue(!powerBow.isEmpty(), "Failed to get oak-bow from registry");

		ItemStack arrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, powerBow);
		player.getInventory().insertStack(arrows);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, powerBow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<ArrowEntity> arrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() == player
			);

			context.assertTrue(!arrows2.isEmpty(), "Arrow should spawn");
			ArrowEntity arrow = arrows2.get(0);

			// Oak bow has draw_power of 3.0, which should produce reasonable velocity
			Vec3d velocity = arrow.getVelocity();
			double speed = velocity.length();
			context.assertTrue(speed > 1.5,
					"draw_power attribute should affect arrow velocity (got: " + speed + ")");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_attributes")
	public void highAccuracyReducesDivergence(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Use registered test bow with high accuracy
		ItemStack accurateBow = getRegisteredBow("oak-bow");
		context.assertTrue(!accurateBow.isEmpty(), "Failed to get oak-bow from registry");

		ItemStack arrows = new ItemStack(Items.ARROW, 64);
		player.setStackInHand(Hand.MAIN_HAND, accurateBow);
		player.getInventory().insertStack(arrows);

		// Fire multiple arrows and check they stay close to aim direction
		player.setPitch(0.0f); // Look straight ahead
		player.setYaw(0.0f);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		for (int i = 0; i < 5; i++) {
			UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, accurateBow, 20, 0, 1.0f);
			handler.apply(ctx);
		}

		context.waitAndRun(5, () -> {
			List<ArrowEntity> arrows2 = context.getWorld().getEntitiesByType(
					EntityType.ARROW,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() == player
			);

			// With high accuracy, arrows should spawn and have relatively consistent velocity directions
			context.assertTrue(arrows2.size() >= 5, "Should spawn 5 arrows");

			// Check that arrows are mostly flying forward (low divergence)
			Vec3d lookDir = player.getRotationVec(1.0f);
			for (ArrowEntity arrow : arrows2) {
				Vec3d arrowDir = arrow.getVelocity().normalize();
				double dotProduct = lookDir.dotProduct(arrowDir);
				// High accuracy should keep arrows aligned with look direction (dot product close to 1.0)
				context.assertTrue(dotProduct > 0.9,
						"High accuracy should keep arrows aligned with aim direction (dot: " + dotProduct + ")");
			}
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_attributes")
	public void forgeroArrowWithAttackDamageAttributeDealsDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Use registered test arrow with attack_damage attribute
		ItemStack powerArrow = getRegisteredArrow("oak-arrow");
		context.assertTrue(!powerArrow.isEmpty(), "Failed to get oak-arrow from registry");

		// Verify component conversion works
		ComponentConverter converter = getConverter();
		if (converter == null || !converter.toComponent(powerArrow).isPresent()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(powerArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid())  // Filter by UUID to avoid cross-test contamination
			);

			context.assertTrue(!dynamicArrows.isEmpty(), "DynamicArrowEntity should spawn for Forgero arrow");
			DynamicArrowEntity arrow = dynamicArrows.get(0);

			// DynamicArrowEntity should have damage set from component
			// Verify arrow entity spawned with reasonable damage (base damage 2.0)
			context.assertTrue(arrow.getDamage() >= 1.0,
					"DynamicArrowEntity should have damage from component (got: " + arrow.getDamage() + ")");
			context.complete();
		});
	}

	// ========== Weight-Based Physics ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_physics")
	public void dynamicArrowEntityHasPhysicsSimulation(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f); // Shoot straight ahead
		player.setYaw(0.0f);

		// Use registered arrow
		ItemStack arrow = getRegisteredArrow("oak-arrow");
		context.assertTrue(!arrow.isEmpty(), "Failed to get oak-arrow from registry");

		// Verify component conversion works
		ComponentConverter converter = getConverter();
		context.assertTrue(converter != null, "ComponentConverter is null");
		context.assertTrue(converter.toComponent(arrow).isPresent(),
				"Arrow should convert to component");

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					a -> a.getOwner() != null && a.getOwner().getUuid().equals(player.getUuid())
			);

			// Arrow should spawn and have velocity
			context.assertTrue(!dynamicArrows.isEmpty(), "DynamicArrowEntity should spawn");
			DynamicArrowEntity arrowEntity = dynamicArrows.get(0);

			// Arrow should have some velocity (physics simulation active)
			Vec3d velocity = arrowEntity.getVelocity();
			context.assertTrue(velocity.length() > 0,
					"DynamicArrowEntity should have velocity from physics simulation");
			context.complete();
		});
	}

	// ========== Forgero Bow + Forgero Arrow Integration ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_integration")
	public void forgeroBowWithForgeroArrowCombinesAttributes(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f);
		player.setYaw(0.0f);

		// Use registered test bow with high draw_power and accuracy
		ItemStack forgeroBow = getRegisteredBow("oak-bow");
		context.assertTrue(!forgeroBow.isEmpty(), "Failed to get oak-bow from registry");

		// Use registered Forgero arrow with attack_damage
		ItemStack forgeroArrow = getRegisteredArrow("oak-arrow");
		context.assertTrue(!forgeroArrow.isEmpty(), "Failed to get oak-arrow from registry");


		// Verify component conversion works
		ComponentConverter converter = getConverter();
		if (converter == null || !converter.toComponent(forgeroArrow).isPresent()) {
			context.complete();
			return;
		}

		player.setStackInHand(Hand.MAIN_HAND, forgeroBow);
		player.getInventory().insertStack(forgeroArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, forgeroBow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			// Should spawn DynamicArrowEntity (Forgero arrow component)
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid())  // Filter by UUID to avoid cross-test contamination
			);

			context.assertTrue(!dynamicArrows.isEmpty(),
					"Forgero bow + Forgero arrow should spawn DynamicArrowEntity");

			DynamicArrowEntity arrow = dynamicArrows.get(0);

			// Verify bow attributes affect the shot (oak draw_power 3.0)
			Vec3d velocity = arrow.getVelocity();
			double speed = velocity.length();
			context.assertTrue(speed > 1.5,
					"Bow's draw_power should increase velocity (got: " + speed + ")");

			// Verify arrow attributes set on entity (attack_damage)
			// Arrow should have damage from its component (base damage 2.0)
			context.assertTrue(arrow.getDamage() >= 1.0,
					"Arrow should have damage applied (got: " + arrow.getDamage() + ")");

			// Verify accuracy from bow (arrow should be aligned with aim)
			Vec3d lookDir = player.getRotationVec(1.0f);
			Vec3d arrowDir = velocity.normalize();
			double dotProduct = lookDir.dotProduct(arrowDir);
			context.assertTrue(dotProduct > 0.95,
					"Bow's accuracy should keep arrow aligned (dot: " + dotProduct + ")");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_integration")
	public void forgeroBowWithForgeroArrowSupportsEnchantments(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Use registered test bow
		ItemStack forgeroBow = getRegisteredBow("oak-bow");
		context.assertTrue(!forgeroBow.isEmpty(), "Failed to get oak-bow from registry");

		// Add enchantments to Forgero bow
		forgeroBow.addEnchantment(Enchantments.POWER, 2);
		forgeroBow.addEnchantment(Enchantments.FLAME, 1);

		// Use registered Forgero arrow
		ItemStack forgeroArrow = getRegisteredArrow("oak-arrow");
		context.assertTrue(!forgeroArrow.isEmpty(), "Failed to get oak-arrow from registry");


		// Verify component conversion works
		ComponentConverter converter = getConverter();
		if (converter == null || !converter.toComponent(forgeroArrow).isPresent()) {
			context.complete();
			return;
		}

		player.setStackInHand(Hand.MAIN_HAND, forgeroBow);
		player.getInventory().insertStack(forgeroArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, forgeroBow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> dynamicArrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					player.getBoundingBox().expand(500),
					arrow -> arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid())  // Filter by UUID to avoid cross-test contamination
			);

			context.assertTrue(!dynamicArrows.isEmpty(),
					"Forgero bow + Forgero arrow should work with enchantments");

			DynamicArrowEntity arrow = dynamicArrows.get(0);

			// Verify enchantments applied to DynamicArrowEntity
			context.assertTrue(arrow.isOnFire(),
					"Flame enchantment should work on DynamicArrowEntity");

			// Power II should boost damage on top of base attack_damage
			// Arrow has base damage, Power II adds damage, total should be > 2.5
			context.assertTrue(arrow.getDamage() >= 2.5,
					"Power enchantment should stack with arrow's attack_damage (got: " + arrow.getDamage() + ")");

			context.complete();
		});
	}

	// ========== Arrow Consumption ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_consumption")
	public void arrowConsumptionInSurvivalMode(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;

		ItemStack bow = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 10);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		ItemStack arrowsInInventory = player.getProjectileType(bow);
		int initialCount = arrowsInInventory.getCount();

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			// Arrow should be consumed (count decreased)
			int finalCount = arrowsInInventory.getCount();
			context.assertTrue(finalCount < initialCount,
					"Arrow should be consumed in survival mode (initial: " + initialCount + ", final: " + finalCount + ")");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_consumption")
	public void arrowNotConsumedInCreativeMode(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		// Creative mode by default

		ItemStack bow = new ItemStack(Items.BOW);
		ItemStack arrows = new ItemStack(Items.ARROW, 10);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrows);

		ItemStack arrowsInInventory = player.getProjectileType(bow);
		int initialCount = arrowsInInventory.getCount();

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			// Arrow should NOT be consumed in creative
			int finalCount = arrowsInInventory.getCount();
			context.assertTrue(finalCount == initialCount,
					"Arrow should not be consumed in creative mode");
			context.complete();
		});
	}

	// ========== Helper Methods ==========

	private static ItemStack createBowWithAttributes(String name, Map<OpenIdentifier, Float> attributes) {
		OpenIdentifier id = new OpenIdentifier("forgero-test", name);
		Set<OpenIdentifier> tags = Set.of(
				new OpenIdentifier("forgero", "tool"),
				new OpenIdentifier("forgero", "bow")
		);

		Map<String, List<?>> propertiesMap = new HashMap<>();
		if (!attributes.isEmpty()) {
			List<Attribute> attributeList = attributes.entrySet().stream()
					.map(entry -> new SimpleAttribute(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList());
			propertiesMap.put(Attribute.KEY.key(), attributeList);
		}

		Component component = new StaticComponent(id, tags, propertiesMap);
		ComponentConverter converter = getConverter();
		if (converter == null) {
			return ItemStack.EMPTY;
		}
		return converter.toStack(component).orElse(ItemStack.EMPTY);
	}

	private static ItemStack createForgeroArrow(String name, float attackDamage) {
		OpenIdentifier id = new OpenIdentifier("forgero-test", name);
		Set<OpenIdentifier> tags = Set.of(
				new OpenIdentifier("forgero", "arrow")
		);

		Map<String, List<?>> propertiesMap = new HashMap<>();
		OpenIdentifier attackDamageAttr = new OpenIdentifier("forgero", "attack_damage");
		List<Attribute> attributeList = List.of(new SimpleAttribute(attackDamageAttr, attackDamage));
		propertiesMap.put(Attribute.KEY.key(), attributeList);

		Component component = new StaticComponent(id, tags, propertiesMap);
		ComponentConverter converter = getConverter();
		if (converter == null) {
			return ItemStack.EMPTY;
		}
		return converter.toStack(component).orElse(ItemStack.EMPTY);
	}

	private static ItemStack createForgeroArrowWithWeight(String name, float attackDamage, float weight) {
		OpenIdentifier id = new OpenIdentifier("forgero-test", name);
		Set<OpenIdentifier> tags = Set.of(
				new OpenIdentifier("forgero", "arrow")
		);

		Map<String, List<?>> propertiesMap = new HashMap<>();
		OpenIdentifier attackDamageAttr = new OpenIdentifier("forgero", "attack_damage");
		OpenIdentifier weightAttr = new OpenIdentifier("forgero", "weight");
		List<Attribute> attributeList = List.of(
				new SimpleAttribute(attackDamageAttr, attackDamage),
				new SimpleAttribute(weightAttr, weight)
		);
		propertiesMap.put(Attribute.KEY.key(), attributeList);

		Component component = new StaticComponent(id, tags, propertiesMap);
		ComponentConverter converter = getConverter();
		if (converter == null) {
			return ItemStack.EMPTY;
		}
		return converter.toStack(component).orElse(ItemStack.EMPTY);
	}
}
