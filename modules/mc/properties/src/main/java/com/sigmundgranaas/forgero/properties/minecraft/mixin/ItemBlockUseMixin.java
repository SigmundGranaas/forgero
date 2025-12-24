package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.blockuse.BlockUseManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept block use events for BlockUseProperty.
 * Triggers when a player uses an item on a block (right-click).
 */
@Mixin(Item.class)
public abstract class ItemBlockUseMixin {

	/**
	 * Inject into useOnBlock to trigger BlockUseProperty effects.
	 */
	@Inject(
			method = "useOnBlock",
			at = @At("HEAD"),
			cancellable = true
	)
	private void forgero$onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		PlayerEntity player = context.getPlayer();
		if (player == null) {
			return;
		}

		ItemStack stack = context.getStack();
		Hand hand = context.getHand();
		BlockHitResult hitResult = new BlockHitResult(
				context.getHitPos(),
				context.getSide(),
				context.getBlockPos(),
				false
		);

		ActionResult result = BlockUseManager.handleBlockUse(stack, player, hand, hitResult);
		if (result != ActionResult.PASS) {
			cir.setReturnValue(result);
			cir.cancel();
		}
	}
}
