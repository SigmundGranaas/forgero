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

		// Determine stage index for current temperature
		int stageIdx = segmentIndex(temp, bounds);

		// Map stage index to HUD color schema
		final int BLUE   = 0xFF0077FF; // tempering (and cold)
		final int YELLOW = 0xFFFFCC00; // shaping and welding
		final int GREEN  = 0xFF00CC00; // forging
		final int RED    = 0xFFCC0000; // critical and overheated
		int color;
		if (stageIdx == 4) color = GREEN; // forging
		else if (stageIdx == 3 || stageIdx == 5) color = YELLOW; // shaping or welding
		else if (stageIdx == 2 || stageIdx == 6) color = RED; // critical or overheated
		else if (stageIdx == 1) color = BLUE; // tempering
		else color = YELLOW; // fallback

		Text label = Text.literal("Temperature: ").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
		Text forgingRange = Text.literal(String.format("(%d–%d°C)", forgingMin, forgingMax))
			.styled(style -> style.withColor(TextColor.fromRgb(GREEN)));
		Text value = Text.literal(String.format("%d°C ", temp))
			.styled(style -> style.withColor(TextColor.fromRgb(color)));

		tooltip.add(label.copy().append(value).append(forgingRange));
	}

	// Helper: same as HUD
	private int segmentIndex(int value, int[] boundaries) {
		int idx = java.util.Arrays.binarySearch(boundaries, value);
		if (idx >= 0) {
			return Math.min(idx, boundaries.length - 2);
		}
		int insertionPoint = -(idx + 1);
		return Math.max(0, insertionPoint - 1);
	}
}
