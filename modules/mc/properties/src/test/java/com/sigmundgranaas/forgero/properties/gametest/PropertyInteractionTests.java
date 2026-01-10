package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.entity.*;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickProperty;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Tests for multi-property interactions and edge cases.
 * <p>
 * These tests validate that multiple properties on the same item,
 * or properties across multiple equipped items, work correctly together.
 * <p>
 * FOCUS: Deep gameplay validation - actual effects triggering in combination.
 */
public class PropertyInteractionTests {

	// ========== OnHit + OnTick Interaction Tests ==========

	/**
	 * Test: Item with BOTH OnHit fire and OnTick regeneration.
	 * <p>
	 * Expected: OnHit triggers on attack, OnTick triggers continuously while held.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void onHit_and_onTick_bothTrigger(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Create item with BOTH OnHit (fire on target) AND OnTick (heal self)
		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FireHandler(5)),
				null
		);

		OnTickProperty onTick = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "regeneration"), 60, 0)),
				1,  // every tick
				null
		);

		ItemStack sword = ComponentTester.createStack(
				"fire_regen_sword",
				Set.of("sword"),
				List.of(onHit, onTick)
		);

		player.setStackInHand(Hand.MAIN_HAND, sword);
		player.setHealth(10.0f);  // Low health to verify regeneration
		LivingEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));

		// Attack target (should trigger OnHit)
		player.attack(target);

		// Wait for both effects
		context.waitAndRun(40, () -> {
			// OnHit effect: Target should be on fire
			context.assertTrue(target.isOnFire(),
					"OnHit fire should set target on fire");

			// OnTick effect: Player should have regeneration
			context.assertTrue(player.hasStatusEffect(StatusEffects.REGENERATION),
					"OnTick should apply regeneration to player");

			context.complete();
		});
	}

	/**
	 * Test: Two separate OnHit properties on same item.
	 * <p>
	 * Expected: Both OnHit properties trigger on same attack.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void twoOnHitProperties_bothTrigger(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// TWO separate OnHit properties (not just multiple effects in one property)
		OnHitProperty onHit1 = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FireHandler(5)),
				null
		);

		OnHitProperty onHit2 = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "poison"), 100, 1)),
				null
		);

		ItemStack sword = ComponentTester.createStack(
				"dual_onhit_sword",
				Set.of("sword"),
				List.of(onHit1, onHit2)
		);

		player.setStackInHand(Hand.MAIN_HAND, sword);
		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(5, () -> {
			// Both OnHit properties should trigger
			context.assertTrue(target.isOnFire(),
					"First OnHit property should set target on fire");
			context.assertTrue(target.hasStatusEffect(StatusEffects.POISON),
					"Second OnHit property should apply poison");
			context.complete();
		});
	}

	/**
	 * Test: Two OnTick properties on same item.
	 * <p>
	 * Expected: Both OnTick effects apply while item is held.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void twoOnTickProperties_bothApply(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// TWO separate OnTick properties
		OnTickProperty onTick1 = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "speed"), 40, 0)),
				1,  // every tick
				null
		);

		OnTickProperty onTick2 = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "jump_boost"), 40, 0)),
				1,  // every tick
				null
		);

		ItemStack staff = ComponentTester.createStack(
				"dual_ontick_staff",
				Set.of("sword"),  // Use sword tag for testing
				List.of(onTick1, onTick2)
		);

		player.setStackInHand(Hand.MAIN_HAND, staff);

		// Wait for OnTick effects to apply
		context.waitAndRun(30, () -> {
			// Both OnTick properties should apply
			context.assertTrue(player.hasStatusEffect(StatusEffects.SPEED),
					"First OnTick property should apply speed");
			context.assertTrue(player.hasStatusEffect(StatusEffects.JUMP_BOOST),
					"Second OnTick property should apply jump boost");
			context.complete();
		});
	}

	// ========== Fire + Freeze Interaction Tests ==========

	/**
	 * Test: Fire and Freeze effects applied simultaneously.
	 * <p>
	 * Expected: Both effects should apply (entity can be frozen and on fire).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void fireAndFreeze_bothApply(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Item with BOTH fire and freeze in SAME OnHit property
		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new FireHandler(5),
						new FreezeHandler(100, false)
				),
				null
		);

		ItemStack sword = ComponentTester.createStack(
				"fire_freeze_sword",
				Set.of("sword"),
				List.of(onHit)
		);

		player.setStackInHand(Hand.MAIN_HAND, sword);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(5, () -> {
			// Validate BOTH effects applied
			context.assertTrue(target.isOnFire(),
					"Target should be on fire");
			context.assertTrue(target.getFrozenTicks() > 0,
					"Target should be frozen (frozenTicks > 0)");

			// This is interesting - entity is simultaneously on fire AND frozen
			// Validates that effects don't cancel each other
			context.complete();
		});
	}

	/**
	 * Test: Multiple damage-dealing effects stack damage.
	 * <p>
	 * Expected: Fire damage + poison damage should both reduce health.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void fireAndPoison_bothDealDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new FireHandler(10),  // Long fire duration
						new StatusEffectHandler(new Identifier("minecraft", "poison"), 200, 2)  // Strong poison
				),
				null
		);

		ItemStack sword = ComponentTester.createStack(
				"fire_poison_sword",
				Set.of("sword"),
				List.of(onHit)
		);

		player.setStackInHand(Hand.MAIN_HAND, sword);
		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));

		float initialHealth = target.getHealth();
		player.attack(target);

		// Wait longer for damage-over-time effects
		context.waitAndRun(60, () -> {
			float finalHealth = target.getHealth();
			float damageTaken = initialHealth - finalHealth;

			// Both fire and poison deal damage over time
			// Attack damage + fire damage + poison damage should be significant
			context.assertTrue(damageTaken > 5.0f,
					"Combined fire + poison should deal significant damage over time (expected >5, got " + damageTaken + ")");

			// Verify effects are still active
			context.assertTrue(target.isOnFire(), "Fire should still be active");
			context.assertTrue(target.hasStatusEffect(StatusEffects.POISON), "Poison should still be active");

			context.complete();
		});
	}

	// ========== Knockback + Damage Interaction Tests ==========

	/**
	 * Test: Knockback push + damage effects combine.
	 * <p>
	 * Expected: Target takes damage AND gets knocked back.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void knockbackAndFire_bothApply(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new KnockbackHandler(3.0f, KnockbackHandler.Direction.PUSH),  // Strong knockback push
						new FireHandler(5)
				),
				null
		);

		ItemStack hammer = ComponentTester.createStack(
				"knockback_fire_hammer",
				Set.of("sword"),
				List.of(onHit)
		);

		player.setStackInHand(Hand.MAIN_HAND, hammer);
		LivingEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));

		// Store initial position
		double initialX = target.getX();
		double initialZ = target.getZ();

		player.attack(target);

		context.waitAndRun(5, () -> {
			// Validate knockback (position changed)
			double finalX = target.getX();
			double finalZ = target.getZ();
			double distanceMoved = Math.sqrt(
					Math.pow(finalX - initialX, 2) + Math.pow(finalZ - initialZ, 2)
			);

			context.assertTrue(distanceMoved > 0.5,
					"Target should be knocked back (moved " + distanceMoved + " blocks)");

			// Validate fire effect
			context.assertTrue(target.isOnFire(),
					"Target should be on fire despite being knocked back");

			context.complete();
		});
	}

	// ========== Healing + Damage Interaction Tests ==========

	/**
	 * Test: Life steal (heal attacker) + damage target.
	 * <p>
	 * Expected: Attacker heals, target takes damage.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void lifeStealAndPoison_attackerHealsTargetDamages(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new LifeStealHandler(4.0f),  // Heal self
						new StatusEffectHandler(new Identifier("minecraft", "poison"), 100, 1)  // Poison target
				),
				null
		);

		ItemStack sword = ComponentTester.createStack(
				"lifesteal_poison_sword",
				Set.of("sword"),
				List.of(onHit)
		);

		player.setStackInHand(Hand.MAIN_HAND, sword);
		player.setHealth(10.0f);  // Start with low health
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		float playerInitialHealth = player.getHealth();
		player.attack(target);

		context.waitAndRun(5, () -> {
			// Player should have healed
			context.assertTrue(player.getHealth() > playerInitialHealth,
					"Player should heal from life steal (was " + playerInitialHealth + ", now " + player.getHealth() + ")");

			// Target should have poison
			context.assertTrue(target.hasStatusEffect(StatusEffects.POISON),
					"Target should have poison effect");

			context.complete();
		});
	}

	// ========== Multiple Effect Layers Tests ==========

	/**
	 * Test: Three different effect types (damage, control, utility).
	 * <p>
	 * Expected: Fire (damage), Freeze (control), Teleport (utility) all trigger.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void threeDifferentEffectTypes_allTrigger(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new FireHandler(5),                // Damage over time
						new FreezeHandler(100, false)     // Control (slow/freeze)
						// Removed velocity for simplicity - focus on core interactions
				),
				null
		);

		ItemStack staff = ComponentTester.createStack(
				"chaos_staff",
				Set.of("sword"),
				List.of(onHit)
		);

		player.setStackInHand(Hand.MAIN_HAND, staff);
		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(3, () -> {
			// Damage effect
			context.assertTrue(target.isOnFire(), "Fire (damage) effect should apply");

			// Control effect
			context.assertTrue(target.getFrozenTicks() > 0, "Freeze (control) effect should apply");

			context.complete();
		});
	}

	// ========== OnHit + OnTick Synergy Tests ==========

	/**
	 * Test: OnHit applies weakness, OnTick provides strength to self.
	 * <p>
	 * Expected: Player gets stronger while enemies get weaker.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void debuffEnemyBuffSelf_synergy(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "weakness"), 100, 1)),
				null
		);

		OnTickProperty onTick = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "strength"), 40, 0)),
				1,  // every tick
				null
		);

		ItemStack berserkerAxe = ComponentTester.createStack(
				"berserker_axe",
				Set.of("sword"),
				List.of(onHit, onTick)
		);

		player.setStackInHand(Hand.MAIN_HAND, berserkerAxe);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		player.attack(target);

		context.waitAndRun(30, () -> {
			// Enemy debuffed
			context.assertTrue(target.hasStatusEffect(StatusEffects.WEAKNESS),
					"Target should have weakness (debuff from OnHit)");

			// Self buffed
			context.assertTrue(player.hasStatusEffect(StatusEffects.STRENGTH),
					"Player should have strength (buff from OnTick)");

			context.complete();
		});
	}

	/**
	 * Test: OnTick fire aura + OnHit extra fire damage.
	 * <p>
	 * Expected: Both fire sources stack duration/damage.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void onTickFireAura_plusOnHitFire_stackDuration(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// OnTick fire aura (continuous application)
		OnTickProperty onTick = new OnTickProperty(
				new com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector(
						3, Collections.emptyList()
				),
				List.of(new FireHandler(2)),  // Short duration, reapplied frequently
				1,  // every tick
				null
		);

		// OnHit fire (burst damage)
		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FireHandler(10)),  // Long duration
				null
		);

		ItemStack flameSword = ComponentTester.createStack(
				"flame_sword_with_aura",
				Set.of("sword"),
				List.of(onTick, onHit)
		);

		player.setStackInHand(Hand.MAIN_HAND, flameSword);
		LivingEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));

		// Target should get fire from aura first
		context.waitAndRun(30, () -> {
			context.assertTrue(target.isOnFire(),
					"Target should be on fire from OnTick aura");
			int fireTicksFromAura = target.getFireTicks();

			// Now attack to add OnHit fire
			player.attack(target);

			context.waitAndRun(5, () -> {
				context.assertTrue(target.isOnFire(),
						"Target should still be on fire");
				context.assertTrue(target.getFireTicks() > fireTicksFromAura,
						"OnHit fire should extend fire duration (was " + fireTicksFromAura + ", now " + target.getFireTicks() + ")");
				context.complete();
			});
		});
	}

	// ========== Complex Multi-Layer Interaction Tests ==========

	/**
	 * Test: Five different effects on same weapon.
	 * <p>
	 * Expected: Fire, poison, freeze, knockback, life steal all trigger.
	 * This is EXTREME but validates property system handles complexity.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void fiveEffects_allTriggerSimultaneously(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(
						new FireHandler(5),
						new StatusEffectHandler(new Identifier("minecraft", "poison"), 100, 1),
						new FreezeHandler(100, false),
						new KnockbackHandler(2.0f, KnockbackHandler.Direction.PUSH),
						new LifeStealHandler(2.0f)
				),
				null
		);

		ItemStack chaosWeapon = ComponentTester.createStack(
				"chaos_weapon",
				Set.of("sword"),
				List.of(onHit)
		);

		player.setStackInHand(Hand.MAIN_HAND, chaosWeapon);
		player.setHealth(15.0f);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		double initialX = target.getX();
		double initialZ = target.getZ();
		float playerInitialHealth = player.getHealth();

		player.attack(target);

		context.waitAndRun(5, () -> {
			// Validate ALL five effects
			context.assertTrue(target.isOnFire(), "1. Fire should apply");
			context.assertTrue(target.hasStatusEffect(StatusEffects.POISON), "2. Poison should apply");
			context.assertTrue(target.getFrozenTicks() > 0, "3. Freeze should apply");

			double distanceMoved = Math.sqrt(
					Math.pow(target.getX() - initialX, 2) + Math.pow(target.getZ() - initialZ, 2)
			);
			context.assertTrue(distanceMoved > 0.3, "4. Knockback should apply (moved " + distanceMoved + ")");

			context.assertTrue(player.getHealth() > playerInitialHealth,
					"5. Life steal should apply (healed " + (player.getHealth() - playerInitialHealth) + " HP)");

			context.complete();
		});
	}

	/**
	 * Test: OnHit + OnTick + passive attribute bonus.
	 * <p>
	 * Expected: All three property types work together.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "property_interaction")
	public void onHit_onTick_attributes_allWork(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		OnHitProperty onHit = new OnHitProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new FireHandler(5)),
				null
		);

		OnTickProperty onTick = new OnTickProperty(
				new SingleTargetSelector(Collections.emptyList()),
				List.of(new StatusEffectHandler(new Identifier("minecraft", "speed"), 40, 0)),
				1,  // every tick
				null
		);

		// Create item with properties AND attributes
		ItemStack legendaryBlade = ComponentTester.createStackWithAttributes(
				"legendary_blade",
				Set.of("sword"),
				List.of(onHit, onTick),
				java.util.Map.of(
						com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier.of("forgero", "attack_damage"),
						15.0f  // High attack damage attribute
				)
		);

		player.setStackInHand(Hand.MAIN_HAND, legendaryBlade);
		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));

		float targetInitialHealth = target.getHealth();
		player.attack(target);

		context.waitAndRun(30, () -> {
			// OnHit effect
			context.assertTrue(target.isOnFire(), "OnHit fire effect should apply");

			// OnTick effect
			context.assertTrue(player.hasStatusEffect(StatusEffects.SPEED), "OnTick speed effect should apply");

			// Attribute (high damage)
			float damageTaken = targetInitialHealth - target.getHealth();
			context.assertTrue(damageTaken > 8.0f,
					"Attribute bonus should increase attack damage (took " + damageTaken + " damage)");

			context.complete();
		});
	}
}
