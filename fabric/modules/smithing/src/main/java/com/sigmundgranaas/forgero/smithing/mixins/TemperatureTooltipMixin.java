package com.sigmundgranaas.forgero.smithing.mixins;

import static com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.hasMaxTemperature;

import java.util.List;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem;
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem.TemperatureStages;
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
		if (!hasMaxTemperature(itemStack)) {
			return;
		}
		int temp = TemperatureUtils.getTemperature(itemStack);

		TemperatureStages stages = DynamicTemperatureSystem.calculateStages(itemStack);
		boolean isWorkable = DynamicTemperatureSystem.isWorkable(temp, stages);
		int tempColor = isWorkable ? 0x00FF00 : 0xFFFFFF;

		Text label = Text.literal("Temperature: ").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)));
		Text value = Text.literal(String.format("%d°C ", temp))
			.styled(style -> style.withColor(TextColor.fromRgb(tempColor)));
		Text forgingRange = Text.literal(String.format("(%d–%d°C)", stages.hotStart, stages.hotEnd))
			.styled(style -> style.withColor(TextColor.fromRgb(0x00FF00)));

		tooltip.add(label.copy().append(value).append(forgingRange));
	}
}
