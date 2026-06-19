package com.sigmundgranaas.forgero.smithing.temperature;

import static com.sigmundgranaas.forgero.smithing.Attributes.*;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.state.property.Properties;
import net.minecraft.world.World;

public class TemperatureUtils {
    public static final String TEMPERATURE_KEY = "forgero_temperature";
    public static final String MAX_TEMPERATURE_KEY = "forgero_max_temperature";
    public static final String WORKABLE_TEMPERATURE_START_KEY = "forgero_workable_temperature_start";
    public static final String WORKABLE_TEMPERATURE_END_KEY = "forgero_workable_temperature_end";
    public static final int DEFAULT_TEMPERATURE = 20;
    public static final int MIN_TEMPERATURE = 0;

    public static int getTemperature(ItemStack stack) {
        if (stack.isEmpty()) {
            return DEFAULT_TEMPERATURE;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(TEMPERATURE_KEY)) {
            return DEFAULT_TEMPERATURE;
        }
        int stored = nbt.getInt(TEMPERATURE_KEY);
        return clamp(stored, stack);
    }

    public static void setTemperature(ItemStack stack, int temperature) {
        if (stack.isEmpty()) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(TEMPERATURE_KEY, clamp(temperature, stack));
    }

    public static void copyTemperatureData(ItemStack source, ItemStack target) {
        if (source.isEmpty() || target.isEmpty()) {
            return;
        }

        setMaxTemperature(target, getMaxTemp(source));
        setWorkableTemperatureStart(target, getWorkableTemperatureStart(source));
        setWorkableTemperatureEnd(target, getWorkableTemperatureEnd(source));
        setTemperature(target, getTemperature(source));
    }

    public static void removeTemperatureData(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasNbt()) {
            return;
        }

        NbtCompound nbt = stack.getNbt();

        if (nbt == null) {
            return;
        }

        nbt.remove(TEMPERATURE_KEY);
        nbt.remove(MAX_TEMPERATURE_KEY);
        nbt.remove(WORKABLE_TEMPERATURE_START_KEY);
        nbt.remove(WORKABLE_TEMPERATURE_END_KEY);

        if (nbt.isEmpty()) {
            stack.setNbt(null);
        }
    }

    public static boolean areEqualIgnoringTemperature(ItemStack left, ItemStack right) {
        if (left == right || ItemStack.areEqual(left, right)) {
            return true;
        }

        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        if (left.getItem() != right.getItem() || left.getCount() != right.getCount()) {
            return false;
        }

        return Objects.equals(
            nbtWithoutTemperature(left),
            nbtWithoutTemperature(right)
        );
    }

    private static NbtCompound nbtWithoutTemperature(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();

        if (nbt == null) {
            return null;
        }

        NbtCompound copy = nbt.copy();
        removeTemperature(copy);

        return copy.isEmpty() ? null : copy;
    }

    private static void removeTemperature(NbtElement element) {
        if (element instanceof NbtCompound compound) {
            removeTemperature(compound);
        } else if (element instanceof NbtList list) {
            for (NbtElement child : list) {
                removeTemperature(child);
            }
        }
    }

    private static void removeTemperature(NbtCompound compound) {
        compound.remove(TEMPERATURE_KEY);

        for (String key : new ArrayList<>(compound.getKeys())) {
            NbtElement child = compound.get(key);

            if (child == null) {
                continue;
            }

            removeTemperature(child);

            if (isEmptySerializedStackTag(compound, key, child)) {
                compound.remove(key);
            }
        }
    }

    private static boolean isEmptySerializedStackTag(NbtCompound parent, String key, NbtElement child) {
        return "tag".equals(key)
            && child instanceof NbtCompound childCompound
            && childCompound.isEmpty()
            && parent.contains("id")
            && parent.contains("Count");
    }

    public static int getMaxTemp(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(MAX_TEMPERATURE_KEY)) {
            return Math.max(0, nbt.getInt(MAX_TEMPERATURE_KEY));
        }
        Optional<State> state = StateService.INSTANCE.convert(stack);
        if (state.isPresent()) {
            int attr = ComputedAttribute.of(state.get(), MAX_TEMPERATURE).asInt();
            return Math.max(0, attr);
        }
        return 0;
    }

    public static void setMaxTemperature(ItemStack stack, int maxTemperature) {
        if (stack.isEmpty()) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(MAX_TEMPERATURE_KEY, Math.max(0, maxTemperature));
    }

    public static boolean hasMaxTemperature(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof ToolStateItem
                && !(stack.getItem() instanceof MorphedItem)
                && !hasStoredTemperature(stack)) {
            return false;
        }
        return getMaxTemp(stack) > 0;
    }

    private static boolean hasStoredTemperature(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(TEMPERATURE_KEY);
    }

    public static int clamp(int temperature, ItemStack stack) {
        int max = Math.max(getMaxTemp(stack), DEFAULT_TEMPERATURE);
        return Math.max(MIN_TEMPERATURE, Math.min(max, temperature));
    }

    public static boolean isBlockFilledWaterCauldron(BlockState state) {
        return state != null
            && state.isOf(Blocks.WATER_CAULDRON)
            && state.contains(Properties.LEVEL_3)
            && state.get(Properties.LEVEL_3) == 3;
    }

    public static boolean isWaterCauldron(BlockState state) {
        return state != null && state.isOf(Blocks.WATER_CAULDRON);
    }

    public static boolean isItemInFilledWaterCauldron(ItemEntity itemEntity, World world) {
        if (itemEntity == null || world == null) return false;
        BlockState state = world.getBlockState(itemEntity.getBlockPos());
        return isBlockFilledWaterCauldron(state);
    }

    public static int getWorkableTemperatureStart(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(WORKABLE_TEMPERATURE_START_KEY)) {
            return Math.max(0, nbt.getInt(WORKABLE_TEMPERATURE_START_KEY));
        }
        Optional<State> state = StateService.INSTANCE.convert(stack);
        if (state.isPresent()) {
			int attr = ComputedAttribute.of(state.get(), WORKABLE_TEMPERATURE_START).asInt();
            return Math.max(0, attr);
        }
        return 0;
    }

    public static int getWorkableTemperatureEnd(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(WORKABLE_TEMPERATURE_END_KEY)) {
            return Math.max(0, nbt.getInt(WORKABLE_TEMPERATURE_END_KEY));
        }
        Optional<State> state = StateService.INSTANCE.convert(stack);
        if (state.isPresent()) {
			int attr = ComputedAttribute.of(state.get(), WORKABLE_TEMPERATURE_END).asInt();
            return Math.max(0, attr);
        }
        return 0;
    }

    public static boolean hasWorkableTemperatureStart(ItemStack stack) {
        return getWorkableTemperatureStart(stack) > 0;
    }

    public static boolean hasWorkableTemperatureEnd(ItemStack stack) {
        return getWorkableTemperatureEnd(stack) > 0;
    }

    public static boolean hasWorkableTemperatureRange(ItemStack stack) {
        return hasWorkableTemperatureStart(stack) && hasWorkableTemperatureEnd(stack);
    }

    public static void setWorkableTemperatureStart(ItemStack stack, int temperature) {
        if (stack.isEmpty()) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(WORKABLE_TEMPERATURE_START_KEY, Math.max(0, temperature));
    }

    public static void setWorkableTemperatureEnd(ItemStack stack, int temperature) {
        if (stack.isEmpty()) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(WORKABLE_TEMPERATURE_END_KEY, Math.max(0, temperature));
    }
}
