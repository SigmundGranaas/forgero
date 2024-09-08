package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import com.sigmundgranaas.forgero.minecraft.common.feature.swinghand.SwingHandFeatureExecutor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@Mixin(LivingEntity.class)
public class LivingEntitySwingHandMixin {

	private MatchContext forgero$context = null;

	@Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("RETURN"))
	private void forgero$afterSwingHandEvent(Hand hand, boolean fromServerPlayer, CallbackInfo info) {
		@SuppressWarnings("DataFlowIssue")
		LivingEntity entity = (LivingEntity) (Object) this;
		ItemStack main = entity.getMainHandStack();

		if(forgero$context == null){
			 this.forgero$context = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, entity), new MatchContext.KeyValuePair(WORLD, entity.getWorld()));
		}

		 SwingHandFeatureExecutor.initFromMainHandStack(entity, main, hand).execute(forgero$context);
	}
}
