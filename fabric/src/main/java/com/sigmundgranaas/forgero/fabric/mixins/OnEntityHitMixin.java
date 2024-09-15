package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.feature.onhit.entity.OnHitEntityFeatureExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import static com.sigmundgranaas.forgero.match.MinecraftContextKeys.*;

@Mixin(LivingEntity.class)
public abstract class OnEntityHitMixin {

	/**
	 * Injects the onHit logic after the LivingEntity is damaged by another entity.
	 *
	 * @param source The damage source.
	 * @param amount The amount of damage.
	 * @param cir    The callback information.
	 */
	@Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("RETURN"))
	private void forgero$onDamageEntityMixin$OnHitHandler(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Entity attacker = source.getAttacker();
		if (attacker != null && (cir.getReturnValue() || attacker.getWorld().isClient) && attacker instanceof LivingEntity entity) {
			@SuppressWarnings("DataFlowIssue")
			Entity target = (Entity) (Object) this;

			MatchContext context = MatchContext.of()
			                                   .put(ENTITY, entity)
			                                   .put(WORLD, entity.getWorld())
			                                   .put(ENTITY_TARGET, target);

			OnHitEntityFeatureExecutor.initFromMainHandStack(entity, target)
			                          .executeIfNotCoolingDown(context);
		}
	}
}
