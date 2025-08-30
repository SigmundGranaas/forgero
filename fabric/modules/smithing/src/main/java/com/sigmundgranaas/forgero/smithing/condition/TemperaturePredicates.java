package com.sigmundgranaas.forgero.smithing.condition;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;

/**
 * Temperature-based predicates for smithing conditions
 */
public class TemperaturePredicates {


	public static Predicate<MatchContext> veryHotStageHitPredicate() {
		return context -> context.get(MinecraftContextKeys.VERY_HOT_STAGE_HITS).orElse(0) >= 6;
	}
}
