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
     * Predicate that checks if the majority of hits were in starting temperature stage
     */
    public static Predicate<MatchContext> majorityStartingStageHits() {
        return context -> {
            Integer startingHits = context.get(MinecraftContextKeys.STARTING_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && (double) startingHits / totalHits > 0.5;
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
     * Predicate that checks if at least X hits were in starting temperature stage
     */
    public static Predicate<MatchContext> minStartingStageHits(int minHits) {
        return context -> {
            Integer startingHits = context.get(MinecraftContextKeys.STARTING_STAGE_HITS).orElse(0);
            return startingHits >= minHits;
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
     * Predicate that checks if hits are predominantly in cold temperature stages (purple, starting)
     */
    public static Predicate<MatchContext> coldWorkingPredicate() {
        return context -> {
            Integer purpleHits = context.get(MinecraftContextKeys.PURPLE_STAGE_HITS).orElse(0);
            Integer startingHits = context.get(MinecraftContextKeys.STARTING_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);

            int coldHits = purpleHits + startingHits;
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
}
