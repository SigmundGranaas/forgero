package com.sigmundgranaas.forgero.smithing.temperature;

import static com.sigmundgranaas.forgero.smithing.Attributes.MAX_TEMPERATURE;
import static com.sigmundgranaas.forgero.smithing.Attributes.WORKABLE_TEMPERATURE_END;
import static com.sigmundgranaas.forgero.smithing.Attributes.WORKABLE_TEMPERATURE_START;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public record TemperatureProfile(int maxTemperature, int workableStart, int workableEnd) {
	public static final String MAX_TEMPERATURE_KEY = "forgero_max_temperature";
	public static final String WORKABLE_TEMPERATURE_START_KEY = "forgero_workable_temperature_start";
	public static final String WORKABLE_TEMPERATURE_END_KEY = "forgero_workable_temperature_end";

	public static final TemperatureProfile EMPTY = new TemperatureProfile(0, 0, 0);

	public static TemperatureProfile of(int maxTemperature, int workableStart, int workableEnd) {
		return new TemperatureProfile(
				Math.max(0, maxTemperature),
				Math.max(0, workableStart),
				Math.max(0, workableEnd)
		);
	}

	public static TemperatureProfile from(ItemStack stack) {
		if (stack.isEmpty()) {
			return EMPTY;
		}

		NbtCompound nbt = stack.getNbt();
		Optional<State> state = StateService.INSTANCE.convert(stack);

		return of(
				readProfileValue(nbt, MAX_TEMPERATURE_KEY).orElseGet(() -> attributeValue(state, MAX_TEMPERATURE)),
				readProfileValue(nbt, WORKABLE_TEMPERATURE_START_KEY).orElseGet(() -> attributeValue(state, WORKABLE_TEMPERATURE_START)),
				readProfileValue(nbt, WORKABLE_TEMPERATURE_END_KEY).orElseGet(() -> attributeValue(state, WORKABLE_TEMPERATURE_END))
		);
	}

	public boolean hasMaxTemperature() {
		return maxTemperature > 0;
	}

	public boolean hasWorkableStart() {
		return workableStart > 0;
	}

	public boolean hasWorkableEnd() {
		return workableEnd > 0;
	}

	public boolean hasWorkableRange() {
		return hasWorkableStart() && hasWorkableEnd();
	}

	public int clamp(int temperature) {
		int max = Math.max(maxTemperature, TemperatureState.DEFAULT_TEMPERATURE);
		return Math.max(TemperatureState.MIN_TEMPERATURE, Math.min(max, temperature));
	}

	public void writeTo(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putInt(MAX_TEMPERATURE_KEY, maxTemperature);
		nbt.putInt(WORKABLE_TEMPERATURE_START_KEY, workableStart);
		nbt.putInt(WORKABLE_TEMPERATURE_END_KEY, workableEnd);
	}

	public static void setMaxTemperature(ItemStack stack, int maxTemperature) {
		if (stack.isEmpty()) {
			return;
		}

		stack.getOrCreateNbt().putInt(MAX_TEMPERATURE_KEY, Math.max(0, maxTemperature));
	}

	public static void setWorkableStart(ItemStack stack, int temperature) {
		if (stack.isEmpty()) {
			return;
		}

		stack.getOrCreateNbt().putInt(WORKABLE_TEMPERATURE_START_KEY, Math.max(0, temperature));
	}

	public static void setWorkableEnd(ItemStack stack, int temperature) {
		if (stack.isEmpty()) {
			return;
		}

		stack.getOrCreateNbt().putInt(WORKABLE_TEMPERATURE_END_KEY, Math.max(0, temperature));
	}

	public static void removeFrom(NbtCompound nbt) {
		nbt.remove(MAX_TEMPERATURE_KEY);
		nbt.remove(WORKABLE_TEMPERATURE_START_KEY);
		nbt.remove(WORKABLE_TEMPERATURE_END_KEY);
	}

	private static Optional<Integer> readProfileValue(NbtCompound nbt, String key) {
		if (nbt == null || !nbt.contains(key)) {
			return Optional.empty();
		}

		return Optional.of(Math.max(0, nbt.getInt(key)));
	}

	private static int attributeValue(Optional<State> state, String attribute) {
		return state.map(value -> Math.max(0, ComputedAttribute.of(value, attribute).asInt())).orElse(0);
	}
}
