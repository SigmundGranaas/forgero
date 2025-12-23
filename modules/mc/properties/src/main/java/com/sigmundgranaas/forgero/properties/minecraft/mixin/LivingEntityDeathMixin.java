package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.onkill.OnKillManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to trigger On-Kill effects when an entity dies.
 * Injects at the beginning of the onDeath method to apply victory rewards.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathMixin {
	@Inject(method = "onDeath", at = @At("HEAD"))
	private void forgero$onKill(DamageSource damageSource, CallbackInfo ci) {
		LivingEntity victim = (LivingEntity) (Object) this;

		// Check if there's an attacker
		if (damageSource.getAttacker() instanceof LivingEntity killer) {
			OnKillManager.handleKill(killer, victim);
		}
	}
}
