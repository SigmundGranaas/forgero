package com.sigmundgranaas.forgero.smithing.condition;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;

/**
 * Temperature-based predicates for smithing conditions
 */
public class TemperaturePredicates {

    public static Predicate<MatchContext> isinPerfectstage() {
        return context -> {
            Integer perfectHits = context.get(MinecraftContextKeys.PERFECT_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            Integer fastMarkerHits = context.get(MinecraftContextKeys.FAST_MARKER_HITS).orElse(0);
            Integer missHits = context.get(MinecraftContextKeys.MISS_HITS).orElse(0);

            boolean allHitsPerfectStage = totalHits > 0 && perfectHits.equals(totalHits);
            boolean allFastMarkersHit = fastMarkerHits >= 5;
            boolean noMisses = missHits == 0;

            return allHitsPerfectStage && allFastMarkersHit && noMisses;
        };
    }

    public static Predicate<MatchContext> MostHitsInForgingStage() {
        return context -> {
            Integer forgingHits = context.get(MinecraftContextKeys.FORGING_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && forgingHits > totalHits / 2;
        };
    }

    public static Predicate<MatchContext> MostHitsInOverheatedStage() {
        return context -> {
            Integer overheatedHits = context.get(MinecraftContextKeys.OVERHEATED_STAGE_HITS).orElse(0);
            Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
            return totalHits > 0 && overheatedHits > totalHits / 2;
        };
    }



}
