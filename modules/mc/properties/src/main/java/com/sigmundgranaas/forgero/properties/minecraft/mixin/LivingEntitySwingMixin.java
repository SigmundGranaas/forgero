package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.swing.SwingHandManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept hand swing events for SwingHandProperty.
 * Triggers when any living entity swings their hand.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {

	/**
	 * Inject into swingHand to trigger SwingHandProperty effects when an entity swings their hand.
	 */
	@Inject(
			method = "swingHand(Lnet/minecraft/util/Hand;Z)V",
			at = @At("HEAD")
	)
	private void forgero$onSwingHand(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (!entity.getWorld().isClient()) {
			ItemStack stack = entity.getStackInHand(hand);
			if (!stack.isEmpty()) {
				SwingHandManager.handleSwing(stack, entity, hand);
			}
		}
	}
}
