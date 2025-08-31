package com.sigmundgranaas.forgero.smithing.condition.custom;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;

/**
 * Temperature-based predicates for smithing conditions
 */
public class TemperaturePredicates {

	// Stage indices, keep in sync with MinigameLogic.getStageIndex
	public static final int COLD = 0;
	public static final int WARM = 1;
	public static final int HOT = 2;
	public static final int VERY_HOT = 3;
	public static final int NEAR_MELT = 4;
	public static final int MOLTEN = 5;

	public static Predicate<MatchContext> perfectVeryHotFastPredicate() {
		return context ->
				context.get(MinecraftContextKeys.VERY_HOT_STAGE_FRACTION).orElse(0.0) == 1.0 &&
						context.get(MinecraftContextKeys.MISS_HITS).orElse(0) == 0 &&
						context.get(MinecraftContextKeys.FAST_MARKER_HITS).orElse(0) == 3;
	}



	public static Predicate<MatchContext> veryHotStageHitPredicate() {
		return context -> context.get(MinecraftContextKeys.VERY_HOT_STAGE_HITS).orElse(0) >= 6;
	}

	// --- New time-based (continuous) temperature predicates ---

	/**
	 * True if the item was in the Very Hot stage for 100% of the tracked minigame time
	 */
	public static Predicate<MatchContext> veryHotAllTimePredicate() {
		return context -> {
			int total = context.get(MinecraftContextKeys.TOTAL_STAGE_TICKS).orElse(0);
			int veryHot = context.get(MinecraftContextKeys.VERY_HOT_STAGE_TICKS).orElse(0);
			return total > 0 && veryHot == total;
		};
	}

	/**
	 * True if the item was in the Cold stage for at least the given fraction of time (0.0 - 1.0)
	 */
	public static Predicate<MatchContext> coldStageFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.COLD_STAGE_FRACTION).orElse(0.0) >= fraction;
	}

	// --- Stage transition predicates ---

	/**
	 * True if there were at least minCount transitions from 'fromStage' to 'toStage'.
	 */
	public static Predicate<MatchContext> hasTransition(int fromStage, int toStage, int minCount) {
		return context -> {
			int[] matrix = context.get(MinecraftContextKeys.STAGE_TRANSITION_MATRIX).orElse(null);
			if (matrix == null || matrix.length < 36) return false;
			int idx = fromStage * 6 + toStage;
			if (idx < 0 || idx >= matrix.length) return false;
			return matrix[idx] >= minCount;
		};
	}

	/**
	 * True if the exact contiguous chain of stages appears in order in the stage change sequence.
	 * The sequence includes the initial observed stage followed by each changed-to stage.
	 */
	public static Predicate<MatchContext> hasChain(int... chain) {
		return context -> {
			int[] seq = context.get(MinecraftContextKeys.STAGE_CHANGE_SEQUENCE).orElse(null);
			if (seq == null || seq.length == 0 || chain == null || chain.length == 0) return false;
			// Search for contiguous subsequence
			outer:
			for (int i = 0; i <= seq.length - chain.length; i++) {
				for (int j = 0; j < chain.length; j++) {
					if (seq[i + j] != chain[j]) continue outer;
				}
				return true;
			}
			return false;
		};
	}

	// Convenience predicates
	public static Predicate<MatchContext> veryHotToMoltenOnce() {
		return hasTransition(VERY_HOT, MOLTEN, 1);
	}

	public static Predicate<MatchContext> veryHotToHotToColdChain() {
		return hasChain(VERY_HOT, HOT, COLD);
	}
}
