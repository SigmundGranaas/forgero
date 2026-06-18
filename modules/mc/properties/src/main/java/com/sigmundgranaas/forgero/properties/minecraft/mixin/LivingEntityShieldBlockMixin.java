package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.onblock.OnBlockManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fires {@code forgero:on_block} when an attack is blocked by a shield. Injected at the
 * {@code damageShield} call inside {@code LivingEntity.damage}, which runs only on a successful block.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldBlockMixin {

	@Inject(
			method = "damage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"
			)
	)
	private void forgero$onShieldBlock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity defender = (LivingEntity) (Object) this;
		if (source.getAttacker() instanceof LivingEntity attacker) {
			OnBlockManager.handleBlock(defender, attacker);
		}
	}
}
