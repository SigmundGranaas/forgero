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

		int[] bounds = TemperatureColorProvider.getStageBoundaries(maxTemp);
		int forgingMin = bounds[4];
		int forgingMax = bounds[5];

		Text label = Text.literal("Temperature: ").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
		// Use orange for forging stage min/max
		Text forgingRange = Text.literal(String.format("(%d–%d°C)", forgingMin, forgingMax))
			.styled(style -> style.withColor(TextColor.fromRgb(0xFFFF9900)));
		Text value = Text.literal(String.format("%d°C ", temp))
			.styled(style -> style.withColor(TextColor.fromRgb(argb)));

		tooltip.add(label.copy().append(value).append(forgingRange));
	}
}
