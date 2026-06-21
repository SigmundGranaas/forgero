package com.sigmundgranaas.forgero.smithing.temperature;

import java.util.ArrayList;
import java.util.Objects;

import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;
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

public final class TemperatureRules {
	private static final int REAL_CAP = 10000;

	private static final int COLOR_RED = 0xFFFF0000;
	private static final int COLOR_ORANGE_RED = 0xFFFF3300;
	private static final int COLOR_ORANGE = 0xFFFF9900;
	private static final int COLOR_YELLOW = 0xFFFFCC33;
	private static final int COLOR_PALE = 0xFFFFFF99;
	private static final int COLOR_AMBIENT = 0x00FFFFFF;

	private TemperatureRules() {
	}

	public static class TemperatureStages {
		public final int ambient;
		public final int coldEnd;
		public final int warmEnd;
		public final int hotStart;
		public final int hotEnd;
		public final int overheatedStart;
		public final int workableStart;
		public final int workableEnd;
		public final int max;

		public TemperatureStages(int ambient, int coldEnd, int warmEnd, int hotStart, int hotEnd, int overheatedStart, int workableStart, int workableEnd, int max) {
			this.ambient = ambient;
			this.coldEnd = coldEnd;
			this.warmEnd = warmEnd;
			this.hotStart = hotStart;
			this.hotEnd = hotEnd;
			this.overheatedStart = overheatedStart;
			this.workableStart = workableStart;
			this.workableEnd = workableEnd;
			this.max = max;
		}
	}

	public enum TemperatureStage {
		COLD, WARM, HOT, WORKABLE, OVERHEATED
	}

	public static boolean canTrackTemperature(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		if (stack.getItem() instanceof ToolStateItem
				&& !(stack.getItem() instanceof MorphedItem)
				&& !TemperatureState.hasStoredTemperature(stack)) {
			return false;
		}

		return TemperatureProfile.from(stack).hasMaxTemperature();
	}

	public static int clamp(int temperature, ItemStack stack) {
		return TemperatureProfile.from(stack).clamp(temperature);
	}

	public static void setTemperature(ItemStack stack, int temperature) {
		TemperatureState.setTemperature(stack, temperature);
	}

	public static void copyTemperatureData(ItemStack source, ItemStack target) {
		if (source.isEmpty() || target.isEmpty()) {
			return;
		}

		TemperatureProfile.from(source).writeTo(target);
		TemperatureState.copyFrom(source, target);
	}

	public static void removeTemperatureData(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return;
		}

		NbtCompound nbt = stack.getNbt();

		if (nbt == null) {
			return;
		}

		removeTemperatureData(nbt);

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

	public static void heat(ItemStack stack, int temperature) {
		int previous = TemperatureState.currentTemperature(stack);
		TemperatureState.setTemperature(stack, temperature);
		int current = TemperatureState.currentTemperature(stack);

		if (crossedIntoWorkable(previous, current, TemperatureProfile.from(stack))
				&& TemperatureState.quenchCount(stack) > TemperatureState.reheatCount(stack)) {
			TemperatureState.incrementReheatCount(stack);
		}
	}

	public static void quench(ItemStack stack, int temperature) {
		int previous = TemperatureState.currentTemperature(stack);
		TemperatureState.setTemperature(stack, temperature);
		int current = TemperatureState.currentTemperature(stack);

		if (crossedBelowWorkable(previous, current, TemperatureProfile.from(stack))) {
			TemperatureState.incrementQuenchCount(stack);
		}
	}

	public static TemperatureStages stages(ItemStack stack) {
		return stages(TemperatureProfile.from(stack));
	}

	public static TemperatureStages stages(int maxTemp, int workableStart, int workableEnd) {
		return stages(TemperatureProfile.of(maxTemp, workableStart, workableEnd));
	}

	public static TemperatureStages stages(TemperatureProfile profile) {
		int effectiveMax = Math.min(Math.max(profile.maxTemperature(), 1), REAL_CAP);
		int workableStart = profile.workableStart();
		int workableEnd = profile.workableEnd();

		if (!hasUsableWorkableRange(profile, effectiveMax)) {
			workableStart = 0;
			workableEnd = 0;
		}

		int coldEnd = (int) (effectiveMax * 0.25);
		int warmEnd = (int) (effectiveMax * 0.55);
		int hotStart = (int) (effectiveMax * 0.55);
		int hotEnd = (int) (effectiveMax * 0.85);
		int overheatedStart = hotEnd;

		return new TemperatureStages(
				TemperatureState.DEFAULT_TEMPERATURE,
				coldEnd,
				warmEnd,
				hotStart,
				hotEnd,
				overheatedStart,
				workableStart,
				workableEnd,
				effectiveMax
		);
	}

	public static TemperatureStage stage(int temperature, TemperatureStages stages) {
		if (isWorkable(temperature, stages)) {
			return TemperatureStage.WORKABLE;
		}
		if (temperature < stages.coldEnd) {
			return TemperatureStage.COLD;
		}
		if (temperature < stages.warmEnd) {
			return TemperatureStage.WARM;
		}
		if (temperature < stages.hotEnd) {
			return TemperatureStage.HOT;
		}
		if (temperature >= stages.overheatedStart) {
			return TemperatureStage.OVERHEATED;
		}

		return TemperatureStage.HOT;
	}

	public static boolean isWorkable(ItemStack stack) {
		return canTrackTemperature(stack) && isWorkable(TemperatureState.currentTemperature(stack), stages(stack));
	}

	public static boolean isWorkable(int temperature, TemperatureStages stages) {
		return stages.workableStart > 0
				&& stages.workableEnd > 0
				&& temperature >= stages.workableStart
				&& temperature <= stages.workableEnd;
	}

	public static int color(ItemStack stack) {
		return color(TemperatureState.currentTemperature(stack), stages(stack));
	}

	public static int color(int temperature, TemperatureStages stages) {
		if (stages.workableStart > 0 && stages.workableEnd > 0) {
			return colorWithWorkableRange(temperature, stages);
		}

		return colorWithoutWorkableRange(temperature);
	}

	public static int hudColor(TemperatureStage stage) {
		return switch (stage) {
			case COLD -> 0xFF2196F3;
			case WARM -> 0xFFFFEB3B;
			case HOT -> 0xFFFF9800;
			case WORKABLE -> 0xFF4CAF50;
			case OVERHEATED -> 0xFFF44336;
		};
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
		if (itemEntity == null || world == null) {
			return false;
		}

		return isBlockFilledWaterCauldron(world.getBlockState(itemEntity.getBlockPos()));
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
		removeTemperatureData(compound);

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

	private static void removeTemperatureData(NbtCompound nbt) {
		TemperatureProfile.removeFrom(nbt);
		TemperatureState.removeFrom(nbt);
	}

	private static boolean isEmptySerializedStackTag(NbtCompound parent, String key, NbtElement child) {
		return "tag".equals(key)
				&& child instanceof NbtCompound childCompound
				&& childCompound.isEmpty()
				&& parent.contains("id")
				&& parent.contains("Count");
	}

	private static int colorWithWorkableRange(int temperature, TemperatureStages stages) {
		int[][] colorScale = {
				{stages.max, COLOR_RED},
				{stages.workableEnd, COLOR_ORANGE_RED},
				{stages.workableStart, COLOR_YELLOW},
				{stages.warmEnd, COLOR_YELLOW},
				{stages.coldEnd, COLOR_PALE},
				{stages.ambient, COLOR_AMBIENT}
		};

		if (temperature >= colorScale[0][0]) {
			return colorScale[0][1];
		}

		if (temperature <= colorScale[colorScale.length - 1][0]) {
			return colorScale[colorScale.length - 1][1];
		}

		if (temperature >= stages.workableStart && temperature <= stages.workableEnd) {
			float workableProgress = (temperature - stages.workableStart) / (float) (stages.workableEnd - stages.workableStart);
			workableProgress = Math.max(0f, Math.min(1f, workableProgress));

			if (workableProgress < 0.5f) {
				return lerpColor(COLOR_YELLOW, COLOR_ORANGE, workableProgress * 2);
			}

			return lerpColor(COLOR_ORANGE, COLOR_ORANGE_RED, (workableProgress - 0.5f) * 2);
		}

		return interpolateScale(temperature, colorScale, COLOR_AMBIENT);
	}

	private static int colorWithoutWorkableRange(int temperature) {
		int[][] colorScale = {
				{1600, 0xFFFFFF99},
				{1500, 0xFFFFFF66},
				{1400, 0xFFFFCC33},
				{1300, 0xFFFF9900},
				{1200, 0xFFFF6600},
				{1100, 0xFFFF3300},
				{1000, 0xFFFF0000},
				{900, 0xFFCC0000},
				{800, 0xFF990000},
				{700, 0xFF660000},
				{600, 0xFF330000},
				{500, 0xFF220000},
				{20, 0x00FFFFFF}
		};

		if (temperature >= colorScale[0][0]) {
			return colorScale[0][1];
		}

		if (temperature <= colorScale[colorScale.length - 1][0]) {
			return colorScale[colorScale.length - 1][1];
		}

		return interpolateScale(temperature, colorScale, colorScale[colorScale.length - 1][1]);
	}

	private static int interpolateScale(int temperature, int[][] colorScale, int fallback) {
		for (int i = 0; i < colorScale.length - 1; i++) {
			int tHigh = colorScale[i][0];
			int tLow = colorScale[i + 1][0];
			int cHigh = colorScale[i][1];
			int cLow = colorScale[i + 1][1];

			if (tHigh != tLow && temperature >= tLow && temperature <= tHigh) {
				float t = (temperature - tLow) / (float) (tHigh - tLow);
				return lerpColor(cLow, cHigh, t);
			}
		}

		return fallback;
	}

	private static int lerpColor(int colorA, int colorB, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int aA = (colorA >> 24) & 0xFF;
		int aR = (colorA >> 16) & 0xFF;
		int aG = (colorA >> 8) & 0xFF;
		int aB = colorA & 0xFF;
		int bA = (colorB >> 24) & 0xFF;
		int bR = (colorB >> 16) & 0xFF;
		int bG = (colorB >> 8) & 0xFF;
		int bB = colorB & 0xFF;

		int r = (int) (aR + (bR - aR) * t);
		int g = (int) (aG + (bG - aG) * t);
		int b = (int) (aB + (bB - aB) * t);
		int a = (int) (aA + (bA - aA) * t);

		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static boolean crossedIntoWorkable(int previous, int current, TemperatureProfile profile) {
		int workableStart = validWorkableStart(profile);
		return workableStart > 0 && previous < workableStart && current >= workableStart;
	}

	private static boolean crossedBelowWorkable(int previous, int current, TemperatureProfile profile) {
		int workableStart = validWorkableStart(profile);
		return workableStart > 0 && previous >= workableStart && current < workableStart;
	}

	private static int validWorkableStart(TemperatureProfile profile) {
		int effectiveMax = Math.min(Math.max(profile.maxTemperature(), 1), REAL_CAP);
		return hasUsableWorkableRange(profile, effectiveMax) ? profile.workableStart() : 0;
	}

	private static boolean hasUsableWorkableRange(TemperatureProfile profile, int effectiveMax) {
		return profile.workableStart() > TemperatureState.DEFAULT_TEMPERATURE
				&& profile.workableEnd() > profile.workableStart()
				&& profile.workableEnd() <= effectiveMax;
	}
}
