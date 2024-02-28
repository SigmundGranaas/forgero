package com.sigmundgranaas.forgero.bow.mixin;

import static com.sigmundgranaas.forgero.bow.Attributes.DRAW_SPEED;
import static com.sigmundgranaas.forgero.bow.handler.LaunchProjectileHandler.getPullProgress;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class BowFovMixin {

	@Inject(method = "getFovMultiplier", at = @At("HEAD"), cancellable = true)
	private void modifyFovForCustomBow(CallbackInfoReturnable<Float> cir) {
		AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
		Optional<State> state = StateService.INSTANCE.convert(player.getActiveItem());
		if (player.isUsingItem() && state.isPresent() && player.getActiveItem().getUseAction() == UseAction.BOW) {
			int i = player.getItemUseTime();
			float g = getPullProgress(i, ComputedAttribute.apply(state.get(), DRAW_SPEED));
			if (g > 1.0F) {
				g = 1.0F;
			} else {
				g *= g;
			}
			float f = 1.0F - g * 0.3F;
			cir.setReturnValue(MathHelper.lerp((MinecraftClient.getInstance().options.getFovEffectScale().getValue()).floatValue(), 1.0F, f));
		}
	}
}
