package com.sigmundgranaas.forgero.smithing.temperature;

import static com.sigmundgranaas.forgero.smithing.Attributes.MAX_TEMPERATURE;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class TemperatureUtils {
    public static final String TEMPERATURE_KEY = "forgero_temperature";
    public static final String MAX_TEMPERATURE_KEY = "forgero_max_temperature";
    public static final int DEFAULT_TEMPERATURE = 20;
    public static final int MIN_TEMPERATURE = 0;

    public static int getTemperature(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.contains(TEMPERATURE_KEY) ? nbt.getInt(TEMPERATURE_KEY) : DEFAULT_TEMPERATURE;
    }

    public static void setTemperature(ItemStack stack, int temperature) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(TEMPERATURE_KEY, clamp(temperature, stack));
    }

    public static int getMaxTemp(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains(MAX_TEMPERATURE_KEY)) {
            return nbt.getInt(MAX_TEMPERATURE_KEY);
        }
        Optional<State> stateOpt = StateService.INSTANCE.convert(stack);
        int maxTemp = 10000; // Default max temperature if attribute is not present
        Optional<State> state = StateService.INSTANCE.convert(stack);
        if (state.isPresent()) {
            int attr = ComputedAttribute.of(state.get(), MAX_TEMPERATURE).asInt();
            if (attr > 0) {
                return attr;
            }
        }
        return 0;
    }

    public static void setMaxTemperature(ItemStack stack, int maxTemperature) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(MAX_TEMPERATURE_KEY, maxTemperature);
    }

    public static boolean hasMaxTemperature(ItemStack stack) {
        // Exclude full tools, but allow MorphedItem
        if (stack.getItem() instanceof ToolStateItem && !(stack.getItem() instanceof MorphedItem)) {
            return false;
        }
        return getMaxTemp(stack) > 0;
    }

    public static int clamp(int temperature, ItemStack stack) {
        return Math.max(MIN_TEMPERATURE, Math.min(getMaxTemp(stack), temperature));
    }
}
