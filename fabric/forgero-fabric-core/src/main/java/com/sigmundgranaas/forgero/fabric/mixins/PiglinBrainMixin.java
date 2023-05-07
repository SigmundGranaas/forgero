package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

	@Inject(method = "isGoldenItem", at = @At("HEAD"), cancellable = true)
	private static void isGoldenItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (isGoldenForgeroTool(stack)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "wearsGoldArmor", at = @At("HEAD"), cancellable = true)
	private static void wearsGold(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (isGoldenForgeroTool(entity.getMainHandStack())) {
			cir.setReturnValue(true);
		} else if (isGoldenForgeroTool(entity.getOffHandStack())) {
			cir.setReturnValue(true);
		}
	}

	private static boolean isGoldenForgeroTool(ItemStack stack) {
		return StateService.INSTANCE.convert(stack)
				.filter(state -> ContainsFeatureCache.check(PropertyTargetCacheKey.of(state, "GOLDEN")))
				.isPresent();
	}
}
