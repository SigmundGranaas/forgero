package com.sigmundgranaas.forgero.smithing.condition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;

public class PredicateConditionLootRegistry {
	private static final Map<Predicate<MatchContext>, List<NamedCondition>> PREDICATE_LOOT_MAP = new HashMap<>();
	private static final Map<Predicate<MatchContext>, NamedCondition> PREDICATE_CONDITION_MAP = new HashMap<>();

	public static final List<NamedCondition> NEUTRAL = List.of(
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:engraved").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:reinforced").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:hardened").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:honed").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:lightweight").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:lucky").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:mighty").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:nimble").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:guarded").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:quick").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:rapid").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:rare").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:sharp").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:sturdy").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:swift").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:tempered").orElse(null),
			com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:resilient").orElse(null)
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:trimmed").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:unbreakable").orElse(null)
	).stream().filter(java.util.Objects::nonNull).toList();

	static {
		// Register dimension-based predicates
		registerCondition(createDimensionPredicate("minecraft:the_nether"), Conditions.INSTANCE.of("forgero:netherborn").orElse(null));
		registerCondition(createDimensionPredicate("minecraft:the_end"), Conditions.INSTANCE.of("forgero:voidtouched").orElse(null));

		// Register temperature-based predicates
		registerTemperaturePredicates();
	}

	private static void registerTemperaturePredicates() {
		// === TIER 1: PERFECT PERFORMANCE CONDITIONS ===
		// Perfect accuracy gets the best condition
		registerCondition(TemperaturePredicates.perfectAccuracy(), Conditions.INSTANCE.of("forgero:unbreakable").orElse(null));

		// === TIER 2: EXCELLENT PERFORMANCE CONDITIONS ===
		// Excellent accuracy with good temperature control
		registerCondition(TemperaturePredicates.excellentAccuracy(), Conditions.INSTANCE.of("forgero:rare").orElse(null));
		registerCondition(TemperaturePredicates.perfectTemperatureControl(), Conditions.INSTANCE.of("forgero:lucky").orElse(null));

		// === TIER 3: GOOD TEMPERATURE-SPECIFIC CONDITIONS ===
		// Each temperature stage gets a specific positive condition when majority hits
		registerCondition(TemperaturePredicates.majorityYellowStageHits(), Conditions.INSTANCE.of("forgero:sharp").orElse(null));
		registerCondition(TemperaturePredicates.majorityOrangeStageHits(), Conditions.INSTANCE.of("forgero:tempered").orElse(null));
		registerCondition(TemperaturePredicates.majorityRedStageHits(), Conditions.INSTANCE.of("forgero:hardened").orElse(null));
		registerCondition(TemperaturePredicates.majorityGreyStageHits(), Conditions.INSTANCE.of("forgero:sturdy").orElse(null));
		registerCondition(TemperaturePredicates.majorityBlueStageHits(), Conditions.INSTANCE.of("forgero:reinforced").orElse(null));
		registerCondition(TemperaturePredicates.majorityPurpleStageHits(), Conditions.INSTANCE.of("forgero:honed").orElse(null));
		registerCondition(TemperaturePredicates.majorityStrawStageHits(), Conditions.INSTANCE.of("forgero:lightweight").orElse(null));
		registerCondition(TemperaturePredicates.majorityBrownStageHits(), Conditions.INSTANCE.of("forgero:nimble").orElse(null));

		// === TIER 4: GOOD ACCURACY CONDITIONS ===
		registerCondition(TemperaturePredicates.goodAccuracy(), Conditions.INSTANCE.of("forgero:trimmed").orElse(null));
		registerCondition(TemperaturePredicates.skilledHotWorking(), Conditions.INSTANCE.of("forgero:mighty").orElse(null));
		registerCondition(TemperaturePredicates.expertColdWorking(), Conditions.INSTANCE.of("forgero:engraved").orElse(null));

		// === TIER 5: DECENT WORKING CONDITIONS ===
		registerCondition(TemperaturePredicates.hotWorkingPredicate(), Conditions.INSTANCE.of("forgero:quick").orElse(null));
		registerCondition(TemperaturePredicates.coldWorkingPredicate(), Conditions.INSTANCE.of("forgero:swift").orElse(null));
		registerCondition(TemperaturePredicates.minRedStageHits(6), Conditions.INSTANCE.of("forgero:rapid").orElse(null));
		registerCondition(TemperaturePredicates.minYellowStageHits(7), Conditions.INSTANCE.of("forgero:guarded").orElse(null));

		// === TIER 6: POOR ACCURACY CONDITIONS (NEGATIVE) ===
		registerCondition(TemperaturePredicates.terribleAccuracy(), Conditions.INSTANCE.of("forgero:cursed").orElse(null));
		registerCondition(TemperaturePredicates.excessiveMissCount(), Conditions.INSTANCE.of("forgero:brittle").orElse(null));
		registerCondition(TemperaturePredicates.poorAccuracy(), Conditions.INSTANCE.of("forgero:flawed").orElse(null));
		registerCondition(TemperaturePredicates.highMissCount(), Conditions.INSTANCE.of("forgero:cracked").orElse(null));

		// === TIER 7: POOR TECHNIQUE CONDITIONS (NEGATIVE) ===
		registerCondition(TemperaturePredicates.rushedWork(), Conditions.INSTANCE.of("forgero:unstable").orElse(null));
		registerCondition(TemperaturePredicates.sloppyHotWorking(), Conditions.INSTANCE.of("forgero:blunt").orElse(null));
		registerCondition(TemperaturePredicates.inconsistentWorking(), Conditions.INSTANCE.of("forgero:dull").orElse(null));

		// === TIER 8: SPECIFIC POOR PERFORMANCE CONDITIONS ===
		registerCondition(context -> {
			// Very high miss rate with poor temperature control
			Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
			Integer missHits = context.get(MinecraftContextKeys.MISS_HITS).orElse(0);
			return missHits >= 8 && accuracyRate < 0.3;
		}, Conditions.INSTANCE.of("forgero:fragile").orElse(null));

		registerCondition(context -> {
			// Too much work done at very low brown temperatures
			Integer brownHits = context.get(MinecraftContextKeys.BROWN_STAGE_HITS).orElse(0);
			Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
			return totalHits > 0 && (double) brownHits / totalHits > 0.7;
		}, Conditions.INSTANCE.of("forgero:sluggish").orElse(null));

		registerCondition(context -> {
			// Many misses with poor overall performance
			Integer missHits = context.get(MinecraftContextKeys.MISS_HITS).orElse(0);
			Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
			return missHits >= 6 && accuracyRate < 0.4;
		}, Conditions.INSTANCE.of("forgero:weak").orElse(null));

		registerCondition(context -> {
			// Poor accuracy with inconsistent work
			Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
			Integer strawHits = context.get(MinecraftContextKeys.STRAW_STAGE_HITS).orElse(0);
			Integer totalHits = context.get(MinecraftContextKeys.TOTAL_HITS).orElse(0);
			boolean poorAccuracy = accuracyRate < 0.5;
			boolean tooMuchStrawWork = totalHits > 0 && (double) strawHits / totalHits > 0.6;
			return poorAccuracy && tooMuchStrawWork;
		}, Conditions.INSTANCE.of("forgero:chipped").orElse(null));

		registerCondition(context -> {
			// Moderate miss count
			Integer missHits = context.get(MinecraftContextKeys.MISS_HITS).orElse(0);
			Double accuracyRate = context.get(MinecraftContextKeys.ACCURACY_RATE).orElse(0.0);
			return missHits >= 4 && missHits < 7 && accuracyRate < 0.6;
		}, Conditions.INSTANCE.of("forgero:worn").orElse(null));
	}

	/**
	 * Get loot table conditions based on the current context
	 */
	public static List<NamedCondition> getLootTable(MatchContext context) {
		for (Predicate<MatchContext> predicate : PREDICATE_LOOT_MAP.keySet()) {
			if (predicate.test(context)) {
				return PREDICATE_LOOT_MAP.get(predicate);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Get a specific condition based on the current context
	 */
	public static NamedCondition getCondition(MatchContext context) {
		for (Predicate<MatchContext> predicate : PREDICATE_CONDITION_MAP.keySet()) {
			if (predicate.test(context)) {
				return PREDICATE_CONDITION_MAP.get(predicate);
			}
		}
		return null;
	}

	/**
	 * Register a predicate with a loot table of conditions
	 */
	public static void register(Predicate<MatchContext> predicate, List<NamedCondition> lootTable) {
		PREDICATE_LOOT_MAP.put(predicate, lootTable);
	}

	/**
	 * Register a predicate with a single condition
	 */
	public static void registerCondition(Predicate<MatchContext> predicate, NamedCondition condition) {
		if (condition != null) {
			PREDICATE_CONDITION_MAP.put(predicate, condition);
		}
	}

	/**
	 * Helper method to create dimension-based predicates
	 */
	public static Predicate<MatchContext> createDimensionPredicate(String dimensionId) {
		return context -> {
			return context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD)
					.map(world -> world.getRegistryKey().getValue().toString().equals(dimensionId))
					.orElse(false) ||
			context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY)
					.map(entity -> entity.getWorld().getRegistryKey().getValue().toString().equals(dimensionId))
					.orElse(false);
		};
	}

	/**
	 * Helper method to create entity-type based predicates
	 */
	public static Predicate<MatchContext> createEntityTypePredicate(String entityTypeId) {
		return context -> {
			return context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY)
					.map(entity -> entity.getType().toString().equals(entityTypeId))
					.orElse(false);
		};
	}

	/**
	 * Helper method to create biome-based predicates
	 */
	public static Predicate<MatchContext> createBiomePredicate(String biomeId) {
		return context -> {
			return context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY)
					.map(entity -> {
						var biome = entity.getWorld().getBiome(entity.getBlockPos());
						return biome.getKey().map(key -> key.getValue().toString().equals(biomeId)).orElse(false);
					})
					.orElse(false);
		};
	}
}
