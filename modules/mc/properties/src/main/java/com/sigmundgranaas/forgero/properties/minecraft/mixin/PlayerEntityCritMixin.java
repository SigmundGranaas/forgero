package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.oncrit.OnCritManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@code forgero:on_crit} when the player lands a vanilla critical hit. Injected at the
 * crit-particle call inside {@code PlayerEntity.attack}, which runs only when a crit landed.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityCritMixin {

	@Inject(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/player/PlayerEntity;addCritParticles(Lnet/minecraft/entity/Entity;)V"
			)
	)
	private void forgero$onCrit(Entity target, CallbackInfo ci) {
		PlayerEntity attacker = (PlayerEntity) (Object) this;
		if (target instanceof LivingEntity victim) {
			OnCritManager.handleCrit(attacker, victim);
		}
	}
}
