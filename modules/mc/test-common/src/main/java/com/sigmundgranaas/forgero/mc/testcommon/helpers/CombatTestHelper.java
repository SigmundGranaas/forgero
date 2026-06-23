package com.sigmundgranaas.forgero.mc.testcommon.helpers;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Measures REAL melee combat outcomes in a GameTest.
 * <p>
 * Instead of reading an attribute number off an {@link ItemStack}, this drives an actual
 * {@code player.attack(target)} through the vanilla combat path (attack-damage attribute modifiers,
 * cooldown, armor) and returns the health a living target actually lost. Use it to prove a weapon —
 * or an upgrade installed on one — changes damage as experienced in play, not just on paper.
 * <p>
 * All measurements are synchronous (vanilla applies melee damage in the same tick), so callers can
 * compare two weapons directly without juggling {@code waitAndRun} callbacks.
 */
public final class CombatTestHelper {

	/** Ticks needed for every vanilla weapon (slowest = axe at 1.0 speed) to reach a full-strength swing. */
	public static final int FULL_CHARGE_TICKS = 25;

	private CombatTestHelper() {
	}

	/**
	 * Measures the damage {@code weapon} deals with one full-strength hit, using a 20-HP zombie target.
	 *
	 * @return health actually lost by the target
	 */
	public static float measureMeleeDamage(TestContext context, ItemStack weapon) {
		return measureMeleeDamage(context, weapon, EntityType.ZOMBIE);
	}

	/**
	 * Measures the damage {@code weapon} deals with one full-strength hit against a chosen target type.
	 * Pick a target with enough health to survive the blow (the call fails fast if the target dies,
	 * because a dead target only gives a lower bound on damage).
	 *
	 * @return health actually lost by the target
	 */
	public static float measureMeleeDamage(TestContext context, ItemStack weapon,
			EntityType<? extends LivingEntity> targetType) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, weapon);
		// Charge the attack cooldown so the hit lands at 100% (a fresh swing is at ~0%).
		for (int i = 0; i < FULL_CHARGE_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(targetType, new BlockPos(2, 2, 2));
		target.setHealth(target.getMaxHealth());
		float before = target.getHealth();

		player.attack(target);

		float dealt = before - target.getHealth();
		boolean died = target.isDead() || target.getHealth() <= 0;
		target.discard();
		if (died) {
			throw new GameTestException("measureMeleeDamage: target died from the hit (~" + dealt
					+ " dmg vs " + before + " HP); use a higher-HP target type to measure this weapon.");
		}
		return dealt;
	}

	/**
	 * Asserts {@code strongWeapon} deals measurably more melee damage than {@code baseWeapon}.
	 *
	 * @param expectedExtra the expected additional damage (e.g. +2 for a damage gem)
	 * @param tolerance     allowed deviation from {@code expectedExtra}
	 */
	public static void assertDealsMoreDamage(TestContext context, ItemStack baseWeapon, ItemStack strongWeapon,
			float expectedExtra, float tolerance) {
		float base = measureMeleeDamage(context, baseWeapon);
		float strong = measureMeleeDamage(context, strongWeapon);
		float delta = strong - base;
		if (Math.abs(delta - expectedExtra) > tolerance) {
			throw new GameTestException("Expected ~+" + expectedExtra + " real melee damage (±" + tolerance
					+ "), but measured base=" + base + ", upgraded=" + strong + " (delta=" + delta + "). "
					+ "The upgrade's attack-damage attribute is not reaching actual combat.");
		}
	}
}
