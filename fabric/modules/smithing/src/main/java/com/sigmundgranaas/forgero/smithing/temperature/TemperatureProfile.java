package com.sigmundgranaas.forgero.smithing.temperature;

import static com.sigmundgranaas.forgero.smithing.Attributes.MAX_TEMPERATURE;
import static com.sigmundgranaas.forgero.smithing.Attributes.WORKABLE_TEMPERATURE_END;
import static com.sigmundgranaas.forgero.smithing.Attributes.WORKABLE_TEMPERATURE_START;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.customdata.DataVisitor;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules.TemperatureStage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public record TemperatureProfile(int maxTemperature, int workableStart, int workableEnd, List<TemperatureBand> bands) {
	public static final String MAX_TEMPERATURE_KEY = "forgero_max_temperature";
	public static final String WORKABLE_TEMPERATURE_START_KEY = "forgero_workable_temperature_start";
	public static final String WORKABLE_TEMPERATURE_END_KEY = "forgero_workable_temperature_end";
	public static final String TEMPERATURE_BANDS_KEY = "forgero_temperature_bands";
	private static final String CUSTOM_DATA_KEY = "temperature_profile";
	private static final String STAGE_KEY = "stage";
	private static final String START_KEY = "start";
	private static final String END_KEY = "end";
	private static final DataVisitor<JsonTemperatureProfile> TEMPERATURE_PROFILE_VISITOR =
			DataVisitor.of(JsonTemperatureProfile.class, CUSTOM_DATA_KEY);

	public static final TemperatureProfile EMPTY = new TemperatureProfile(0, 0, 0);

	public TemperatureProfile {
		maxTemperature = Math.max(0, maxTemperature);
		workableStart = Math.max(0, workableStart);
		workableEnd = Math.max(0, workableEnd);
		bands = List.copyOf(bands == null ? List.of() : bands);
	}

	public TemperatureProfile(int maxTemperature, int workableStart, int workableEnd) {
		this(
				Math.max(0, maxTemperature),
				Math.max(0, workableStart),
				Math.max(0, workableEnd),
				legacyBands(maxTemperature, workableStart, workableEnd)
		);
	}

	public static TemperatureProfile of(int maxTemperature, int workableStart, int workableEnd) {
		int max = Math.max(0, maxTemperature);
		int start = Math.max(0, workableStart);
		int end = Math.max(0, workableEnd);
		return new TemperatureProfile(
				max,
				start,
				end,
				legacyBands(max, start, end)
		);
	}

	public static TemperatureProfile of(int maxTemperature, List<TemperatureBand> bands) {
		int max = Math.max(0, maxTemperature);
		List<TemperatureBand> normalizedBands = normalizeBands(max, bands);
		TemperatureBand workable = firstBand(normalizedBands, TemperatureStage.WORKABLE).orElse(null);

		return new TemperatureProfile(
				Math.max(0, maxTemperature),
				workable == null ? 0 : workable.start(),
				workable == null ? 0 : workable.end(),
				normalizedBands
		);
	}

	public static TemperatureProfile from(ItemStack stack) {
		if (stack.isEmpty()) {
			return EMPTY;
		}

		NbtCompound nbt = stack.getNbt();
		Optional<State> state = StateService.INSTANCE.convert(stack);

		Optional<TemperatureProfile> serializedProfile = readSerializedProfile(nbt);

		if (serializedProfile.isPresent()) {
			return serializedProfile.get();
		}

		Optional<TemperatureProfile> legacyNbtProfile = readLegacyProfile(nbt);

		if (legacyNbtProfile.isPresent()) {
			return legacyNbtProfile.get();
		}

		Optional<TemperatureProfile> customDataProfile = state
				.flatMap(value -> value.customData().accept(TEMPERATURE_PROFILE_VISITOR))
				.map(JsonTemperatureProfile::toProfile);

		if (customDataProfile.isPresent()) {
			return customDataProfile.get();
		}

		return fromAttributes(state);
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
		return hasWorkableStart() && hasWorkableEnd() && workableEnd > workableStart;
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
		nbt.put(TEMPERATURE_BANDS_KEY, writeBands());
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
		nbt.remove(TEMPERATURE_BANDS_KEY);
	}

	public Optional<TemperatureBand> band(TemperatureStage stage) {
		return firstBand(bands, stage);
	}

	private NbtList writeBands() {
		NbtList list = new NbtList();

		for (TemperatureBand band : bands) {
			NbtCompound compound = new NbtCompound();
			compound.putString(STAGE_KEY, band.stage().name().toLowerCase(Locale.ENGLISH));
			compound.putInt(START_KEY, band.start());
			compound.putInt(END_KEY, band.end());
			list.add(compound);
		}

		return list;
	}

	private static Optional<TemperatureProfile> readSerializedProfile(NbtCompound nbt) {
		if (nbt == null || !nbt.contains(TEMPERATURE_BANDS_KEY, NbtElement.LIST_TYPE)) {
			return Optional.empty();
		}

		int max = readProfileValue(nbt, MAX_TEMPERATURE_KEY).orElse(0);

		if (max <= 0) {
			return Optional.empty();
		}

		NbtList list = nbt.getList(TEMPERATURE_BANDS_KEY, NbtCompound.COMPOUND_TYPE);
		List<TemperatureBand> bands = new ArrayList<>();

		for (int i = 0; i < list.size(); i++) {
			NbtCompound compound = list.getCompound(i);
			TemperatureStage stage = stageFrom(compound.getString(STAGE_KEY)).orElse(null);

			if (stage == null || !compound.contains(END_KEY)) {
				continue;
			}

			bands.add(new TemperatureBand(
					stage,
					compound.contains(START_KEY) ? compound.getInt(START_KEY) : TemperatureState.MIN_TEMPERATURE,
					compound.getInt(END_KEY)
			));
		}

		return bands.isEmpty() ? Optional.empty() : Optional.of(of(max, bands));
	}

	private static Optional<TemperatureProfile> readLegacyProfile(NbtCompound nbt) {
		Optional<Integer> max = readProfileValue(nbt, MAX_TEMPERATURE_KEY);

		if (max.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(of(
				max.get(),
				readProfileValue(nbt, WORKABLE_TEMPERATURE_START_KEY).orElse(0),
				readProfileValue(nbt, WORKABLE_TEMPERATURE_END_KEY).orElse(0)
		));
	}

	private static Optional<Integer> readProfileValue(NbtCompound nbt, String key) {
		if (nbt == null || !nbt.contains(key)) {
			return Optional.empty();
		}

		return Optional.of(Math.max(0, nbt.getInt(key)));
	}

	private static TemperatureProfile fromAttributes(Optional<State> state) {
		return of(
				attributeValue(state, MAX_TEMPERATURE),
				attributeValue(state, WORKABLE_TEMPERATURE_START),
				attributeValue(state, WORKABLE_TEMPERATURE_END)
		);
	}

	private static int attributeValue(Optional<State> state, String attribute) {
		return state.map(value -> Math.max(0, ComputedAttribute.of(value, attribute).asInt())).orElse(0);
	}

	private static List<TemperatureBand> normalizeBands(int maxTemperature, List<TemperatureBand> rawBands) {
		if (maxTemperature <= 0 || rawBands == null || rawBands.isEmpty()) {
			return List.of();
		}

		List<TemperatureBand> normalized = new ArrayList<>();
		int previousEnd = TemperatureState.MIN_TEMPERATURE - 1;

		for (TemperatureBand band : rawBands) {
			if (band == null || band.stage() == null) {
				continue;
			}

			int start = Math.max(TemperatureState.MIN_TEMPERATURE, band.start());
			int end = Math.min(maxTemperature, Math.max(TemperatureState.MIN_TEMPERATURE, band.end()));

			if (normalized.isEmpty() && start > TemperatureState.MIN_TEMPERATURE) {
				normalized.add(new TemperatureBand(TemperatureStage.COLD, TemperatureState.MIN_TEMPERATURE, start - 1));
				previousEnd = start - 1;
			}

			if (start > previousEnd + 1) {
				start = previousEnd + 1;
			}

			if (start <= previousEnd) {
				start = previousEnd + 1;
			}

			if (end < start) {
				continue;
			}

			normalized.add(new TemperatureBand(band.stage(), start, end));
			previousEnd = end;

			if (previousEnd >= maxTemperature) {
				break;
			}
		}

		if (!normalized.isEmpty() && previousEnd < maxTemperature) {
			normalized.add(new TemperatureBand(TemperatureStage.OVERHEATED, previousEnd + 1, maxTemperature));
		}

		return List.copyOf(normalized);
	}

	private static List<TemperatureBand> legacyBands(int maxTemperature, int workableStart, int workableEnd) {
		int max = Math.max(0, maxTemperature);

		if (max <= 0) {
			return List.of();
		}

		int start = Math.max(0, workableStart);
		int end = Math.max(0, workableEnd);

		if (start > TemperatureState.DEFAULT_TEMPERATURE && end > start && end <= max) {
			int lowerSplit = Math.max(TemperatureState.MIN_TEMPERATURE, start / 2);
			int upperSplit = end + Math.max(0, (max - end) / 2);

			List<TemperatureBand> bands = new ArrayList<>();
			addBandIfValid(bands, TemperatureStage.COLD, TemperatureState.MIN_TEMPERATURE, lowerSplit);
			addBandIfValid(bands, TemperatureStage.WARM, lowerSplit + 1, start - 1);
			addBandIfValid(bands, TemperatureStage.WORKABLE, start, end);
			addBandIfValid(bands, TemperatureStage.HOT, end + 1, upperSplit);
			addBandIfValid(bands, TemperatureStage.OVERHEATED, upperSplit + 1, max);
			return normalizeBands(max, bands);
		}

		int coldEnd = Math.max(TemperatureState.MIN_TEMPERATURE, max / 4);
		int warmEnd = Math.max(coldEnd, max / 2);
		int hotEnd = Math.max(warmEnd, (max * 3) / 4);

		List<TemperatureBand> bands = new ArrayList<>();
		addBandIfValid(bands, TemperatureStage.COLD, TemperatureState.MIN_TEMPERATURE, coldEnd);
		addBandIfValid(bands, TemperatureStage.WARM, coldEnd + 1, warmEnd);
		addBandIfValid(bands, TemperatureStage.HOT, warmEnd + 1, hotEnd);
		addBandIfValid(bands, TemperatureStage.OVERHEATED, hotEnd + 1, max);
		return normalizeBands(max, bands);
	}

	private static void addBandIfValid(List<TemperatureBand> bands, TemperatureStage stage, int start, int end) {
		if (end >= start) {
			bands.add(new TemperatureBand(stage, start, end));
		}
	}

	private static Optional<TemperatureBand> firstBand(List<TemperatureBand> bands, TemperatureStage stage) {
		return bands.stream()
				.filter(band -> band.stage() == stage)
				.findFirst();
	}

	private static Optional<TemperatureStage> stageFrom(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		String normalized = value.toUpperCase(Locale.ENGLISH).replace('-', '_');

		if ("OVERHEAT".equals(normalized)) {
			normalized = TemperatureStage.OVERHEATED.name();
		}

		try {
			return Optional.of(TemperatureStage.valueOf(normalized));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}

	public record TemperatureBand(TemperatureStage stage, int start, int end) {
		public TemperatureBand {
			start = Math.max(TemperatureState.MIN_TEMPERATURE, start);
			end = Math.max(TemperatureState.MIN_TEMPERATURE, end);
		}

		public boolean contains(int temperature) {
			return temperature >= start && temperature <= end;
		}
	}

	private static final class JsonTemperatureProfile {
		@SerializedName(value = "max", alternate = {"max_temperature", "maxTemperature"})
		private int maxTemperature;
		@SerializedName(value = "stages", alternate = {"bands", "ranges"})
		private List<JsonTemperatureBand> stages = List.of();

		private TemperatureProfile toProfile() {
			List<TemperatureBand> bands = new ArrayList<>();
			int previousEnd = TemperatureState.MIN_TEMPERATURE - 1;

			for (JsonTemperatureBand stage : stages == null ? List.<JsonTemperatureBand>of() : stages) {
				Optional<TemperatureStage> stageId = stage.stage();

				if (stageId.isEmpty()) {
					continue;
				}

				int start = stage.start == null ? previousEnd + 1 : stage.start;
				bands.add(new TemperatureBand(stageId.get(), start, stage.end));
				previousEnd = stage.end;
			}

			return TemperatureProfile.of(maxTemperature, bands);
		}
	}

	private static final class JsonTemperatureBand {
		private String id;
		private String stage;
		private Integer start;
		private int end;

		private Optional<TemperatureStage> stage() {
			return stageFrom(stage == null ? id : stage);
		}
	}
}
