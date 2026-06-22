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

	public static Predicate<MatchContext> missHitsAtLeast(int minMisses) {
		return context -> context.get(MinecraftContextKeys.MISS_HITS).orElse(0) >= minMisses;
	}

	public static Predicate<MatchContext> noMisses() {
		return missHitsAtMost(0);
	}

	public static Predicate<MatchContext> missHitsAtMost(int maxMisses) {
		return context -> context.get(MinecraftContextKeys.MISS_HITS).orElse(0) <= maxMisses;
	}

	public static Predicate<MatchContext> coolingMarkerHitsAtLeast(int minHits) {
		return context -> context.get(MinecraftContextKeys.COOLING_MARKER_HITS).orElse(0) >= minHits;
	}

	public static Predicate<MatchContext> coolingMarkerHitsAtMost(int maxHits) {
		return context -> context.get(MinecraftContextKeys.COOLING_MARKER_HITS).orElse(0) <= maxHits;
	}

	public static Predicate<MatchContext> coolingMarkerHitsInStageAtLeast(int stage, int minHits) {
		return context -> coolingMarkerHitsInStage(context, stage) >= minHits;
	}

	public static Predicate<MatchContext> coolingMarkerHitsInStageAtMost(int stage, int maxHits) {
		return context -> coolingMarkerHitsInStage(context, stage) <= maxHits;
	}

	public static Predicate<MatchContext> perfectStrikeFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.PERFECT_STRIKE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> goodStrikeFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.GOOD_STRIKE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> poorStrikeFractionAtLeast(double fraction) {
		return context -> context.get(MinecraftContextKeys.POOR_STRIKE_FRACTION).orElse(0.0) >= fraction;
	}

	public static Predicate<MatchContext> poorStrikeFractionAtMost(double fraction) {
		return context -> context.get(MinecraftContextKeys.POOR_STRIKE_FRACTION).orElse(0.0) <= fraction;
	}

	public static Predicate<MatchContext> perfectStrikesAtLeast(int minHits) {
		return context -> context.get(MinecraftContextKeys.PERFECT_STRIKES).orElse(0) >= minHits;
	}

	public static Predicate<MatchContext> goodStrikesAtLeast(int minHits) {
		return context -> context.get(MinecraftContextKeys.GOOD_STRIKES).orElse(0) >= minHits;
	}

	public static Predicate<MatchContext> poorStrikesAtMost(int maxHits) {
		return context -> context.get(MinecraftContextKeys.POOR_STRIKES).orElse(0) <= maxHits;
	}

	public static Predicate<MatchContext> noPoorStrikes() {
		return poorStrikesAtMost(0);
	}

	public static Predicate<MatchContext> perfectStrikeStreakAtLeast(int minStreak) {
		return context -> context.get(MinecraftContextKeys.PERFECT_STRIKE_STREAK).orElse(0) >= minStreak;
	}

	public static Predicate<MatchContext> skilledStrikeStreakAtLeast(int minStreak) {
		return context -> context.get(MinecraftContextKeys.SKILLED_STRIKE_STREAK).orElse(0) >= minStreak;
	}

	public static Predicate<MatchContext> perfectCoolingStrikesAtLeast(int minHits) {
		return context -> context.get(MinecraftContextKeys.PERFECT_COOLING_STRIKES).orElse(0) >= minHits;
	}

	public static Predicate<MatchContext> goodCoolingStrikesAtLeast(int minHits) {
		return context -> context.get(MinecraftContextKeys.GOOD_COOLING_STRIKES).orElse(0) >= minHits;
	}

	public static Predicate<MatchContext> poorCoolingStrikesAtMost(int maxHits) {
		return context -> context.get(MinecraftContextKeys.POOR_COOLING_STRIKES).orElse(0) <= maxHits;
	}

	public static Predicate<MatchContext> quenchCountAtLeast(int minQuenches) {
		return context -> context.get(MinecraftContextKeys.QUENCH_COUNT).orElse(0) >= minQuenches;
	}

	public static Predicate<MatchContext> totalQuenchSessionsAtLeast(int minSessions) {
		return context -> context.get(MinecraftContextKeys.TOTAL_QUENCH_SESSIONS).orElse(0) >= minSessions;
	}

	public static Predicate<MatchContext> totalQuenchSessionsAtMost(int maxSessions) {
		return context -> context.get(MinecraftContextKeys.TOTAL_QUENCH_SESSIONS).orElse(0) <= maxSessions;
	}

	public static Predicate<MatchContext> inProgressQuenchSessionsAtLeast(int minSessions) {
		return context -> context.get(MinecraftContextKeys.IN_PROGRESS_QUENCH_SESSIONS).orElse(0) >= minSessions;
	}

	public static Predicate<MatchContext> inProgressQuenchSessionsAtMost(int maxSessions) {
		return context -> context.get(MinecraftContextKeys.IN_PROGRESS_QUENCH_SESSIONS).orElse(0) <= maxSessions;
	}

	public static Predicate<MatchContext> finalQuenchSessionsAtMost(int maxSessions) {
		return context -> context.get(MinecraftContextKeys.FINAL_QUENCH_SESSIONS).orElse(0) <= maxSessions;
	}

	public static Predicate<MatchContext> finalQuenchSessionsAtLeast(int minSessions) {
		return context -> context.get(MinecraftContextKeys.FINAL_QUENCH_SESSIONS).orElse(0) >= minSessions;
	}

	public static Predicate<MatchContext> finalQuenchCompletedInOneGo() {
		return context -> context.get(MinecraftContextKeys.FINAL_QUENCH_COMPLETED_IN_ONE_GO).orElse(false);
	}

	public static Predicate<MatchContext> quenchedDuringSmithing() {
		return context -> context.get(MinecraftContextKeys.QUENCHED_DURING_SMITHING).orElse(false);
	}

	public static Predicate<MatchContext> noInProgressQuenching() {
		return context -> !context.get(MinecraftContextKeys.QUENCHED_DURING_SMITHING).orElse(false);
	}

	public static Predicate<MatchContext> finalQuenchStartedInStage(int stage) {
		return context -> isValidStage(stage)
				&& context.get(MinecraftContextKeys.FINAL_QUENCH_START_STAGE).orElse(-1) == stage;
	}

	public static Predicate<MatchContext> finalQuenchStartedInAnyStage(int... stages) {
		return context -> {
			int finalStage = context.get(MinecraftContextKeys.FINAL_QUENCH_START_STAGE).orElse(-1);

			if (!isValidStage(finalStage) || stages == null || stages.length == 0) {
				return false;
			}

			for (int stage : stages) {
				if (finalStage == stage) {
					return true;
				}
			}

			return false;
		};
	}

	public static Predicate<MatchContext> finalQuenchStartedHotOrWorkable() {
		return finalQuenchStartedInAnyStage(HOT, WORKABLE);
	}

	public static Predicate<MatchContext> finalQuenchStartedAtTemperatureAtLeast(int minTemperature) {
		return context -> context.get(MinecraftContextKeys.FINAL_QUENCH_START_TEMPERATURE).orElse(0) >= minTemperature;
	}

	public static Predicate<MatchContext> finalQuenchStartedAtTemperatureAtMost(int maxTemperature) {
		return context -> context.get(MinecraftContextKeys.FINAL_QUENCH_START_TEMPERATURE).orElse(0) <= maxTemperature;
	}

	public static Predicate<MatchContext> lastQuenchStartedInStage(int stage) {
		return context -> isValidStage(stage)
				&& context.get(MinecraftContextKeys.LAST_QUENCH_START_STAGE).orElse(-1) == stage;
	}

	public static Predicate<MatchContext> lastQuenchStartedAtTemperatureAtLeast(int minTemperature) {
		return context -> context.get(MinecraftContextKeys.LAST_QUENCH_START_TEMPERATURE).orElse(0) >= minTemperature;
	}

	public static Predicate<MatchContext> lastQuenchStartedAtTemperatureAtMost(int maxTemperature) {
		return context -> context.get(MinecraftContextKeys.LAST_QUENCH_START_TEMPERATURE).orElse(0) <= maxTemperature;
	}

	public static Predicate<MatchContext> quenchStartedInStageAtLeast(int stage, int minCount) {
		return context -> {
			if (!isValidStage(stage)) {
				return false;
			}

			int[] stages = context.get(MinecraftContextKeys.QUENCH_START_STAGES).orElse(null);

			if (stages == null || stages.length == 0) {
				return false;
			}

			int count = 0;

			for (int value : stages) {
				if (value == stage) {
					count++;
				}
			}

			return count >= minCount;
		};
	}

	public static Predicate<MatchContext> quenchStartedAtTemperatureAtLeast(int minTemperature, int minCount) {
		return context -> countQuenchTemperatures(context, temperature -> temperature >= minTemperature) >= minCount;
	}

	public static Predicate<MatchContext> quenchStartedAtTemperatureAtMost(int maxTemperature, int minCount) {
		return context -> countQuenchTemperatures(context, temperature -> temperature <= maxTemperature) >= minCount;
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

	private static int coolingMarkerHitsInStage(MatchContext context, int stage) {
		if (!isValidStage(stage)) {
			return 0;
		}

		int[] stageCounts = context.get(MinecraftContextKeys.COOLING_STAGE_HIT_COUNTS).orElse(null);

		if (stageCounts != null && stageCounts.length > stage) {
			return Math.max(0, stageCounts[stage]);
		}

		return switch (stage) {
			case COLD -> context.get(MinecraftContextKeys.COOLING_COLD_STAGE_HITS).orElse(0);
			case WARM -> context.get(MinecraftContextKeys.COOLING_WARM_STAGE_HITS).orElse(0);
			case HOT -> context.get(MinecraftContextKeys.COOLING_HOT_STAGE_HITS).orElse(0);
			case WORKABLE -> context.get(MinecraftContextKeys.COOLING_WORKABLE_STAGE_HITS).orElse(0);
			case OVERHEATED -> context.get(MinecraftContextKeys.COOLING_OVERHEATED_STAGE_HITS).orElse(0);
			default -> 0;
		};
	}

	private static int countQuenchTemperatures(MatchContext context, java.util.function.IntPredicate predicate) {
		int[] temperatures = context.get(MinecraftContextKeys.QUENCH_START_TEMPERATURES).orElse(null);

		if (temperatures == null || temperatures.length == 0) {
			return 0;
		}

		int count = 0;

		for (int temperature : temperatures) {
			if (predicate.test(temperature)) {
				count++;
			}
		}

		return count;
	}
}
