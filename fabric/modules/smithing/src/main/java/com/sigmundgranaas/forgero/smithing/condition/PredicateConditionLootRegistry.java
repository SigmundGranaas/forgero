package com.sigmundgranaas.forgero.smithing.condition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;

public class PredicateConditionLootRegistry {
	private static final Map<Predicate<MatchContext>, List<NamedCondition>> PREDICATE_LOOT_MAP = new LinkedHashMap<>();
	private static final Map<Predicate<MatchContext>, NamedCondition> PREDICATE_CONDITION_MAP = new LinkedHashMap<>();

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

		// Register temperature and performance-based predicates
		registerTemperaturePredicates();
	}

	private static void registerTemperaturePredicates() {
		// Register the new isinPerfectstage predicate
		registerCondition(TemperaturePredicates.isinPerfectstage(),
			Conditions.INSTANCE.of("forgero:voidtouched").orElse(null));


		register(TemperaturePredicates.MostHitsInForgingStage(), NEUTRAL);
		register(TemperaturePredicates.MostHitsInForgingStage(), NEUTRAL);
	}

	public static List<NamedCondition> getLootTable(MatchContext context) {
		for (Predicate<MatchContext> predicate : PREDICATE_LOOT_MAP.keySet()) {
			if (predicate.test(context)) {
				return PREDICATE_LOOT_MAP.get(predicate);
			}
		}
		return Collections.emptyList();
	}

	public static NamedCondition getCondition(MatchContext context) {
		for (Predicate<MatchContext> predicate : PREDICATE_CONDITION_MAP.keySet()) {
			if (predicate.test(context)) {
				return PREDICATE_CONDITION_MAP.get(predicate);
			}
		}
		return null;
	}


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
			boolean inDimension = context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD)
					.map(world -> world.getRegistryKey().getValue().toString().equals(dimensionId))
					.orElse(false) ||
				context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY)
					.map(entity -> entity.getWorld().getRegistryKey().getValue().toString().equals(dimensionId))
					.orElse(false);

			Integer forgingHits = context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.FORGING_STAGE_HITS).orElse(0);
			Integer totalHits = context.get(com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.TOTAL_HITS).orElse(0);
			boolean mostHitsInForgingStage = totalHits > 0 && forgingHits > totalHits / 2;

			return inDimension && mostHitsInForgingStage;
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
