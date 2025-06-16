package com.sigmundgranaas.forgero.smithing.component;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HeatedItemComponent {
	private static final String HEAT_NBT_KEY = "forgero_heat";
	private static final String LAST_HEATED_NBT_KEY = "forgero_last_heated";
	private static final String WORKED_NBT_KEY = "forgero_worked";

	public static final int MAX_HEAT = 1000;
	public static final int MIN_WORKING_HEAT = 600;
	public static final int MAX_WORKING_HEAT = 900;
	public static final int IDEAL_HEAT_MIN = 700;
	public static final int IDEAL_HEAT_MAX = 800;
	public static final int COOLING_RATE = 2; // Heat lost per second when not near heat source
	public static final int HAMMER_HEAT_REDUCTION = 50;

	public static int getHeat(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		return nbt.getInt(HEAT_NBT_KEY);
	}

	public static void setHeat(ItemStack stack, int heat) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putInt(HEAT_NBT_KEY, Math.max(0, Math.min(MAX_HEAT, heat)));
		nbt.putLong(LAST_HEATED_NBT_KEY, System.currentTimeMillis());
	}

	public static long getLastHeated(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		return nbt.getLong(LAST_HEATED_NBT_KEY);
	}

	public static boolean isWorked(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		return nbt.getBoolean(WORKED_NBT_KEY);
	}

	public static void setWorked(ItemStack stack, boolean worked) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean(WORKED_NBT_KEY, worked);
	}

	public static boolean isHot(ItemStack stack) {
		return getHeat(stack) > 0;
	}

	public static boolean canPickupWithHands(ItemStack stack) {
		return getHeat(stack) <= 100; // Only pickup when cool enough
	}

	public static boolean canWork(ItemStack stack) {
		int heat = getHeat(stack);
		return heat >= MIN_WORKING_HEAT && heat <= MAX_WORKING_HEAT;
	}

	public static boolean isIdealWorkingHeat(ItemStack stack) {
		int heat = getHeat(stack);
		return heat >= IDEAL_HEAT_MIN && heat <= IDEAL_HEAT_MAX;
	}

	public static void updateHeat(ItemStack stack, boolean nearHeatSource) {
		if (stack.isEmpty()) return;

		long currentTime = System.currentTimeMillis();
		long lastHeated = getLastHeated(stack);
		int currentHeat = getHeat(stack);

		if (currentHeat <= 0) return; // Already cold

		// Calculate seconds since last update
		long secondsPassed = (currentTime - lastHeated) / 1000;

		if (nearHeatSource) {
			// Heat up when near heat source
			setHeat(stack, Math.min(MAX_HEAT, currentHeat + (int)(secondsPassed * 10)));
		} else {
			// Cool down naturally
			setHeat(stack, currentHeat - (int)(secondsPassed * COOLING_RATE));
		}
	}

	public static void addHeat(ItemStack stack, int amount) {
		int currentHeat = getHeat(stack);
		setHeat(stack, currentHeat + amount);
	}

	public static void reduceHeat(ItemStack stack, int amount) {
		int currentHeat = getHeat(stack);
		setHeat(stack, currentHeat - amount);
	}

	public static HeatLevel getHeatLevel(ItemStack stack) {
		int heat = getHeat(stack);
		if (heat >= 900) return HeatLevel.WHITE_HOT;
		if (heat >= 750) return HeatLevel.ORANGE_HOT;
		if (heat >= 600) return HeatLevel.RED_HOT;
		if (heat >= 300) return HeatLevel.WARM;
		if (heat >= 100) return HeatLevel.COOLING;
		return HeatLevel.COLD;
	}

	public static Text getHeatText(ItemStack stack) {
		HeatLevel level = getHeatLevel(stack);
		int heat = getHeat(stack);

		String heatText = String.format("Heat: %d/%d", heat, MAX_HEAT);

		return Text.literal(heatText + " (" + level.getDisplayName() + ")")
				.formatted(level.getColor());
	}

	public enum HeatLevel {
		COLD("Cold", Formatting.BLUE),
		COOLING("Cooling", Formatting.AQUA),
		WARM("Warm", Formatting.YELLOW),
		RED_HOT("Red Hot", Formatting.RED),
		ORANGE_HOT("Orange Hot", Formatting.GOLD),
		WHITE_HOT("White Hot", Formatting.WHITE);

		private final String displayName;
		private final Formatting color;

		HeatLevel(String displayName, Formatting color) {
			this.displayName = displayName;
			this.color = color;
		}

		public String getDisplayName() {
			return displayName;
		}

		public Formatting getColor() {
			return color;
		}
	}
}
