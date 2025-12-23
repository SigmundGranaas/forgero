package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.ondamage.OnDamageReceivedManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to trigger On-Damage-Received effects when an entity takes damage.
 * Injects before damage is applied to allow defensive effects.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
	@Inject(
		method = "damage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
		)
	)
	private void forgero$onDamageReceived(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity defender = (LivingEntity) (Object) this;
		OnDamageReceivedManager.handleDamageReceived(defender, source, amount);
	}
}
