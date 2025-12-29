package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.entity.*;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingProperty;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.FilterWrapper;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.Instant;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.PatternSelector;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.SingleSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickManager;
import com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickProperty;
import com.sigmundgranaas.forgero.properties.minecraft.swing.SwingHandManager;
import com.sigmundgranaas.forgero.properties.minecraft.swing.SwingHandProperty;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Integration tests for property handlers using actual player actions.
 *
 * These tests verify that when a player performs an action (attack, swing, mine, etc.)
 * with a Forgero item that has properties attached, the effects actually trigger
 * through the full mixin-property-handler pipeline.
 *
 * <p>Unlike unit tests that call handlers directly, these tests:</p>
 * <ul>
 *   <li>Create a Forgero item with properties</li>
 *   <li>Give the item to a test player</li>
 *   <li>Have the player perform the triggering action</li>
 *   <li>Verify the effects actually applied to targets</li>
 * </ul>
 */
public class PropertyIntegrationGametest {

	// ========== OnHit Fire Effect Tests ==========

	/**
	 * Tests that a player attacking with a fire-enchanted sword sets the target on fire.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitFireIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create item with fire OnHit effect
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FireHandler(5)), // 5 seconds
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"fire_sword",
				Set.of("sword"),
				List.of(property)
		);
		context.assertTrue(!stack.isEmpty(), "Stack should not be empty");

		// Setup
		player.setStackInHand(Hand.MAIN_HAND, stack);
		PigEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));
		context.assertTrue(!target.isOnFire(), "Target should not be on fire initially");

		// Action: Player attacks target
		player.attack(target);

		// Verify effect triggered
		context.waitAndRun(3, () -> {
			context.assertTrue(target.isOnFire(), "Target should be on fire after attack");
			context.assertTrue(target.getFireTicks() > 0, "Target should have fire ticks");
			context.complete();
		});
	}

	// ========== OnHit Status Effect Tests ==========

	/**
	 * Tests that a player attacking with a poison weapon applies the status effect.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitPoisonIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create item with poison OnHit effect
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "poison"), 100, 1)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"poison_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		CowEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));

		// Attack
		player.attack(target);

		context.waitAndRun(3, () -> {
			context.assertTrue(target.hasStatusEffect(StatusEffects.POISON),
					"Target should have poison effect after attack");
			context.complete();
		});
	}

	/**
	 * Tests slowness effect application through OnHit.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitSlownessIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "slowness"), 200, 2)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"slowness_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(3, () -> {
			context.assertTrue(target.hasStatusEffect(StatusEffects.SLOWNESS),
					"Target should have slowness effect");
			var effect = target.getStatusEffect(StatusEffects.SLOWNESS);
			context.assertTrue(effect != null && effect.getAmplifier() == 2,
					"Slowness amplifier should be 2");
			context.complete();
		});
	}

	// ========== OnHit Knockback Tests ==========

	/**
	 * Tests that knockback effect pushes target away from player.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitKnockbackPushIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.teleport(context.getAbsolutePos(new BlockPos(0, 1, 0)).getX(),
				context.getAbsolutePos(new BlockPos(0, 1, 0)).getY(),
				context.getAbsolutePos(new BlockPos(0, 1, 0)).getZ());

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new KnockbackHandler(3.0f, KnockbackHandler.Direction.PUSH)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"knockback_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
		target.setVelocity(Vec3d.ZERO);
		Vec3d initialPos = target.getPos();

		player.attack(target);

		context.waitAndRun(5, () -> {
			double distance = target.getPos().distanceTo(initialPos);
			context.assertTrue(distance > 0.5, "Target should have been pushed away, moved " + distance);
			context.complete();
		});
	}

	/**
	 * Tests that knockback effect pulls target toward player.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitKnockbackPullIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.teleport(context.getAbsolutePos(new BlockPos(0, 1, 0)).getX(),
				context.getAbsolutePos(new BlockPos(0, 1, 0)).getY(),
				context.getAbsolutePos(new BlockPos(0, 1, 0)).getZ());

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new KnockbackHandler(2.0f, KnockbackHandler.Direction.PULL)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"pull_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 0));
		target.setVelocity(Vec3d.ZERO);

		double initialDist = target.getPos().distanceTo(player.getPos());
		player.attack(target);

		context.waitAndRun(5, () -> {
			// Target should have velocity toward player
			context.assertTrue(target.getVelocity().lengthSquared() > 0.01,
					"Target should have velocity after being pulled");
			context.complete();
		});
	}

	// ========== OnHit Life Steal Tests ==========

	/**
	 * Tests that life steal heals the attacker when hitting a target.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitLifeStealIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setHealth(10.0f); // Damage player first
		float initialPlayerHealth = player.getHealth();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new LifeStealHandler(5.0f)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"vampiric_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		float initialTargetHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(3, () -> {
			context.assertTrue(target.getHealth() < initialTargetHealth,
					"Target should have lost health");
			context.assertTrue(player.getHealth() > initialPlayerHealth,
					"Player should have gained health from life steal");
			context.complete();
		});
	}

	// ========== OnHit Lightning Tests ==========

	/**
	 * Tests that lightning effect summons a lightning bolt at target location.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitLightningIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new LightningHandler()),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"lightning_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(3, () -> {
			context.expectEntity(EntityType.LIGHTNING_BOLT);
			context.complete();
		});
	}

	// ========== OnHit Freeze Tests ==========

	/**
	 * Tests that freeze effect applies frozen ticks to target.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitFreezeIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FreezeHandler(200, false)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"freeze_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		context.assertTrue(target.getFrozenTicks() == 0, "Target should not be frozen initially");

		player.attack(target);

		context.waitAndRun(3, () -> {
			context.assertTrue(target.getFrozenTicks() > 0,
					"Target should be frozen after attack");
			context.complete();
		});
	}

	// ========== OnHit Disarm Tests ==========

	/**
	 * Tests that disarm effect drops the target's weapon.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitDisarmIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new DisarmHandler()),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"disarm_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		target.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
		context.assertTrue(!target.getMainHandStack().isEmpty(), "Target should be holding a weapon");

		player.attack(target);

		context.waitAndRun(5, () -> {
			context.assertTrue(target.getMainHandStack().isEmpty(),
					"Target should have dropped their weapon");
			context.expectEntity(EntityType.ITEM);
			context.complete();
		});
	}

	// ========== OnHit Convert Tests ==========

	/**
	 * Tests that convert effect changes entity type.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitConvertIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new ConvertHandler(new Identifier("minecraft", "zombie"))),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"convert_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		// Spawn a villager to convert to zombie
		context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 1, 2));

		player.attack(context.getWorld().getEntitiesByType(EntityType.VILLAGER,
				Box.of(context.getAbsolutePos(new BlockPos(2, 1, 2)).toCenterPos(), 2, 2, 2),
				e -> true).get(0));

		context.waitAndRun(5, () -> {
			context.expectEntity(EntityType.ZOMBIE);
			context.complete();
		});
	}

	// ========== OnHit Spawn Entity Tests ==========

	/**
	 * Tests that spawn entity effect creates entities at target location.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitSpawnEntityIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new SpawnEntityHandler(
						new Identifier("minecraft", "chicken"),
						3, // count
						Vec3d.ZERO,
						true, // at target
						false, // hostile to target
						true // persistent
				)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"spawn_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(5, () -> {
			var chickens = context.getWorld().getEntitiesByType(EntityType.CHICKEN,
					Box.of(target.getPos(), 10, 10, 10), e -> true);
			context.assertTrue(chickens.size() >= 3,
					"Should have spawned at least 3 chickens, found " + chickens.size());
			context.complete();
		});
	}

	// ========== Multiple Effects Tests ==========

	/**
	 * Tests that multiple effects on the same property all trigger.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitMultipleEffectsIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Item with fire + poison + freeze
		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new FireHandler(3),
						new StatusEffectHandler(new Identifier("minecraft", "poison"), 100, 1),
						new FreezeHandler(100, false)
				),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"multi_effect_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(5, () -> {
			context.assertTrue(target.isOnFire(), "Target should be on fire");
			context.assertTrue(target.hasStatusEffect(StatusEffects.POISON), "Target should have poison");
			context.assertTrue(target.getFrozenTicks() > 0, "Target should be frozen");
			context.complete();
		});
	}

	// ========== AOE Selector Tests ==========

	/**
	 * Tests that AOE selector affects multiple nearby entities.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitAoeIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// AOE fire effect
		OnHitProperty property = new OnHitProperty(
				new AreaOfEffectSelector(5, Collections.emptyList()), // 5 block radius
				List.of(new FireHandler(5)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"aoe_fire_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Spawn multiple targets in range
		LivingEntity target1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		LivingEntity target2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 1, 3));
		LivingEntity target3 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 3));

		// Attack one target, AOE should hit all
		player.attack(target1);

		context.waitAndRun(5, () -> {
			context.assertTrue(target1.isOnFire(), "Primary target should be on fire");
			context.assertTrue(target2.isOnFire(), "Nearby target 2 should be on fire from AOE");
			context.assertTrue(target3.isOnFire(), "Nearby target 3 should be on fire from AOE");
			context.complete();
		});
	}

	// ========== Velocity Handler Tests ==========

	/**
	 * Tests that velocity handler launches the target upward.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitVelocityLaunchIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new VelocityHandler(
						VelocityHandler.VelocityTarget.TARGET,
						0.5,
						VelocityHandler.VelocityMode.ADD,
						2.0 // vertical bias (launch up)
				)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"launch_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		target.setVelocity(Vec3d.ZERO);

		player.attack(target);

		context.waitAndRun(3, () -> {
			context.assertTrue(target.getVelocity().y > 0.5,
					"Target should have upward velocity, has " + target.getVelocity().y);
			context.complete();
		});
	}

	/**
	 * Tests that velocity handler dashes the player forward.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitVelocityDashSelfIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setVelocity(Vec3d.ZERO);
		Vec3d initialPos = player.getPos();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new VelocityHandler(
						VelocityHandler.VelocityTarget.SELF,
						3.0,
						VelocityHandler.VelocityMode.ADD,
						0.0
				)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"dash_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(3, () -> {
			context.assertTrue(player.getVelocity().lengthSquared() > 0.1,
					"Player should have velocity after dash");
			context.complete();
		});
	}

	// ========== Teleport Handler Tests ==========

	/**
	 * Tests that teleport handler moves the target randomly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitTeleportTargetIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new TeleportHandler("target", true, true, 5)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"teleport_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		Vec3d initialPos = target.getPos();

		player.attack(target);

		context.waitAndRun(5, () -> {
			double distance = target.getPos().distanceTo(initialPos);
			context.assertTrue(distance > 0.1, "Target should have teleported, moved " + distance);
			context.complete();
		});
	}

	// ========== Edge Cases ==========

	/**
	 * Tests that effects don't trigger with empty hand.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testNoEffectWithEmptyHand(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(3, () -> {
			context.assertTrue(!target.isOnFire(), "Target should not be on fire with empty hand");
			context.complete();
		});
	}

	/**
	 * Tests that effects don't trigger with vanilla items.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testNoEffectWithVanillaItem(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));

		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		int initialFrozenTicks = target.getFrozenTicks();

		player.attack(target);

		context.waitAndRun(3, () -> {
			// Vanilla sword shouldn't add freeze or fire
			context.assertTrue(target.getFrozenTicks() == initialFrozenTicks,
					"Target frozen ticks should be unchanged with vanilla item");
			context.complete();
		});
	}

	// ========== OnTick Property Tests ==========

	/**
	 * Tests that OnTick property with status effect applies periodically to holder.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnTickSelfStatusEffectIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create item with OnTick property that applies regeneration to self
		OnTickProperty property = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()), // targets self
				List.of(new StatusEffectHandler(new Identifier("minecraft", "regeneration"), 40, 0)),
				1, // every tick
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"regen_held_item",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		context.assertTrue(!player.hasStatusEffect(StatusEffects.REGENERATION),
				"Player should not have regeneration initially");

		// Manually trigger the OnTick manager since entity ticking may not work in gametest
		OnTickManager.handle(player);

		context.waitAndRun(3, () -> {
			context.assertTrue(player.hasStatusEffect(StatusEffects.REGENERATION),
					"Player should have regeneration after OnTick");
			context.complete();
		});
	}

	/**
	 * Tests that OnTick property with AOE affects nearby entities.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnTickAoeSlownessIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.teleport(context.getAbsolutePos(new BlockPos(5, 1, 5)).getX(),
				context.getAbsolutePos(new BlockPos(5, 1, 5)).getY(),
				context.getAbsolutePos(new BlockPos(5, 1, 5)).getZ());

		// Create item with OnTick AOE slowness aura
		OnTickProperty property = new OnTickProperty(
				new AreaOfEffectSelector(5, Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "slowness"), 40, 0)),
				1, // every tick
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"aura_slowness_item",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Spawn nearby enemies
		LivingEntity zombie1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
		LivingEntity zombie2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 6));

		// Manually trigger OnTick
		OnTickManager.handle(player);

		context.waitAndRun(3, () -> {
			context.assertTrue(zombie1.hasStatusEffect(StatusEffects.SLOWNESS),
					"Nearby zombie 1 should have slowness from aura");
			context.assertTrue(zombie2.hasStatusEffect(StatusEffects.SLOWNESS),
					"Nearby zombie 2 should have slowness from aura");
			context.complete();
		});
	}

	/**
	 * Tests that OnTick property with fire effect sets nearby enemies on fire.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnTickFireAuraIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnTickProperty property = new OnTickProperty(
				new AreaOfEffectSelector(3, Collections.emptyList()),
				List.of(new FireHandler(3)),
				1,
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"fire_aura_item",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));
		context.assertTrue(!target.isOnFire(), "Target should not be on fire initially");

		// Trigger OnTick
		OnTickManager.handle(player);

		context.waitAndRun(3, () -> {
			context.assertTrue(target.isOnFire(), "Nearby target should be on fire from aura");
			context.complete();
		});
	}

	// ========== SwingHand Property Tests ==========

	/**
	 * Tests that SwingHand property triggers on player swing.
	 * Note: Swing effects are typically sounds/particles, so we verify no crash and the manager processes.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSwingHandPropertyIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		SwingHandProperty property = new SwingHandProperty(
				List.of(
						new SwingSoundEffect("minecraft:entity.player.attack.sweep", 1.0f, 1.0f),
						new SwingParticleEffect("minecraft:sweep_attack", 5, 0.3)
				),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"swing_effect_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Trigger swing via manager
		SwingHandManager.handleSwing(stack, player, Hand.MAIN_HAND);

		// Verify no crash and complete
		context.complete();
	}

	/**
	 * Tests SwingHand with sound effect.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSwingHandSoundIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		SwingHandProperty property = new SwingHandProperty(
				List.of(new SwingSoundEffect("minecraft:block.anvil.land", 1.0f, 1.2f)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"swing_sound_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Call the swing handler
		SwingHandManager.handleSwing(stack, player, Hand.MAIN_HAND);

		// Sound was played (can't verify sound in gametest, but no crash)
		context.complete();
	}

	// ========== BlockBreaking Property Tests ==========

	/**
	 * Tests 3x3 pattern mining with instant break.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockBreaking3x3PatternIntegration(TestContext context) {
		// Create the dynamic item with an instant-break 3x3 property
		var filter = new FilterWrapper(List.of(new SameBlockFilter()));
		var pattern = new PatternSelector(List.of("xxx", "xcx", "xxx"), 1, "multi", filter);
		var speed = new Instant(false);
		var pickaxeProperty = new BlockBreakingProperty(pattern, speed, null);

		ItemStack pickaxeStack = ComponentTester.createStack(
				"test_pickaxe_3x3_integration",
				Set.of("pickaxe", "tool"),
				List.of(pickaxeProperty)
		);
		context.assertTrue(!pickaxeStack.isEmpty(), "Failed to create dynamic pickaxe stack");

		// Setup world state
		BlockPos centerRelative = new BlockPos(1, 1, 1);
		BlockPos centerAbsolute = context.getAbsolutePos(centerRelative);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				context.setBlockState(centerRelative.add(x, 0, z), Blocks.STONE);
			}
		}

		// Execute the action
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.teleport(centerAbsolute.getX(), centerAbsolute.getY(), centerAbsolute.getZ());
		player.setStackInHand(Hand.MAIN_HAND, pickaxeStack);
		player.setYaw(0);
		player.setPitch(45);

		PlayerActionTestHelper actionHelper = new PlayerActionTestHelper(context, player);
		actionHelper.mineBlock(centerAbsolute);

		// Assert the outcome
		context.waitAndRun(5, () -> {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					context.expectBlock(Blocks.AIR, centerRelative.add(x, 0, z));
				}
			}
			context.complete();
		});
	}

	/**
	 * Tests single block instant break.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBlockBreakingSingleInstantIntegration(TestContext context) {
		var filter = new FilterWrapper(List.of());
		var selector = new SingleSelector();
		var speed = new Instant(false);
		var pickaxeProperty = new BlockBreakingProperty(selector, speed, null);

		ItemStack pickaxeStack = ComponentTester.createStack(
				"test_instant_pickaxe",
				Set.of("pickaxe", "tool"),
				List.of(pickaxeProperty)
		);

		BlockPos blockPos = new BlockPos(2, 1, 2);
		BlockPos absolutePos = context.getAbsolutePos(blockPos);
		context.setBlockState(blockPos, Blocks.OBSIDIAN);

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.teleport(absolutePos.getX(), absolutePos.getY() + 1, absolutePos.getZ());
		player.setStackInHand(Hand.MAIN_HAND, pickaxeStack);

		PlayerActionTestHelper actionHelper = new PlayerActionTestHelper(context, player);
		actionHelper.mineBlock(absolutePos);

		context.waitAndRun(3, () -> {
			context.expectBlock(Blocks.AIR, blockPos);
			context.complete();
		});
	}

	// ========== Combined Properties Tests ==========

	/**
	 * Tests item with both OnHit and OnTick properties.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCombinedOnHitAndOnTickIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setHealth(10.0f);

		// OnHit: Fire effect
		OnHitProperty onHitProperty = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FireHandler(5)),
				null
		);

		// OnTick: Regeneration aura to self
		OnTickProperty onTickProperty = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "regeneration"), 100, 0)),
				1,
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"combined_sword",
				Set.of("sword"),
				List.of(onHitProperty, onTickProperty)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Trigger OnTick
		OnTickManager.handle(player);

		// Attack target
		player.attack(target);

		context.waitAndRun(5, () -> {
			context.assertTrue(target.isOnFire(), "Target should be on fire from OnHit");
			context.assertTrue(player.hasStatusEffect(StatusEffects.REGENERATION),
					"Player should have regeneration from OnTick");
			context.complete();
		});
	}

	/**
	 * Tests item with OnHit and SwingHand properties.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCombinedOnHitAndSwingIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHitProperty = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "poison"), 100, 1)),
				null
		);

		SwingHandProperty swingProperty = new SwingHandProperty(
				List.of(new SwingSoundEffect("minecraft:entity.player.attack.sweep", 1.0f, 1.0f)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"swing_hit_sword",
				Set.of("sword"),
				List.of(onHitProperty, swingProperty)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		// Swing triggers swing effects
		SwingHandManager.handleSwing(stack, player, Hand.MAIN_HAND);

		// Attack triggers OnHit
		player.attack(target);

		context.waitAndRun(5, () -> {
			context.assertTrue(target.hasStatusEffect(StatusEffects.POISON),
					"Target should have poison from OnHit");
			context.complete();
		});
	}

	// ========== Explosion Handler Tests ==========

	/**
	 * Tests that explosion handler creates an explosion at target.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnHitExplosionIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty property = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new ExplosionHandler(2.0f, false, World.ExplosionSourceType.BLOCK)),
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"explosion_sword",
				Set.of("sword"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Place a fragile block near target
		BlockPos blockPos = new BlockPos(2, 1, 3);
		context.setBlockState(blockPos, Blocks.DIRT);

		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(5, () -> {
			// Explosion should have destroyed the dirt block
			context.expectBlock(Blocks.AIR, blockPos);
			context.complete();
		});
	}

	// ========== Magnet Handler Tests ==========

	/**
	 * Tests that magnet handler attracts nearby items.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOnTickMagnetIntegration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.teleport(context.getAbsolutePos(new BlockPos(5, 64, 5)).getX(),
				context.getAbsolutePos(new BlockPos(5, 64, 5)).getY(),
				context.getAbsolutePos(new BlockPos(5, 64, 5)).getZ());

		OnTickProperty property = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()), // Self-targeting for magnet
				List.of(new MagnetHandler(10.0, 0.5)), // 10 block radius
				1,
				null
		);

		ItemStack stack = ComponentTester.createStack(
				"magnet_tool",
				Set.of("tool"),
				List.of(property)
		);

		player.setStackInHand(Hand.MAIN_HAND, stack);

		// Spawn an item nearby
		Vec3d playerPos = player.getPos();
		ItemEntity droppedItem = new ItemEntity(
				context.getWorld(),
				playerPos.x + 3,
				playerPos.y,
				playerPos.z,
				new ItemStack(Items.DIAMOND)
		);
		droppedItem.setNoGravity(true);
		context.getWorld().spawnEntity(droppedItem);

		Vec3d initialItemPos = droppedItem.getPos();

		// Apply magnet effect multiple times
		for (int i = 0; i < 10; i++) {
			OnTickManager.handle(player);
		}

		context.waitAndRun(5, () -> {
			double initialDist = initialItemPos.distanceTo(playerPos);
			double currentDist = droppedItem.getPos().distanceTo(player.getPos());
			context.assertTrue(currentDist < initialDist,
					"Item should be closer to player after magnet effect");
			context.complete();
		});
	}
}
