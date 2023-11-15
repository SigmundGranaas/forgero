package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.streamFeature;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnHitEntityFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

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
			ItemStack main = entity.getMainHandStack();
			if (entity instanceof ServerPlayerEntity player && player.getItemCooldownManager().isCoolingDown(main.getItem())) {
				return;
			}
			Entity target = (Entity) (Object) this;
			MatchContext context = MatchContext.of()
					.put(ENTITY, entity)
					.put(WORLD, entity.world)
					.put(ENTITY_TARGET, target);

			streamFeature(main, context, OnHitEntityFeature.KEY)
					.forEach(handler -> {
						handler.onHit(entity, entity.world, target);
						handler.handle(entity, main, Hand.MAIN_HAND);
					});
		}
	}
}
