package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.entityuse.EntityUseManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept entity use events for EntityUseProperty.
 * Triggers when a player uses an item on an entity (right-click).
 */
@Mixin(Item.class)
public abstract class ItemEntityUseMixin {

	/**
	 * Inject into useOnEntity to trigger EntityUseProperty effects.
	 */
	@Inject(
			method = "useOnEntity",
			at = @At("HEAD"),
			cancellable = true
	)
	private void forgero$onUseOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand,
	                                    CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = EntityUseManager.handleEntityUse(stack, user, entity, hand);
		if (result != ActionResult.PASS) {
			cir.setReturnValue(result);
			cir.cancel();
		}
	}
}
