package com.sigmundgranaas.forgero.smithing.minigame;

import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules.TemperatureStage;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureState;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class SmithingRewardData {
	public static final int QUENCH_CONTEXT_IN_PROGRESS = 0;
	public static final int QUENCH_CONTEXT_FINAL = 1;

	private static final String QUENCH_SESSION_ACTIVE_KEY = "forgero_quenchSessionActive";
	private static final String QUENCH_SESSION_CONTEXT_KEY = "forgero_quenchSessionContext";
	private static final String QUENCH_SESSION_START_TEMP_KEY = "forgero_quenchSessionStartTemperature";
	private static final String QUENCH_SESSION_START_STAGE_KEY = "forgero_quenchSessionStartStage";
	private static final String TOTAL_QUENCH_SESSIONS_KEY = "forgero_totalQuenchSessions";
	private static final String IN_PROGRESS_QUENCH_SESSIONS_KEY = "forgero_inProgressQuenchSessions";
	private static final String FINAL_QUENCH_SESSIONS_KEY = "forgero_finalQuenchSessions";
	private static final String FINAL_QUENCH_START_TEMP_KEY = "forgero_finalQuenchStartTemperature";
	private static final String FINAL_QUENCH_START_STAGE_KEY = "forgero_finalQuenchStartStage";
	private static final String LAST_QUENCH_START_TEMP_KEY = "forgero_lastQuenchStartTemperature";
	private static final String LAST_QUENCH_START_STAGE_KEY = "forgero_lastQuenchStartStage";
	private static final String FINAL_QUENCH_ONE_GO_KEY = "forgero_finalQuenchCompletedInOneGo";
	private static final String QUENCHED_DURING_SMITHING_KEY = "forgero_quenchedDuringSmithing";
	private static final String QUENCH_START_TEMPERATURES_KEY = "forgero_quenchStartTemperatures";
	private static final String QUENCH_START_STAGES_KEY = "forgero_quenchStartStages";
	private static final String QUENCH_CONTEXT_SEQUENCE_KEY = "forgero_quenchContextSequence";

	private SmithingRewardData() {
	}

	public static void beginQuenchSessionIfNeeded(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		NbtCompound nbt = stack.getOrCreateNbt();

		if (nbt.getBoolean(QUENCH_SESSION_ACTIVE_KEY)) {
			return;
		}

		int temperature = TemperatureState.currentTemperature(stack);
		int stage = stageIndexFor(TemperatureRules.stage(temperature, TemperatureRules.stages(stack)));
		boolean finalQuench = MorphedItem.needsQuench(stack);
		int context = finalQuench ? QUENCH_CONTEXT_FINAL : QUENCH_CONTEXT_IN_PROGRESS;

		nbt.putBoolean(QUENCH_SESSION_ACTIVE_KEY, true);
		nbt.putInt(QUENCH_SESSION_CONTEXT_KEY, context);
		nbt.putInt(QUENCH_SESSION_START_TEMP_KEY, temperature);
		nbt.putInt(QUENCH_SESSION_START_STAGE_KEY, stage);

		increment(nbt, TOTAL_QUENCH_SESSIONS_KEY);
		nbt.putInt(LAST_QUENCH_START_TEMP_KEY, temperature);
		nbt.putInt(LAST_QUENCH_START_STAGE_KEY, stage);
		appendInt(nbt, QUENCH_START_TEMPERATURES_KEY, temperature);
		appendInt(nbt, QUENCH_START_STAGES_KEY, stage);
		appendInt(nbt, QUENCH_CONTEXT_SEQUENCE_KEY, context);

		if (finalQuench) {
			increment(nbt, FINAL_QUENCH_SESSIONS_KEY);

			if (!nbt.contains(FINAL_QUENCH_START_TEMP_KEY)) {
				nbt.putInt(FINAL_QUENCH_START_TEMP_KEY, temperature);
				nbt.putInt(FINAL_QUENCH_START_STAGE_KEY, stage);
			}
		} else {
			increment(nbt, IN_PROGRESS_QUENCH_SESSIONS_KEY);
			nbt.putBoolean(QUENCHED_DURING_SMITHING_KEY, true);
		}
	}

	public static void completeFinalQuenchIfReady(ItemStack stack) {
		if (stack.isEmpty() || !MorphedItem.needsQuench(stack)) {
			return;
		}

		if (TemperatureState.currentTemperature(stack) > TemperatureState.DEFAULT_TEMPERATURE) {
			return;
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		boolean activeFinalSession = nbt.getBoolean(QUENCH_SESSION_ACTIVE_KEY)
				&& nbt.getInt(QUENCH_SESSION_CONTEXT_KEY) == QUENCH_CONTEXT_FINAL;
		boolean completedInOneGo = activeFinalSession
				&& finalQuenchSessions(stack) == 1
				&& nbt.getInt(QUENCH_SESSION_START_TEMP_KEY) > TemperatureState.DEFAULT_TEMPERATURE;

		nbt.putBoolean(FINAL_QUENCH_ONE_GO_KEY, completedInOneGo);
	}

	public static void clearActiveQuenchSession(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return;
		}

		NbtCompound nbt = stack.getNbt();

		if (nbt == null) {
			return;
		}

		nbt.remove(QUENCH_SESSION_ACTIVE_KEY);
		nbt.remove(QUENCH_SESSION_CONTEXT_KEY);
		nbt.remove(QUENCH_SESSION_START_TEMP_KEY);
		nbt.remove(QUENCH_SESSION_START_STAGE_KEY);
	}

	public static int totalQuenchSessions(ItemStack stack) {
		return intValue(stack, TOTAL_QUENCH_SESSIONS_KEY);
	}

	public static int inProgressQuenchSessions(ItemStack stack) {
		return intValue(stack, IN_PROGRESS_QUENCH_SESSIONS_KEY);
	}

	public static int finalQuenchSessions(ItemStack stack) {
		return intValue(stack, FINAL_QUENCH_SESSIONS_KEY);
	}

	public static int finalQuenchStartTemperature(ItemStack stack) {
		return intValue(stack, FINAL_QUENCH_START_TEMP_KEY);
	}

	public static int finalQuenchStartStage(ItemStack stack) {
		return intValue(stack, FINAL_QUENCH_START_STAGE_KEY);
	}

	public static int lastQuenchStartTemperature(ItemStack stack) {
		return intValue(stack, LAST_QUENCH_START_TEMP_KEY);
	}

	public static int lastQuenchStartStage(ItemStack stack) {
		return intValue(stack, LAST_QUENCH_START_STAGE_KEY);
	}

	public static boolean finalQuenchCompletedInOneGo(ItemStack stack) {
		return booleanValue(stack, FINAL_QUENCH_ONE_GO_KEY);
	}

	public static boolean quenchedDuringSmithing(ItemStack stack) {
		return booleanValue(stack, QUENCHED_DURING_SMITHING_KEY);
	}

	public static int[] quenchStartTemperatures(ItemStack stack) {
		return intArray(stack, QUENCH_START_TEMPERATURES_KEY);
	}

	public static int[] quenchStartStages(ItemStack stack) {
		return intArray(stack, QUENCH_START_STAGES_KEY);
	}

	public static int[] quenchContextSequence(ItemStack stack) {
		return intArray(stack, QUENCH_CONTEXT_SEQUENCE_KEY);
	}

	private static void increment(NbtCompound nbt, String key) {
		nbt.putInt(key, nbt.getInt(key) + 1);
	}

	private static void appendInt(NbtCompound nbt, String key, int value) {
		int[] existing = nbt.getIntArray(key);
		int[] updated = new int[existing.length + 1];

		System.arraycopy(existing, 0, updated, 0, existing.length);
		updated[existing.length] = value;

		nbt.putIntArray(key, updated);
	}

	private static int intValue(ItemStack stack, String key) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return 0;
		}

		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains(key) ? nbt.getInt(key) : 0;
	}

	private static boolean booleanValue(ItemStack stack, String key) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return false;
		}

		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.getBoolean(key);
	}

	private static int[] intArray(ItemStack stack, String key) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return new int[0];
		}

		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains(key) ? nbt.getIntArray(key) : new int[0];
	}

	private static int stageIndexFor(TemperatureStage stage) {
		return switch (stage) {
			case COLD -> 0;
			case WARM -> 1;
			case HOT -> 2;
			case WORKABLE -> 3;
			case OVERHEATED -> 4;
		};
	}
}
