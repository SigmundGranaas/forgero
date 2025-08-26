package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.List;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.world.World;

@Mixin(Item.class)
public class TemperatureTooltipMixin {
	@Inject(method = "appendTooltip", at = @At("TAIL"))
	private void forgero$addTemperatureTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext, CallbackInfo ci) {
		if (!TemperatureUtils.hasMaxTemperature(itemStack)) {
			return;
		}
		int temp = TemperatureUtils.getTemperature(itemStack);
		int maxTemp = TemperatureUtils.getMaxTemp(itemStack);
		int argb = TemperatureColorProvider.getHeatColor(temp, maxTemp);
		Text label = Text.literal("Temperature:").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
		Text value = Text.literal(" " + temp + "°C").styled(style -> style.withColor(TextColor.fromRgb(argb)));
		tooltip.add(label.copy().append(value));
	}
}
