package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.item.DefaultStateItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@Mixin(DefaultStateItem.class)
public class TemperatureTooltipMixin {
	@Inject(method = "appendTooltip", at = @At("TAIL"))
	private void forgero$addTemperatureTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
		int temp = TemperatureUtils.getTemperature(stack);
		String color;
		if (temp > 100) {
			color = "§c"; // red for dangerous/hot
		} else if (temp > TemperatureUtils.DEFAULT_TEMPERATURE) {
			color = "§e"; // yellow for warm
		} else {
			color = "§f"; // white for safe/room temp
		}
		tooltip.add(Text.literal(color + "Temperature: " + temp + "°C"));
	}
}
