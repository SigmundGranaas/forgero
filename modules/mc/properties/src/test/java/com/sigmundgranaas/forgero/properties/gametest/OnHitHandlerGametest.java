package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.FireHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.StatusEffectHandler;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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
}
