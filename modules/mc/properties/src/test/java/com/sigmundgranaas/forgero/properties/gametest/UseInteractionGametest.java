package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.ContextualUseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.SimpleUseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionManager;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionProperty;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.ConsumeStackHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.CooldownHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.DamageStackHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers.StartUseHandler;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionPropertiesPlugin;
import com.sigmundgranaas.forgero.effects.entity.SoundHandler;
import com.sigmundgranaas.forgero.effects.entity.ParticleHandler;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;

/**
 * Comprehensive gametests for the UseInteraction system.
 * Tests individual components and their integration through mixins.
 *
 * <p>These tests follow the same patterns as other property gametests
 * in the Forgero project.</p>
 */
public class UseInteractionGametest {

	// ========== UseContext Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseContextStartFactory(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		UseContext ctx = UseContext.start(context.getWorld(), player, Hand.MAIN_HAND, stack);

		context.assertTrue(ctx.world() == context.getWorld(), "World should match");
		context.assertTrue(ctx.user() == player, "User should match");
		context.assertTrue(ctx.hand() == Hand.MAIN_HAND, "Hand should match");
		context.assertTrue(ctx.stack() == stack, "Stack should match");
		context.assertTrue(ctx.chargeTime() == 0, "Charge time should be 0 at start");
		context.assertTrue(ctx.remainingTicks() == 0, "Remaining ticks should be 0 at start");
		context.assertTrue(ctx.pullProgress() == 0f, "Pull progress should be 0 at start");
		context.assertTrue(ctx.target() == null, "Target should be null");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseContextTickFactory(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);

		UseContext ctx = UseContext.tick(context.getWorld(), player, Hand.MAIN_HAND, stack, 10, 90, 0.5f);

		context.assertTrue(ctx.chargeTime() == 10, "Charge time should be 10");
		context.assertTrue(ctx.remainingTicks() == 90, "Remaining ticks should be 90");
		context.assertTrue(ctx.pullProgress() == 0.5f, "Pull progress should be 0.5");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseContextReleaseFactory(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);

		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, stack, 20, 80, 1.0f);

		context.assertTrue(ctx.chargeTime() == 20, "Charge time should be 20");
		context.assertTrue(ctx.remainingTicks() == 80, "Remaining ticks should be 80");
		context.assertTrue(ctx.pullProgress() == 1.0f, "Pull progress should be 1.0");
		context.assertTrue(ctx.isFullyCharged(), "Should be fully charged");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseContextFinishFactory(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);

		UseContext ctx = UseContext.finish(context.getWorld(), player, Hand.MAIN_HAND, stack, 100);

		context.assertTrue(ctx.chargeTime() == 100, "Charge time should equal total use time");
		context.assertTrue(ctx.remainingTicks() == 0, "Remaining ticks should be 0");
		context.assertTrue(ctx.pullProgress() == 1.0f, "Pull progress should be 1.0 at finish");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseContextHelperMethods(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);

		UseContext ctx = UseContext.start(context.getWorld(), player, Hand.MAIN_HAND, stack);

		context.assertTrue(ctx.isServer(), "Should be server side in gametest");
		context.assertFalse(ctx.isClient(), "Should not be client side in gametest");
		context.assertTrue(ctx.asPlayer().isPresent(), "asPlayer should return present for PlayerEntity");
		context.assertTrue(ctx.asPlayer().get() == player, "asPlayer should return correct player");
		context.assertFalse(ctx.isFullyCharged(), "Should not be fully charged at start");
		context.assertTrue(ctx.getTarget().isEmpty(), "Target should be empty");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseContextWithTarget(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		UseContext ctx = UseContext.startWithTarget(context.getWorld(), player, Hand.MAIN_HAND, stack, target);

		context.assertTrue(ctx.getTarget().isPresent(), "Target should be present");
		context.assertTrue(ctx.getTarget().get() == target, "Target should match");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseContextImmutability(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);
		ItemStack newStack = new ItemStack(Items.DIAMOND);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		UseContext original = UseContext.start(context.getWorld(), player, Hand.MAIN_HAND, stack);
		UseContext withNewStack = original.withStack(newStack);
		UseContext withTarget = original.withTarget(target);

		// Original should be unchanged
		context.assertTrue(original.stack() == stack, "Original stack unchanged");
		context.assertTrue(original.target() == null, "Original target unchanged");

		// New contexts should have updates
		context.assertTrue(withNewStack.stack() == newStack, "New context has new stack");
		context.assertTrue(withTarget.getTarget().get() == target, "New context has target");

		context.complete();
	}

	// ========== Individual Handler Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testStartUseHandler(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		StartUseHandler handler = new StartUseHandler();

		// Verify handler type
		context.assertTrue(handler.type().equals("forgero:start_use"), "Type should match");

		// Apply handler
		handler.apply(player, stack, Hand.MAIN_HAND);

		// Handler sets current hand - verify no exception
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testStartUseHandlerNonPlayer(TestContext context) {
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		ItemStack stack = new ItemStack(Items.STICK);

		StartUseHandler handler = new StartUseHandler();

		// Should not throw for non-player
		handler.apply(zombie, stack, Hand.MAIN_HAND);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeStackHandler(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		// Disable creative mode to test consumption
		player.getAbilities().creativeMode = false;
		ItemStack stack = new ItemStack(Items.APPLE, 5);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ConsumeStackHandler handler = new ConsumeStackHandler(2);

		context.assertTrue(stack.getCount() == 5, "Stack count should be 5 before handler");

		handler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.getCount() == 3, "Stack count should be 3 after consuming 2");
		context.assertTrue(handler.type().equals("forgero:consume_stack"), "Type should match");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeStackHandlerCreativeMode(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.APPLE, 5);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ConsumeStackHandler handler = new ConsumeStackHandler(2);

		handler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.getCount() == 5, "Stack count should remain 5 in creative mode");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testDamageStackHandler(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		// Disable creative mode to test damage
		player.getAbilities().creativeMode = false;
		ItemStack stack = new ItemStack(Items.IRON_SWORD);
		int initialDamage = stack.getDamage();
		player.setStackInHand(Hand.MAIN_HAND, stack);

		DamageStackHandler handler = new DamageStackHandler(5);

		handler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.getDamage() > initialDamage, "Stack should be damaged");
		context.assertTrue(handler.type().equals("forgero:damage_stack"), "Type should match");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testDamageStackHandlerCreativeMode(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.IRON_SWORD);
		int initialDamage = stack.getDamage();
		player.setStackInHand(Hand.MAIN_HAND, stack);

		DamageStackHandler handler = new DamageStackHandler(5);

		handler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.getDamage() == initialDamage, "Stack should not be damaged in creative mode");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCooldownHandler(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.ENDER_PEARL);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		CooldownHandler handler = new CooldownHandler(100);

		handler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL), "Item should be on cooldown");
		context.assertTrue(handler.type().equals("forgero:cooldown"), "Type should match");
		context.complete();
	}

	// ========== UseInteractionProperty Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseInteractionPropertyConstruction(TestContext context) {
		List<UseHandler> onStart = List.of(new StartUseHandler());
		List<UseHandler> onRelease = List.of(new DamageStackHandler(1));

		UseInteractionProperty property = new UseInteractionProperty(
				UseAction.BOW,
				72000,
				true,
				onStart,
				List.of(),
				onRelease,
				List.of(),
				null
		);

		context.assertTrue(property.useAction() == UseAction.BOW, "UseAction should be BOW");
		context.assertTrue(property.maxUseTime() == 72000, "MaxUseTime should be 72000");
		context.assertTrue(property.usedOnRelease(), "Should be used on release");
		context.assertTrue(property.hasStartHandlers(), "Should have start handlers");
		context.assertFalse(property.hasTickHandlers(), "Should not have tick handlers");
		context.assertTrue(property.hasReleaseHandlers(), "Should have release handlers");
		context.assertFalse(property.hasFinishHandlers(), "Should not have finish handlers");
		context.assertTrue(property.hasUseAction(), "Should have use action");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseInteractionPropertyNoUseAction(TestContext context) {
		UseInteractionProperty property = new UseInteractionProperty(
				UseAction.NONE,
				0,
				false,
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				null
		);

		context.assertFalse(property.hasUseAction(), "Should not have use action when NONE");
		context.assertFalse(property.hasStartHandlers(), "Should not have start handlers");
		context.complete();
	}

	// ========== Handler Chain Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMultipleHandlersChained(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;
		ItemStack stack = new ItemStack(Items.IRON_SWORD);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Create multiple handlers
		StartUseHandler startHandler = new StartUseHandler();
		DamageStackHandler damageHandler = new DamageStackHandler(1);
		CooldownHandler cooldownHandler = new CooldownHandler(20);

		// Apply all handlers (simulating dispatch)
		startHandler.apply(player, stack, Hand.MAIN_HAND);
		damageHandler.apply(player, stack, Hand.MAIN_HAND);
		cooldownHandler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.getDamage() > 0, "Stack should be damaged");
		context.assertTrue(player.getItemCooldownManager().isCoolingDown(Items.IRON_SWORD), "Should be on cooldown");
		context.complete();
	}

	// ========== Handler Dispatch Pattern Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSimpleUseHandlerDispatch(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);

		UseHandler handler = new StartUseHandler();

		// Verify instanceof dispatch works
		context.assertTrue(handler instanceof SimpleUseHandler, "StartUseHandler should be SimpleUseHandler");
		context.assertFalse(handler instanceof ContextualUseHandler, "StartUseHandler should not be ContextualUseHandler");

		if (handler instanceof SimpleUseHandler simple) {
			simple.apply(player, stack, Hand.MAIN_HAND);
		}

		context.complete();
	}

	// ========== UseInteractionManager Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseInteractionManagerWithEmptyStack(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack emptyStack = ItemStack.EMPTY;
		player.setStackInHand(Hand.MAIN_HAND, emptyStack);

		// Should handle empty stack gracefully
		var result = UseInteractionManager.handleUse(context.getWorld(), player, Hand.MAIN_HAND);

		context.assertTrue(result.getResult() == ActionResult.PASS, "Empty stack should pass");
		context.assertTrue(!UseInteractionManager.hasUseInteraction(emptyStack), "Empty stack should not have use interaction");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testUseInteractionManagerGettersWithVanillaItem(TestContext context) {
		ItemStack vanillaStack = new ItemStack(Items.STICK);

		// Vanilla items without Forgero components should return empty optionals
		var useAction = UseInteractionManager.getUseAction(vanillaStack);
		var maxUseTime = UseInteractionManager.getMaxUseTime(vanillaStack);
		var usedOnRelease = UseInteractionManager.isUsedOnRelease(vanillaStack);

		context.assertTrue(useAction.isEmpty(), "Vanilla item should have empty use action");
		context.assertTrue(maxUseTime.isEmpty(), "Vanilla item should have empty max use time");
		context.assertTrue(usedOnRelease.isEmpty(), "Vanilla item should have empty used on release");
		context.complete();
	}

	// ========== Full Lifecycle Simulation Tests ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBowLikeItemLifecycle(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;
		ItemStack stack = new ItemStack(Items.IRON_SWORD);  // Using sword as stand-in
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Simulate the phases of a bow-like interaction manually

		// Phase 1: Start
		StartUseHandler startHandler = new StartUseHandler();
		startHandler.apply(player, stack, Hand.MAIN_HAND);

		// Phase 2: Tick (multiple times to simulate charging)
		for (int i = 0; i < 20; i++) {
			// In a real scenario, tick handlers would be applied here
		}

		// Phase 3: Release (fully charged)
		DamageStackHandler damageHandler = new DamageStackHandler(1);
		damageHandler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.getDamage() > 0, "Stack should be damaged after release");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumableLikeItemLifecycle(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;
		ItemStack stack = new ItemStack(Items.APPLE, 10);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Simulate consumable lifecycle

		// Phase 1: Start eating
		StartUseHandler startHandler = new StartUseHandler();
		startHandler.apply(player, stack, Hand.MAIN_HAND);

		// Phase 2: Finish eating (after max use time)
		// Apply consume handler
		ConsumeStackHandler consumeHandler = new ConsumeStackHandler(1);
		consumeHandler.apply(player, stack, Hand.MAIN_HAND);

		// Apply cooldown
		CooldownHandler cooldownHandler = new CooldownHandler(10);
		cooldownHandler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.getCount() == 9, "Stack should have 9 items left");
		context.assertTrue(player.getItemCooldownManager().isCoolingDown(Items.APPLE), "Should be on cooldown");

		context.complete();
	}

	// ========== Edge Cases ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConsumeStackToZero(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;
		ItemStack stack = new ItemStack(Items.APPLE, 1);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ConsumeStackHandler handler = new ConsumeStackHandler(1);

		handler.apply(player, stack, Hand.MAIN_HAND);

		context.assertTrue(stack.isEmpty(), "Stack should be empty after consuming last item");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPullProgressCalculation(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);

		// Test partial charge
		UseContext partialCtx = UseContext.tick(context.getWorld(), player, Hand.MAIN_HAND, stack, 10, 90, 0.5f);
		context.assertFalse(partialCtx.isFullyCharged(), "Should not be fully charged at 50%");

		// Test full charge
		UseContext fullCtx = UseContext.tick(context.getWorld(), player, Hand.MAIN_HAND, stack, 20, 80, 1.0f);
		context.assertTrue(fullCtx.isFullyCharged(), "Should be fully charged at 100%");

		// Test over charge (should still be considered full)
		UseContext overCtx = UseContext.tick(context.getWorld(), player, Hand.MAIN_HAND, stack, 30, 70, 1.5f);
		context.assertTrue(overCtx.isFullyCharged(), "Should be fully charged above 100%");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOffHandUse(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack mainStack = new ItemStack(Items.IRON_SWORD);
		ItemStack offStack = new ItemStack(Items.SHIELD);
		player.setStackInHand(Hand.MAIN_HAND, mainStack);
		player.setStackInHand(Hand.OFF_HAND, offStack);

		UseContext mainCtx = UseContext.start(context.getWorld(), player, Hand.MAIN_HAND, mainStack);
		UseContext offCtx = UseContext.start(context.getWorld(), player, Hand.OFF_HAND, offStack);

		context.assertTrue(mainCtx.hand() == Hand.MAIN_HAND, "Main hand context should be MAIN_HAND");
		context.assertTrue(offCtx.hand() == Hand.OFF_HAND, "Off hand context should be OFF_HAND");
		context.assertTrue(mainCtx.stack() == mainStack, "Main context should have sword");
		context.assertTrue(offCtx.stack() == offStack, "Off context should have shield");

		context.complete();
	}

	// ========== Handler Reuse Tests (SoundHandler, ParticleHandler) ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSoundHandlerImplementsSimpleUseHandler(TestContext context) {
		SoundHandler handler = new SoundHandler(
				Identifier.of("minecraft", "entity.arrow.shoot"),
				1.0f,
				1.0f,
				SoundHandler.SoundTarget.SOURCE
		);

		// Verify SoundHandler implements SimpleUseHandler
		context.assertTrue(handler instanceof SimpleUseHandler,
				"SoundHandler should implement SimpleUseHandler");
		context.assertTrue(handler instanceof UseHandler,
				"SoundHandler should implement UseHandler");
		context.assertTrue(handler.type().equals("forgero:sound"),
				"Type should be forgero:sound");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSoundHandlerRegisteredInUseInteraction(TestContext context) {
		// Verify SoundHandler is registered in UseInteractionPropertiesPlugin
		context.assertTrue(UseInteractionPropertiesPlugin.isHandlerRegistered("forgero:sound"),
				"SoundHandler should be registered");

		var codec = UseInteractionPropertiesPlugin.getHandlerCodec("forgero:sound");
		context.assertTrue(codec != null,
				"SoundHandler codec should be retrievable");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSoundHandlerApplyAsSimpleUseHandler(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		SoundHandler handler = new SoundHandler(
				Identifier.of("minecraft", "entity.arrow.shoot"),
				1.0f,
				1.0f,
				SoundHandler.SoundTarget.SOURCE
		);

		// Apply as SimpleUseHandler - should not throw
		handler.apply(player, stack, Hand.MAIN_HAND);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testParticleHandlerImplementsSimpleUseHandler(TestContext context) {
		ParticleHandler handler = new ParticleHandler(
				Identifier.of("minecraft", "flame"),
				10,
				0.1,
				0.5,
				ParticleHandler.ParticleTarget.SOURCE
		);

		// Verify ParticleHandler implements SimpleUseHandler
		context.assertTrue(handler instanceof SimpleUseHandler,
				"ParticleHandler should implement SimpleUseHandler");
		context.assertTrue(handler instanceof UseHandler,
				"ParticleHandler should implement UseHandler");
		context.assertTrue(handler.type().equals("forgero:particle"),
				"Type should be forgero:particle");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testParticleHandlerRegisteredInUseInteraction(TestContext context) {
		// Verify ParticleHandler is registered in UseInteractionPropertiesPlugin
		context.assertTrue(UseInteractionPropertiesPlugin.isHandlerRegistered("forgero:particle"),
				"ParticleHandler should be registered");

		var codec = UseInteractionPropertiesPlugin.getHandlerCodec("forgero:particle");
		context.assertTrue(codec != null,
				"ParticleHandler codec should be retrievable");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testParticleHandlerApplyAsSimpleUseHandler(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		ItemStack stack = new ItemStack(Items.STICK);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		ParticleHandler handler = new ParticleHandler(
				Identifier.of("minecraft", "flame"),
				10,
				0.1,
				0.5,
				ParticleHandler.ParticleTarget.SOURCE
		);

		// Apply as SimpleUseHandler - should not throw
		handler.apply(player, stack, Hand.MAIN_HAND);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSoundHandlerInUseInteractionProperty(TestContext context) {
		// Create a UseInteractionProperty with SoundHandler in release phase
		SoundHandler soundHandler = new SoundHandler(
				Identifier.of("minecraft", "entity.arrow.shoot"),
				1.0f,
				1.0f,
				SoundHandler.SoundTarget.SOURCE
		);

		List<UseHandler> onRelease = List.of(soundHandler);

		UseInteractionProperty property = new UseInteractionProperty(
				UseAction.BOW,
				72000,
				true,
				List.of(),
				List.of(),
				onRelease,
				List.of(),
				null
		);

		context.assertTrue(property.hasReleaseHandlers(), "Should have release handlers");
		context.assertTrue(property.onRelease().size() == 1, "Should have one release handler");
		context.assertTrue(property.onRelease().get(0) instanceof SoundHandler,
				"Release handler should be SoundHandler");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testParticleHandlerInUseInteractionProperty(TestContext context) {
		// Create a UseInteractionProperty with ParticleHandler in release phase
		ParticleHandler particleHandler = new ParticleHandler(
				Identifier.of("minecraft", "flame"),
				20,
				0.1,
				1.0,
				ParticleHandler.ParticleTarget.SOURCE
		);

		List<UseHandler> onRelease = List.of(particleHandler);

		UseInteractionProperty property = new UseInteractionProperty(
				UseAction.BOW,
				72000,
				true,
				List.of(),
				List.of(),
				onRelease,
				List.of(),
				null
		);

		context.assertTrue(property.hasReleaseHandlers(), "Should have release handlers");
		context.assertTrue(property.onRelease().size() == 1, "Should have one release handler");
		context.assertTrue(property.onRelease().get(0) instanceof ParticleHandler,
				"Release handler should be ParticleHandler");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMultipleHandlerTypesInUseInteractionProperty(TestContext context) {
		// Create a UseInteractionProperty with mixed handler types
		StartUseHandler startHandler = new StartUseHandler();
		SoundHandler soundHandler = new SoundHandler(
				Identifier.of("minecraft", "entity.arrow.shoot"),
				1.0f,
				1.0f,
				SoundHandler.SoundTarget.SOURCE
		);
		ParticleHandler particleHandler = new ParticleHandler(
				Identifier.of("minecraft", "flame"),
				10,
				0.1,
				0.5,
				ParticleHandler.ParticleTarget.SOURCE
		);
		DamageStackHandler damageHandler = new DamageStackHandler(1);

		List<UseHandler> onStart = List.of(startHandler);
		List<UseHandler> onRelease = List.of(soundHandler, particleHandler, damageHandler);

		UseInteractionProperty property = new UseInteractionProperty(
				UseAction.BOW,
				72000,
				true,
				onStart,
				List.of(),
				onRelease,
				List.of(),
				null
		);

		context.assertTrue(property.hasStartHandlers(), "Should have start handlers");
		context.assertTrue(property.hasReleaseHandlers(), "Should have release handlers");
		context.assertTrue(property.onRelease().size() == 3, "Should have three release handlers");

		// Verify the composition works with all handler types
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.getAbilities().creativeMode = false;
		ItemStack stack = new ItemStack(Items.IRON_SWORD);
		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Apply all handlers in sequence
		for (UseHandler handler : property.onStart()) {
			if (handler instanceof SimpleUseHandler simple) {
				simple.apply(player, stack, Hand.MAIN_HAND);
			}
		}
		for (UseHandler handler : property.onRelease()) {
			if (handler instanceof SimpleUseHandler simple) {
				simple.apply(player, stack, Hand.MAIN_HAND);
			}
		}

		// Damage handler should have applied
		context.assertTrue(stack.getDamage() > 0, "Stack should be damaged");

		context.complete();
	}
}
