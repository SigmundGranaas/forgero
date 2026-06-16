package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
	@Redirect(
			method = "updateHeldItems",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
					ordinal = 0
			)
	)
	private boolean forgero$areMainHandStacksEqual(ItemStack left, ItemStack right) {
		return forgero$areEqualOrTemperatureOnly(left, right);
	}

	@Redirect(
			method = "updateHeldItems",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
					ordinal = 1
			)
	)
	private boolean forgero$areOffHandStacksEqual(ItemStack left, ItemStack right) {
		return forgero$areEqualOrTemperatureOnly(left, right);
	}

	@Unique
	private boolean forgero$areEqualOrTemperatureOnly(ItemStack left, ItemStack right) {
		return TemperatureUtils.areEqualIgnoringTemperature(left, right);
	}
}
