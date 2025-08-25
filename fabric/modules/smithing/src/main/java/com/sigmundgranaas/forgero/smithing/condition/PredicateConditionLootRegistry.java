package com.sigmundgranaas.forgero.smithing.condition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;

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
		// Hot working predicates - for items forged with high heat
		registerCondition(TemperaturePredicates.hotWorkingPredicate(), Conditions.INSTANCE.of("forgero:tempered").orElse(null));
		registerCondition(TemperaturePredicates.majorityRedStageHits(), Conditions.INSTANCE.of("forgero:sharp").orElse(null));
		registerCondition(TemperaturePredicates.majorityOrangeStageHits(), Conditions.INSTANCE.of("forgero:hardened").orElse(null));
		registerCondition(TemperaturePredicates.majorityYellowStageHits(), Conditions.INSTANCE.of("forgero:honed").orElse(null));

		// Cold working predicates - for items worked at lower temperatures
		registerCondition(TemperaturePredicates.coldWorkingPredicate(), Conditions.INSTANCE.of("forgero:sturdy").orElse(null));
		registerCondition(TemperaturePredicates.majorityPurpleStageHits(), Conditions.INSTANCE.of("forgero:reinforced").orElse(null));
		registerCondition(TemperaturePredicates.majorityStartingStageHits(), Conditions.INSTANCE.of("forgero:lightweight").orElse(null));

		// Perfect temperature control predicate
		registerCondition(TemperaturePredicates.perfectTemperatureControl(), Conditions.INSTANCE.of("forgero:rare").orElse(null));

		// Minimum hit count predicates for specific effects
		registerCondition(TemperaturePredicates.minRedStageHits(7), Conditions.INSTANCE.of("forgero:mighty").orElse(null));
		registerCondition(TemperaturePredicates.minYellowStageHits(8), Conditions.INSTANCE.of("forgero:swift").orElse(null));
		registerCondition(TemperaturePredicates.minOrangeStageHits(6), Conditions.INSTANCE.of("forgero:quick").orElse(null));
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
