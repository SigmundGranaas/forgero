package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeatures;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.SwingHandFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

@Mixin(LivingEntity.class)
public class LivingEntitySwingHandMixin {

	@Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("RETURN"))
	private void afterSwingHand(Hand hand, boolean fromServerPlayer, CallbackInfo info) {
		@SuppressWarnings("DataFlowIssue")
		LivingEntity entity = (LivingEntity) (Object) this;
		ItemStack main = entity.getMainHandStack();
		if (entity instanceof ServerPlayerEntity player) {
			if (player.getItemCooldownManager().isCoolingDown(main.getItem())) {
				return;
			}
		}

		MatchContext context = MatchContext.of()
				.put(ENTITY, entity)
				.put(WORLD, entity.world);

		cachedFilteredFeatures(main, SwingHandFeature.KEY, context)
				.forEach(handler -> {
					handler.onSwing(entity, hand);
				});
	}
}
