package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.ConvertHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.DisarmHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.FireHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.KnockbackHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.LifeStealHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.LightningHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.StatusEffectHandler;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class OnHitHandlerGametest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFireHandler(TestContext context) {
		FireHandler handler = new FireHandler(5); // 5 seconds
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 1));

		handler.onHit(source, target);

		context.assertTrue(target.isOnFire(), "Target should be on fire");
		context.assertTrue(target.getFireTicks() == 100, "Target should have 100 fire ticks (5 seconds), but had " + target.getFireTicks());
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testStatusEffectHandler(TestContext context) {
		StatusEffectHandler handler = new StatusEffectHandler(new Identifier("minecraft", "slowness"), 100, 2);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 1));

		handler.onHit(source, target);

		context.assertTrue(target.hasStatusEffect(StatusEffects.SLOWNESS), "Target should have slowness effect");
		var effectInstance = target.getStatusEffect(StatusEffects.SLOWNESS);
		context.assertTrue(effectInstance != null, "Status effect instance should not be null");
		// GameTest will fail on the line above if null, so this next check is safe.
		context.assertTrue(effectInstance.getAmplifier() == 2, "Slowness amplifier should be 2, but was " + effectInstance.getAmplifier());

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testKnockbackHandlerPush(TestContext context) {
		KnockbackHandler handler = new KnockbackHandler(2.0f, KnockbackHandler.Direction.PUSH);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 1));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 1));

		target.setVelocity(Vec3d.ZERO);
		context.assertTrue(target.getVelocity().lengthSquared() == 0, "Target should have zero velocity before knockback");

		handler.onHit(source, target);

		context.waitAndRun(1, () -> {
			context.assertTrue(target.getVelocity().lengthSquared() > 0.1, "Target should have velocity after being pushed");
			context.assertTrue(target.getVelocity().x > 0, "Target should be pushed away from the source along the X-axis");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testKnockbackHandlerPull(TestContext context) {
		KnockbackHandler handler = new KnockbackHandler(2.0f, KnockbackHandler.Direction.PULL);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 1));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 1));

		target.setVelocity(Vec3d.ZERO);
		context.assertTrue(target.getVelocity().lengthSquared() == 0, "Target should have zero velocity before knockback");

		handler.onHit(source, target);

		context.waitAndRun(1, () -> {
			context.assertTrue(target.getVelocity().lengthSquared() > 0.1, "Target should have velocity after being pulled");
			context.assertTrue(target.getVelocity().x < 0, "Target should be pulled towards the source along the X-axis");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLifeStealHandler(TestContext context) {
		LifeStealHandler handler = new LifeStealHandler(5.0f);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 1));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));

		source.setHealth(10.0f);
		float initialSourceHealth = source.getHealth();
		float initialTargetHealth = target.getHealth();

		handler.onHit(source, target);

		context.assertTrue(target.getHealth() < initialTargetHealth, "Target's health should decrease");
		context.assertTrue(source.getHealth() > initialSourceHealth, "Source's health should increase");
		context.assertTrue(target.getHealth() == initialTargetHealth - 5.0f, "Target should lose exactly 5 health");
		context.assertTrue(source.getHealth() == initialSourceHealth + 5.0f, "Source should gain exactly 5 health");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLifeStealHandlerLowHealthTarget(TestContext context) {
		LifeStealHandler handler = new LifeStealHandler(10.0f);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 1));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));

		source.setHealth(10.0f);
		target.setHealth(3.0f);

		float initialSourceHealth = source.getHealth();
		float healthToSteal = target.getHealth();

		handler.onHit(source, target);

		context.assertTrue(target.getHealth() <= 0, "Target's health should be 0 or less");
		context.assertTrue(source.getHealth() == initialSourceHealth + healthToSteal, "Source should only gain the health the target had");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLightningHandler(TestContext context) {
		LightningHandler handler = new LightningHandler();
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 1));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 1));

		handler.onHit(source, target);

		context.expectEntity(EntityType.LIGHTNING_BOLT);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testConvertHandler(TestContext context) {
		ConvertHandler handler = new ConvertHandler(new Identifier("minecraft", "zombie"));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();
		VillagerEntity target = context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 1, 1));

		handler.onHit(source, target);

		context.expectEntity(EntityType.ZOMBIE);
		context.dontExpectEntity(target.getType());
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testDisarmHandler(TestContext context) {
		DisarmHandler handler = new DisarmHandler();
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 1));

		target.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
		context.assertTrue(!target.getMainHandStack().isEmpty(), "Target should be holding a sword before disarm");

		handler.onHit(source, target);

		context.expectEntity(EntityType.ITEM);
		context.waitAndRun(1, () -> {
			context.assertTrue(target.getMainHandStack().isEmpty(), "Target should have an empty main hand after disarm");
			context.complete();
		});
	}
}
