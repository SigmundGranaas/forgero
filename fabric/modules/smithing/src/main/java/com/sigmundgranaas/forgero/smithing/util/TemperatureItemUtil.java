package com.sigmundgranaas.forgero.smithing.util;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import net.minecraft.item.ItemStack;

public class TemperatureItemUtil {
    public static boolean hasMaxTemperature(ItemStack stack) {
        return TemperatureUtils.getMaxTemp(stack) > 0;
    }
}
