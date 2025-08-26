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
		// Stage indices for HUD colors
		int idxCold     = segmentIndex(scaleToMax(1572,  maxTemp), bounds);      // (20 + 3125) / 2
		int idxCool     = segmentIndex(scaleToMax(3750,  maxTemp), bounds);      // (3125 + 4375) / 2
		int idxMild     = segmentIndex(scaleToMax(5000,  maxTemp), bounds);      // (4375 + 5625) / 2
		int idxWarm     = segmentIndex(scaleToMax(6250,  maxTemp), bounds);      // (5625 + 6875) / 2
		int idxExtreme  = segmentIndex(scaleToMax(7500,  maxTemp), bounds);      // (6875 + 8125) / 2
		int idxVeryHot  = segmentIndex(scaleToMax(8750,  maxTemp), bounds);      // (8125 + 9375) / 2
		int idxMolten   = segmentIndex(scaleToMax(9687,  maxTemp), bounds);      // (9375 + 10000) / 2

		// Determine stage index for current temperature
		int stageIdx = segmentIndex(temp, bounds);

		// HUD color mapping
		final int BLUE     = 0xFF0000FF; // Cold
		final int CYAN     = 0xFF00FFFF; // Cool/Ambient
		final int TEAL     = 0xFF00FF80; // Mild/Warm
		final int YELLOW   = 0xFFFFFF00; // Warm/Hot
		final int GREEN    = 0xFF00FF00; // Extreme/Unusual
		final int ORANGE   = 0xFFFFA500; // Very Hot/Near Molten
		final int RED      = 0xFFFF0000; // Molten
		int color;
		if (stageIdx == idxCold)    color = BLUE;
		else if (stageIdx == idxCool)    color = CYAN;
		else if (stageIdx == idxMild)    color = TEAL;
		else if (stageIdx == idxWarm)    color = YELLOW;
		else if (stageIdx == idxExtreme) color = GREEN;
		else if (stageIdx == idxVeryHot) color = ORANGE;
		else if (stageIdx == idxMolten)  color = RED;
		else color = YELLOW; // fallback

		// Forging stage is now the Extreme stage
		int extremeMin = bounds[4]; // Extreme stage lower bound
		int extremeMax = bounds[5]; // Extreme stage upper bound

		Text label = Text.literal("Temperature: ").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
		Text forgingRange = Text.literal(String.format("(%d–%d°C)", extremeMin, extremeMax))
			.styled(style -> style.withColor(TextColor.fromRgb(GREEN)));
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
