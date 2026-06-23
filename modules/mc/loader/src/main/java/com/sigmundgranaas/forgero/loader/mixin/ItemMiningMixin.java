// modules/mc/loader/src/main/java/com/sigmundgranaas/forgero/loader/mixin/ItemMiningMixin.java

package com.sigmundgranaas.forgero.loader.mixin;

import com.sigmundgranaas.forgero.common.api.MixinServiceAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies Forgero's mining speed to held items.
 * <p>
 * Injects into {@link ItemStack#getMiningSpeedMultiplier(BlockState)} — NOT
 * {@code Item.getMiningSpeedMultiplier}. {@code MiningToolItem}/{@code ToolItem}/{@code SwordItem}
 * override the {@code Item} method, so a mixin there never runs for real tools; the {@code ItemStack}
 * method is the single chokepoint every item (including overriding tools) flows through, mirroring
 * the durability/attribute mixins.
 */
@Mixin(ItemStack.class)
public class ItemMiningMixin {

	@Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
	private void forgero$injectMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		ItemStack stack = (ItemStack) (Object) this;

		// Check if this is a Forgero item first (fail-fast)
		if (!MixinServiceAccessor.isForgeroItem(stack)) {
			return;
		}

		// Get mining speed from services - returns 0 if tool not effective
		float forgeroSpeed = MixinServiceAccessor.getServices()
				.map(services -> services.itemQuery().getMiningSpeed(stack, state))
				.orElse(0f);

		// Only override if Forgero's speed is greater (e.g. an upgrade raised it above the base tool)
		if (forgeroSpeed > cir.getReturnValueF()) {
			cir.setReturnValue(forgeroSpeed);
		}
	}
}
