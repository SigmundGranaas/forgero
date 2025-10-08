package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityOnHitMixin {
	@Inject(method = "applyDamage", at = @At("HEAD"))
	private void forgero$onHitMixin(DamageSource source, float amount, CallbackInfo ci) {
		Entity attacker = source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (attacker instanceof LivingEntity livingAttacker) {
			ItemStack stack = livingAttacker.getMainHandStack();
			if (!stack.isEmpty()) {
				OnHitManager.handleOnHit(stack, livingAttacker, target);
			}
		}
	}
}
