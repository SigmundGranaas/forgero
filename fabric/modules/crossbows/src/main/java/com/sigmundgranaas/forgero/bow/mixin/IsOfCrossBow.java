package com.sigmundgranaas.forgero.bow.mixin;

import com.sigmundgranaas.forgero.bow.item.DynamicCrossBowItem;

import net.minecraft.item.Item;

import net.minecraft.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;


@Mixin(ItemStack.class)
public abstract class IsOfCrossBow {

	@Shadow
	public abstract Item getItem();

	@Inject(method = "isOf", at = @At("RETURN"), cancellable = true)
	private void forgero$isCrossBow(Item item, CallbackInfoReturnable<Boolean> cir) {
		// There are a ton of checks to decide how to properly render crossbows, and they are all using the CROSSBOW reference to decide.
		// Instead of mixin in to each check to add an alternative, I am just going say this is indeed a crossbow here.
		// This is obviously abusing the isOf check, but this seems like the simplest option for now.
		if(getItem() instanceof DynamicCrossBowItem && item == Items.CROSSBOW){
			cir.setReturnValue(true);
		}
	}
}
