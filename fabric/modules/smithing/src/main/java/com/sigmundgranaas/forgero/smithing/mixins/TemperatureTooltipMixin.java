package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.List;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

		int[] bounds = TemperatureColorProvider.getStageBoundaries(maxTemp);
		// Stage indices for HUD colors (6 stages)
		int idxCold     = 0;
		int idxWarm     = 1;
		int idxHot      = 2;
		int idxVeryHot  = 3;
		int idxNearMelt = 4;
		int idxMolten   = 5;

		// Determine stage index for current temperature
		int stageIdx = segmentIndex(temp, bounds);

		// Use helper method for HUD color mapping
		int color = TemperatureColorProvider.getHudColorForTemperature(temp, maxTemp, bounds, idxCold, idxWarm, idxHot, idxVeryHot, idxNearMelt, idxMolten, stageIdx);

		// Forging stage is now the Very Hot stage (green)
		int forgingMin = bounds[idxVeryHot];
		int forgingMax = bounds[idxNearMelt];

		Text label = Text.literal("Temperature: ").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
		Text forgingRange = Text.literal(String.format("(%d–%d°C)", forgingMin, forgingMax))
			.styled(style -> style.withColor(TextColor.fromRgb(0xFF00FF00)));
		Text value = Text.literal(String.format("%d°C ", temp))
			.styled(style -> style.withColor(TextColor.fromRgb(color)));

		tooltip.add(label.copy().append(value).append(forgingRange));
	}

	@Unique
	private int segmentIndex(int value, int[] boundaries) {
		int idx = java.util.Arrays.binarySearch(boundaries, value);
		if (idx >= 0) {
			return Math.min(idx, boundaries.length - 2);
		}
		int insertionPoint = -(idx + 1);
		return Math.max(0, insertionPoint - 1);
	}

	@Unique
	private int scaleToMax(int base, int maxTemp) {
		if (maxTemp >= 10000) return base;
		return Math.round(base / 10000f * maxTemp);
	}
}
