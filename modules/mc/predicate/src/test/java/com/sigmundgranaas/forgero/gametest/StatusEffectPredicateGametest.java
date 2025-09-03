package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.entity.StatusEffectPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Optional;

public class StatusEffectPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testSimpleEffectMatch(TestContext context) {
		// Checks for Speed of any amplifier/duration
		StatusEffectPredicate.EffectData speedData = new StatusEffectPredicate.EffectData(Optional.empty(), Optional.empty());
		StatusEffectPredicate predicate = new StatusEffectPredicate(Map.of(StatusEffects.SPEED, speedData));
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
		context.assertTrue(predicate.test(entity), "Predicate should match entity with speed effect");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testSimpleEffectMismatch(TestContext context) {
		StatusEffectPredicate.EffectData speedData = new StatusEffectPredicate.EffectData(Optional.empty(), Optional.empty());
		StatusEffectPredicate predicate = new StatusEffectPredicate(Map.of(StatusEffects.SPEED, speedData));
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		// No effect applied
		context.assertFalse(predicate.test(entity), "Predicate should not match entity without speed effect");

		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));
		context.assertFalse(predicate.test(entity), "Predicate should not match entity with wrong effect");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testAmplifierAndDurationMatch(TestContext context) {
		NumericPredicate amplifier = new NumericPredicate(Optional.of(1.0), Optional.empty(), Optional.empty()); // Speed II is amplifier 1
		NumericPredicate duration = new NumericPredicate(Optional.of(100.0), Optional.empty(), Optional.empty());
		StatusEffectPredicate.EffectData effectData = new StatusEffectPredicate.EffectData(Optional.of(amplifier), Optional.of(duration));
		StatusEffectPredicate predicate = new StatusEffectPredicate(Map.of(StatusEffects.SPEED, effectData));
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
		context.assertTrue(predicate.test(entity), "Predicate should match correct amplifier and duration");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testAmplifierMismatch(TestContext context) {
		NumericPredicate amplifier = new NumericPredicate(Optional.of(2.0), Optional.empty(), Optional.empty()); // Requires amplifier 2 (Speed III)
		StatusEffectPredicate.EffectData effectData = new StatusEffectPredicate.EffectData(Optional.of(amplifier), Optional.empty());
		StatusEffectPredicate predicate = new StatusEffectPredicate(Map.of(StatusEffects.SPEED, effectData));
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1)); // Has amplifier 1
		context.assertFalse(predicate.test(entity), "Predicate should not match with wrong amplifier");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMultipleEffectsMatch(TestContext context) {
		StatusEffectPredicate.EffectData speedData = new StatusEffectPredicate.EffectData(Optional.empty(), Optional.empty());
		StatusEffectPredicate.EffectData strengthData = new StatusEffectPredicate.EffectData(Optional.of(new NumericPredicate(Optional.empty(), Optional.empty(), Optional.of(0.0))), Optional.empty());
		StatusEffectPredicate predicate = new StatusEffectPredicate(Map.of(StatusEffects.SPEED, speedData, StatusEffects.STRENGTH, strengthData));
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 0));
		context.assertTrue(predicate.test(entity), "Predicate should match when all specified effects are present");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMultipleEffectsOneMissing(TestContext context) {
		StatusEffectPredicate.EffectData speedData = new StatusEffectPredicate.EffectData(Optional.empty(), Optional.empty());
		StatusEffectPredicate.EffectData strengthData = new StatusEffectPredicate.EffectData(Optional.empty(), Optional.empty());
		StatusEffectPredicate predicate = new StatusEffectPredicate(Map.of(StatusEffects.SPEED, speedData, StatusEffects.STRENGTH, strengthData));
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
		// Missing strength
		context.assertFalse(predicate.test(entity), "Predicate should not match if one effect is missing");

		context.complete();
	}
}
