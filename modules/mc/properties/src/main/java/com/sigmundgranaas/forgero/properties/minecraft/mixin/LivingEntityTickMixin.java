package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.onsneak.OnSneakToggleManager;
import com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickManager;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTickMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void forgero$onTick(CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		OnTickManager.handle(entity);
		OnSneakToggleManager.handleTick(entity);
	}
}
