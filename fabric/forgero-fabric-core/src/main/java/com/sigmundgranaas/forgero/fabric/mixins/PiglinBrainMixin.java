package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureContainerKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

	@Unique
	private static boolean isGoldenForgeroTool(ItemStack stack) {
		return StateService.INSTANCE.convert(stack)
				.filter(state -> FeatureCache.check(FeatureContainerKey.of(state, Feature.key("forgero:golden"))))
				.isPresent();
	}
}
