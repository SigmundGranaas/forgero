// modules/mc/loader/src/main/java/com/sigmundgranaas/forgero/loader/mixin/ItemStackDurabilityMixin.java

package com.sigmundgranaas.forgero.loader.mixin;

import com.sigmundgranaas.forgero.common.api.MixinServiceAccessor;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Simplified durability mixin that delegates to services.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackDurabilityMixin {

	@Shadow
	public abstract int getDamage();

	@Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
	private void forgero$injectCustomDurability(CallbackInfoReturnable<Integer> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		
		MixinServiceAccessor.getResolvedAttributes(stack).ifPresent(attributes -> {
			int durability = (int) attributes.getValue(DefaultAttributes.DURABILITY);
			if (durability > 0) {
				cir.setReturnValue(durability);
			}
		});
	}

	@Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
	public void getItemBarStep(CallbackInfoReturnable<Integer> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		
		MixinServiceAccessor.getResolvedAttributes(stack).ifPresent(attributes -> {
			int durability = (int) attributes.getValue(DefaultAttributes.DURABILITY);
			if (durability > 0) {
				cir.setReturnValue(Math.round(13.0F - (float) this.getDamage() * 13.0F / (float) durability));
			}
		});
	}
}
