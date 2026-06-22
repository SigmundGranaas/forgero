package com.sigmundgranaas.forgero.smithing.condition.custom;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;

public final class TemperaturePredicates {
	public static final int COLD = 0;
	public static final int WARM = 1;
	public static final int HOT = 2;
	public static final int WORKABLE = 3;
	public static final int OVERHEATED = 4;
	public static final int STAGE_COUNT = 5;

	private TemperaturePredicates() {
	}

	public static Predicate<MatchContext> workableStageHitsAtLeast(int minHits) {
		return context -> context.get(MinecraftContextKeys.WORKABLE_STAGE_HITS).orElse(0) >= minHits;
	}

	public static Predicate<MatchContext> workableHitFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.WORKABLE_STAGE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> coldHitFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.COLD_STAGE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> warmHitFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.WARM_STAGE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> hotHitFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.HOT_STAGE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> overheatedHitFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.OVERHEATED_STAGE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> overheatedHitsAtMost(int maxHits) {
		return context -> context.get(MinecraftContextKeys.OVERHEATED_STAGE_HITS).orElse(0) <= maxHits;
	}

	public static Predicate<MatchContext> missHitsAtMost(int maxMisses) {
		return context -> context.get(MinecraftContextKeys.MISS_HITS).orElse(0) <= maxMisses;
	}

	public static Predicate<MatchContext> coolingMarkerHitsAtLeast(int minHits) {
		return context -> context.get(MinecraftContextKeys.COOLING_MARKER_HITS).orElse(0) >= minHits;
	}

	public static Predicate<MatchContext> quenchCountAtLeast(int minQuenches) {
		return context -> context.get(MinecraftContextKeys.QUENCH_COUNT).orElse(0) >= minQuenches;
	}

	public static Predicate<MatchContext> reheatCountAtLeast(int minReheats) {
		return context -> context.get(MinecraftContextKeys.REHEAT_COUNT).orElse(0) >= minReheats;
	}

	public static Predicate<MatchContext> reheatedAfterEveryQuench() {
		return context -> {
			int quenches = context.get(MinecraftContextKeys.QUENCH_COUNT).orElse(0);
			int reheats = context.get(MinecraftContextKeys.REHEAT_COUNT).orElse(0);
			return quenches > 0 && reheats >= quenches;
		};
	}

	public static Predicate<MatchContext> cleanWorkableRun() {
		return context ->
				context.get(MinecraftContextKeys.MISS_HITS).orElse(0) == 0
						&& context.get(MinecraftContextKeys.OVERHEATED_STAGE_HITS).orElse(0) == 0
						&& context.get(MinecraftContextKeys.WORKABLE_STAGE_HITS).orElse(0) > 0;
	}

	public static Predicate<MatchContext> hasTransition(int fromStage, int toStage, int minCount) {
		return context -> {
			int[] matrix = context.get(MinecraftContextKeys.STAGE_TRANSITION_MATRIX).orElse(null);

			if (matrix == null || matrix.length < STAGE_COUNT * STAGE_COUNT) {
				return false;
			}

			if (!isValidStage(fromStage) || !isValidStage(toStage)) {
				return false;
			}

			return matrix[fromStage * STAGE_COUNT + toStage] >= minCount;
		};
	}

	public static Predicate<MatchContext> hasChain(int... chain) {
		return context -> {
			int[] seq = context.get(MinecraftContextKeys.STAGE_CHANGE_SEQUENCE).orElse(null);

			if (seq == null || seq.length == 0 || chain == null || chain.length == 0) {
				return false;
			}

			outer:
			for (int i = 0; i <= seq.length - chain.length; i++) {
				for (int j = 0; j < chain.length; j++) {
					if (seq[i + j] != chain[j]) {
						continue outer;
					}
				}

				return true;
			}

			return false;
		};
	}

	private static boolean isValidStage(int stage) {
		return stage >= 0 && stage < STAGE_COUNT;
	}
}
