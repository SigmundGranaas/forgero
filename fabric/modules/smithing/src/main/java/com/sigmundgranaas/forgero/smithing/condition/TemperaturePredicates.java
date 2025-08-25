package com.sigmundgranaas.forgero.smithing.condition;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;

/**
 * Temperature-based predicates for smithing conditions
 */
public class TemperaturePredicates {

    /**
     * Predicate that checks if the majority of hits were in red temperature stage
     */
    public static Predicate<MatchContext> majorityRedStageHits() {
        return context -> {
            Integer redHits = context.get(MinecraftContextKeys.RED_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) redHits / totalHits > 0.5;
        };
    }

    /**
     * Predicate that checks if the majority of hits were in orange temperature stage
     */
    public static Predicate<MatchContext> majorityOrangeStageHits() {
        return context -> {
            Integer orangeHits = context.get(MinecraftContextKeys.ORANGE_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) orangeHits / totalHits > 0.5;
        };
    }

    /**
     * Predicate that checks if the majority of hits were in yellow temperature stage
     */
    public static Predicate<MatchContext> majorityYellowStageHits() {
        return context -> {
            Integer yellowHits = context.get(MinecraftContextKeys.YELLOW_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) yellowHits / totalHits > 0.5;
        };
    }

    /**
     * Predicate that checks if the majority of hits were in purple temperature stage
     */
    public static Predicate<MatchContext> majorityPurpleStageHits() {
        return context -> {
            Integer purpleHits = context.get(MinecraftContextKeys.PURPLE_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) purpleHits / totalHits > 0.5;
        };
    }


    /**
     * Predicate that checks if the majority of hits were in straw temperature stage
     */
    public static Predicate<MatchContext> majorityStrawStageHits() {
        return context -> {
            Integer strawHits = context.get(MinecraftContextKeys.STRAW_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) strawHits / totalHits > 0.5;
        };
    }

    /**
     * Predicate that checks if the majority of hits were in blue temperature stage
     */
    public static Predicate<MatchContext> majorityBlueStageHits() {
        return context -> {
            Integer blueHits = context.get(MinecraftContextKeys.BLUE_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) blueHits / totalHits > 0.5;
        };
    }

    /**
     * Predicate that checks if the majority of hits were in brown temperature stage
     */
    public static Predicate<MatchContext> majorityBrownStageHits() {
        return context -> {
            Integer brownHits = context.get(MinecraftContextKeys.BROWN_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) brownHits / totalHits > 0.5;
        };
    }

    /**
     * Predicate that checks if the majority of hits were in grey temperature stage
     */
    public static Predicate<MatchContext> majorityGreyStageHits() {
        return context -> {
            Integer greyHits = context.get(MinecraftContextKeys.GREY_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) greyHits / totalHits > 0.5;
        };
    }

    /**
     * Predicate that checks if at least X hits were in red temperature stage
     */
    public static Predicate<MatchContext> minRedStageHits(int minHits) {
        return context -> {
            Integer redHits = context.get(MinecraftContextKeys.RED_STAGE_HITS).orElse(0);
            return redHits >= minHits;
        };
    }

    /**
     * Predicate that checks if at least X hits were in orange temperature stage
     */
    public static Predicate<MatchContext> minOrangeStageHits(int minHits) {
        return context -> {
            Integer orangeHits = context.get(MinecraftContextKeys.ORANGE_STAGE_HITS).orElse(0);
            return orangeHits >= minHits;
        };
    }

    /**
     * Predicate that checks if at least X hits were in yellow temperature stage
     */
    public static Predicate<MatchContext> minYellowStageHits(int minHits) {
        return context -> {
            Integer yellowHits = context.get(MinecraftContextKeys.YELLOW_STAGE_HITS).orElse(0);
            return yellowHits >= minHits;
        };
    }

    /**
     * Predicate that checks if at least X hits were in purple temperature stage
     */
    public static Predicate<MatchContext> minPurpleStageHits(int minHits) {
        return context -> {
            Integer purpleHits = context.get(MinecraftContextKeys.PURPLE_STAGE_HITS).orElse(0);
            return purpleHits >= minHits;
        };
    }

    /**
     * Predicate that checks if at least X hits were in straw temperature stage
     */
    public static Predicate<MatchContext> minStrawStageHits(int minHits) {
        return context -> {
            Integer strawHits = context.get(MinecraftContextKeys.STRAW_STAGE_HITS).orElse(0);
            return strawHits >= minHits;
        };
    }

    /**
     * Predicate that checks if hits are predominantly in hot temperature stages (red, orange, yellow)
     */
    public static Predicate<MatchContext> hotWorkingPredicate() {
        return context -> {
            Integer redHits = context.get(MinecraftContextKeys.RED_STAGE_HITS).orElse(0);
            Integer orangeHits = context.get(MinecraftContextKeys.ORANGE_STAGE_HITS).orElse(0);
            Integer yellowHits = context.get(MinecraftContextKeys.YELLOW_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);

            int hotHits = redHits + orangeHits + yellowHits;
            return totalHits > 0 && (double) hotHits / totalHits > 0.6;
        };
    }

    /**
     * Predicate that checks if hits are predominantly in cold temperature stages (purple, straw, brown)
     */
    public static Predicate<MatchContext> coldWorkingPredicate() {
        return context -> {
            Integer purpleHits = context.get(MinecraftContextKeys.PURPLE_STAGE_HITS).orElse(0);
            Integer strawHits = context.get(MinecraftContextKeys.STRAW_STAGE_HITS).orElse(0);
            Integer brownHits = context.get(MinecraftContextKeys.BROWN_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);

            int coldHits = purpleHits + strawHits + brownHits;
            return totalHits > 0 && (double) coldHits / totalHits > 0.6;
        };
    }

    /**
     * Predicate that checks if work was done with perfect temperature control (majority in optimal ranges)
     */
    public static Predicate<MatchContext> perfectTemperatureControl() {
        return context -> {
            Integer redHits = context.get(MinecraftContextKeys.RED_STAGE_HITS).orElse(0);
            Integer orangeHits = context.get(MinecraftContextKeys.ORANGE_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);

            int optimalHits = redHits + orangeHits;
            return totalHits > 0 && (double) optimalHits / totalHits > 0.8;
        };
    }

    // =================================
    // Accuracy and Performance Predicates
    // =================================

    /**
     * Predicate that checks for perfect accuracy (100% hit rate)
     */
    public static Predicate<MatchContext> perfectAccuracy() {
        return context -> {
            Integer totalAttempts = context.get(MinecraftContextKeys.TOTAL_ATTEMPTS).orElse(0);
            Integer missHits = context.get(MinecraftContextKeys.MISS_HITS).orElse(0);
            return totalAttempts > 0 && missHits == 0;
        };
    }

    /**
     * Predicate that checks for excellent accuracy (>= 90% hit rate)
     */
    public static Predicate<MatchContext> excellentAccuracy() {
        return context -> {
            Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
            return accuracyRate >= 0.9;
        };
    }

    /**
     * Predicate that checks for good accuracy (>= 75% hit rate)
     */
    public static Predicate<MatchContext> goodAccuracy() {
        return context -> {
            Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
            return accuracyRate >= 0.75;
        };
    }

    /**
     * Predicate that checks for poor accuracy (< 50% hit rate)
     */
    public static Predicate<MatchContext> poorAccuracy() {
        return context -> {
            Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
            return accuracyRate < 0.5;
        };
    }

    /**
     * Predicate that checks for terrible accuracy (< 30% hit rate)
     */
    public static Predicate<MatchContext> terribleAccuracy() {
        return context -> {
            Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
            return accuracyRate < 0.3;
        };
    }

    /**
     * Predicate that checks for high miss count (>= 5 misses)
     */
    public static Predicate<MatchContext> highMissCount() {
        return context -> {
            Integer missHits = context.get(MinecraftContextKeys.MISS_HITS).orElse(0);
            return missHits >= 5;
        };
    }

    /**
     * Predicate that checks for excessive miss count (>= 8 misses)
     */
    public static Predicate<MatchContext> excessiveMissCount() {
        return context -> {
            Integer missHits = context.get(MinecraftContextKeys.MISS_HITS).orElse(0);
            return missHits >= 8;
        };
    }

    // =================================
    // Combined Temperature and Performance Predicates
    // =================================

    /**
     * Predicate for skilled hot working - high temperature work with good accuracy
     */
    public static Predicate<MatchContext> skilledHotWorking() {
        return context -> {
            return hotWorkingPredicate().test(context) && goodAccuracy().test(context);
        };
    }

    /**
     * Predicate for expert cold working - cold temperature work with excellent accuracy
     */
    public static Predicate<MatchContext> expertColdWorking() {
        return context -> {
            return coldWorkingPredicate().test(context) && excellentAccuracy().test(context);
        };
    }

    /**
     * Predicate for sloppy hot working - hot work but poor accuracy
     */
    public static Predicate<MatchContext> sloppyHotWorking() {
        return context -> {
            return hotWorkingPredicate().test(context) && poorAccuracy().test(context);
        };
    }

    /**
     * Predicate for inconsistent working - work done at wrong temperatures
     */
    public static Predicate<MatchContext> inconsistentWorking() {
        return context -> {
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            Integer strawHits = context.get(MinecraftContextKeys.STRAW_STAGE_HITS).orElse(0);
            Integer brownHits = context.get(MinecraftContextKeys.BROWN_STAGE_HITS).orElse(0);

            if (totalHits == 0) return false;

            int veryLowTempHits = strawHits + brownHits;
            double lowTempRatio = (double) veryLowTempHits / totalHits;

            // Inconsistent if significant work done at very low temperatures
            return lowTempRatio > 0.4;
        };
    }

    /**
     * Predicate for rushed work - poor temperature control and poor accuracy
     */
    public static Predicate<MatchContext> rushedWork() {
        return context -> {
            return inconsistentWorking().test(context) && poorAccuracy().test(context);
        };
    }
}
