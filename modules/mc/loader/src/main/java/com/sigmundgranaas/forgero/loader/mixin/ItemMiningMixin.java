// modules/mc/loader/src/main/java/com/sigmundgranaas/forgero/loader/mixin/ItemMiningMixin.java

package com.sigmundgranaas.forgero.loader.mixin;

import com.sigmundgranaas.forgero.common.api.MixinServiceAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Simplified ItemMiningMixin that delegates all logic to services.
 */
@Mixin(Item.class)
public class ItemMiningMixin {

	@Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
	private void forgero$injectMiningSpeed(
			ItemStack stack,
			BlockState state,
			CallbackInfoReturnable<Float> cir
	) {
		// Check if this is a Forgero item first (fail-fast)
		if (!MixinServiceAccessor.isForgeroItem(stack)) {
			return;
		}

		// Get mining speed from services - returns 0 if tool not effective
		float forgeroSpeed = MixinServiceAccessor.getServices()
				.map(services -> services.itemQuery().getMiningSpeed(stack, state))
				.orElse(0f);

		// Only override if Forgero's speed is greater
		if (forgeroSpeed > cir.getReturnValueF()) {
			cir.setReturnValue(forgeroSpeed);
		}
	}
}
