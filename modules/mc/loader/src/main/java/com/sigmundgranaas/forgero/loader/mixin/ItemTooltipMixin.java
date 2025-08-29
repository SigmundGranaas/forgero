package com.sigmundgranaas.forgero.loader.mixin;

import com.sigmundgranaas.forgero.common.tooltip.ForgeroTooltipRenderer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin into the Item class to append Forgero's custom tooltip.
 * This injection point is at the TAIL of the appendTooltip method, ensuring
 * that Forgero's information is added after all vanilla tooltip lines.
 */
@Mixin(Item.class)
public abstract class ItemTooltipMixin {
	@Inject(method = "appendTooltip", at = @At("TAIL"))
	public void forgero$appendForgeroTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
		ForgeroTooltipRenderer.append(stack, tooltip, context);
	}
}
