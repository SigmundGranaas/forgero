package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.onblockplace.OnBlockPlaceManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept block placement events.
 * Triggers OnBlockPlaceProperty effects when a player successfully places a block.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemUseMixin {

	/**
	 * Injects at the return point of BlockItem.place() to trigger block placement effects.
	 * Only triggers when:
	 * - The placement was successful (ActionResult.SUCCESS or CONSUME)
	 * - A player initiated the placement (not null)
	 *
	 * @param context The placement context containing position and player info
	 * @param cir Callback info returnable containing the action result
	 */
	@Inject(method = "place", at = @At("RETURN"))
	private void forgero$onBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = cir.getReturnValue();

		// Only trigger if placement was successful
		if (result != ActionResult.SUCCESS && result != ActionResult.CONSUME) {
			return;
		}

		PlayerEntity player = context.getPlayer();
		if (player == null) {
			return; // Dispenser placement or other non-player source
		}

		// Trigger the block place effects
		OnBlockPlaceManager.handleBlockPlace(player, context.getBlockPos(), context.getStack());
	}
}
