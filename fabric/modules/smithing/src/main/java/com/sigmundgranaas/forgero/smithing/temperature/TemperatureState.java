package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public record TemperatureState(int temperature, int quenchCount, int reheatCount) {
	public static final String TEMPERATURE_KEY = "forgero_temperature";
	public static final String QUENCH_COUNT_KEY = "forgero_quench_count";
	public static final String REHEAT_COUNT_KEY = "forgero_reheat_count";
	public static final int DEFAULT_TEMPERATURE = 20;
	public static final int MIN_TEMPERATURE = 0;

	public static TemperatureState from(ItemStack stack) {
		return new TemperatureState(
				currentTemperature(stack),
				quenchCount(stack),
				reheatCount(stack)
		);
	}

	public static int currentTemperature(ItemStack stack) {
		if (stack.isEmpty()) {
			return DEFAULT_TEMPERATURE;
		}

		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains(TEMPERATURE_KEY)) {
			return DEFAULT_TEMPERATURE;
		}

		return TemperatureProfile.from(stack).clamp(nbt.getInt(TEMPERATURE_KEY));
	}

	public static void setTemperature(ItemStack stack, int temperature) {
		if (stack.isEmpty()) {
			return;
		}

		stack.getOrCreateNbt().putInt(TEMPERATURE_KEY, TemperatureProfile.from(stack).clamp(temperature));
	}

	public static boolean hasStoredTemperature(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains(TEMPERATURE_KEY);
	}

	public static int quenchCount(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return 0;
		}

		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains(QUENCH_COUNT_KEY) ? Math.max(0, nbt.getInt(QUENCH_COUNT_KEY)) : 0;
	}

	public static int reheatCount(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return 0;
		}

		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains(REHEAT_COUNT_KEY) ? Math.max(0, nbt.getInt(REHEAT_COUNT_KEY)) : 0;
	}

	public static void setQuenchCount(ItemStack stack, int count) {
		if (stack.isEmpty()) {
			return;
		}

		stack.getOrCreateNbt().putInt(QUENCH_COUNT_KEY, Math.max(0, count));
	}

	public static void setReheatCount(ItemStack stack, int count) {
		if (stack.isEmpty()) {
			return;
		}

		stack.getOrCreateNbt().putInt(REHEAT_COUNT_KEY, Math.max(0, count));
	}

	public static void incrementQuenchCount(ItemStack stack) {
		setQuenchCount(stack, quenchCount(stack) + 1);
	}

	public static void incrementReheatCount(ItemStack stack) {
		setReheatCount(stack, reheatCount(stack) + 1);
	}

	public static void copyFrom(ItemStack source, ItemStack target) {
		setQuenchCount(target, quenchCount(source));
		setReheatCount(target, reheatCount(source));
		setTemperature(target, currentTemperature(source));
	}

	public static void removeFrom(NbtCompound nbt) {
		nbt.remove(TEMPERATURE_KEY);
		nbt.remove(QUENCH_COUNT_KEY);
		nbt.remove(REHEAT_COUNT_KEY);
	}
}
