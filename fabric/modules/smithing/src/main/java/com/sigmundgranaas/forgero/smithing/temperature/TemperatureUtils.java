package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class TemperatureUtils {
    public static final String TEMPERATURE_KEY = "forgero_temperature";
    public static final int DEFAULT_TEMPERATURE = 10;
    public static final int MIN_TEMPERATURE = 0;
    public static final int MAX_TEMPERATURE = 2000;

    public static int getTemperature(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.contains(TEMPERATURE_KEY) ? nbt.getInt(TEMPERATURE_KEY) : DEFAULT_TEMPERATURE;
    }

    public static void setTemperature(ItemStack stack, int temperature) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(TEMPERATURE_KEY, clamp(temperature));
    }

    public static int clamp(int temperature) {
        return Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, temperature));
    }
}

