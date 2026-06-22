package com.sigmundgranaas.forgero.smithing.condition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;
import com.sigmundgranaas.forgero.smithing.condition.custom.TemperaturePredicates;

public class PredicateConditionLootRegistry {
	private static final Map<Predicate<MatchContext>, NamedCondition> PREDICATE_CONDITION_MAP = new LinkedHashMap<>();

	static {
		registerTemperaturePredicates();
	}

	private static void registerTemperaturePredicates() {
		Predicate<MatchContext> cleanFinalQuench = TemperaturePredicates.finalQuenchCompletedInOneGo()
				.and(TemperaturePredicates.finalQuenchSessionsAtMost(1))
				.and(TemperaturePredicates.finalQuenchStartedHotOrWorkable());
		Predicate<MatchContext> cleanWork = TemperaturePredicates.noMisses()
				.and(TemperaturePredicates.noPoorStrikes())
				.and(TemperaturePredicates.overheatedHitsAtMost(0));
		Predicate<MatchContext> preciseTiming = TemperaturePredicates.perfectStrikeFractionAtLeast(0.4)
				.and(TemperaturePredicates.poorStrikeFractionAtMost(0.15))
				.and(TemperaturePredicates.skilledStrikeStreakAtLeast(3));
		Predicate<MatchContext> usefulCooling = TemperaturePredicates.coolingMarkerHitsAtLeast(2)
				.and(
						TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.HOT, 1)
								.or(TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.WORKABLE, 1))
								.or(TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.OVERHEATED, 1))
				)
				.and(
						TemperaturePredicates.perfectCoolingStrikesAtLeast(1)
								.or(TemperaturePredicates.goodCoolingStrikesAtLeast(2))
				)
				.and(TemperaturePredicates.poorCoolingStrikesAtMost(1));
		Predicate<MatchContext> splitFinalQuench = TemperaturePredicates.finalQuenchSessionsAtLeast(2);
		Predicate<MatchContext> interruptedForgingQuench = TemperaturePredicates.inProgressQuenchSessionsAtLeast(2)
				.or(
						TemperaturePredicates.quenchedDuringSmithing()
								.and(TemperaturePredicates.reheatedAfterEveryQuench().negate())
				);
		Predicate<MatchContext> saltWaterForge = createAnyBiomePredicate(
				"minecraft:beach",
				"minecraft:river",
				"minecraft:ocean",
				"minecraft:deep_ocean",
				"minecraft:cold_ocean",
				"minecraft:deep_cold_ocean",
				"minecraft:frozen_ocean",
				"minecraft:deep_frozen_ocean",
				"minecraft:lukewarm_ocean",
				"minecraft:deep_lukewarm_ocean",
				"minecraft:warm_ocean"
		);
		Predicate<MatchContext> stagnantWaterForge = createAnyBiomePredicate("minecraft:swamp", "minecraft:mangrove_swamp");

		registerCondition("forgero:ruined", TemperaturePredicates.missHitsAtLeast(5));
		registerCondition("forgero:brittle",
				TemperaturePredicates.overheatedHitFractionAtLeast(0.25)
						.or(
								TemperaturePredicates.finalQuenchStartedInStage(TemperaturePredicates.OVERHEATED)
										.and(TemperaturePredicates.coolingMarkerHitsAtMost(1))
						)
		);
		registerCondition("forgero:cracked", splitFinalQuench);
		registerCondition("forgero:fragile",
				TemperaturePredicates.finalQuenchStartedInStage(TemperaturePredicates.OVERHEATED)
						.and(cleanFinalQuench)
						.and(usefulCooling.negate())
		);
		registerCondition("forgero:weak",
				TemperaturePredicates.coldHitFractionAtLeast(0.25)
						.or(TemperaturePredicates.finalQuenchStartedInStage(TemperaturePredicates.COLD))
		);
		registerCondition("forgero:dull",
				TemperaturePredicates.warmHitFractionAtLeast(0.35)
						.or(TemperaturePredicates.finalQuenchStartedInStage(TemperaturePredicates.WARM))
		);
		registerCondition("forgero:chipped", TemperaturePredicates.missHitsAtLeast(3));
		registerCondition("forgero:flawed",
				interruptedForgingQuench
						.or(TemperaturePredicates.poorStrikeFractionAtLeast(0.35))
		);
		registerCondition("forgero:unstable",
				TemperaturePredicates.hasTransition(TemperaturePredicates.HOT, TemperaturePredicates.OVERHEATED, 2)
						.or(TemperaturePredicates.hasTransition(TemperaturePredicates.OVERHEATED, TemperaturePredicates.WORKABLE, 2))
		);
		registerCondition("forgero:tainted",
				createDimensionPredicate("minecraft:the_nether")
						.and(
								TemperaturePredicates.overheatedHitFractionAtLeast(0.15)
										.or(TemperaturePredicates.poorStrikeFractionAtLeast(0.25))
						)
		);
		registerCondition("forgero:cursed",
				createDimensionPredicate("minecraft:the_end")
						.and(
								TemperaturePredicates.missHitsAtLeast(1)
										.or(TemperaturePredicates.poorStrikeFractionAtLeast(0.25))
										.or(TemperaturePredicates.finalQuenchSessionsAtLeast(2))
						)
		);
		registerCondition("forgero:rusted",
				stagnantWaterForge
						.and(
								TemperaturePredicates.warmHitFractionAtLeast(0.2)
										.or(TemperaturePredicates.inProgressQuenchSessionsAtLeast(1))
						)
		);
		registerCondition("forgero:corroded",
				saltWaterForge
						.and(
								TemperaturePredicates.inProgressQuenchSessionsAtLeast(1)
										.or(TemperaturePredicates.finalQuenchSessionsAtLeast(2))
						)
		);
		registerCondition("forgero:sluggish",
				TemperaturePredicates.poorStrikeFractionAtLeast(0.25)
						.and(TemperaturePredicates.hotHitFractionAtLeast(0.35).negate())
		);
		registerCondition("forgero:blunt",
				TemperaturePredicates.hotHitFractionAtLeast(0.35)
						.and(TemperaturePredicates.perfectStrikeFractionAtLeast(0.2).negate())
		);
		registerCondition("forgero:worn",
				TemperaturePredicates.totalQuenchSessionsAtLeast(3)
						.or(TemperaturePredicates.warmHitFractionAtLeast(0.25))
		);

		registerCondition("forgero:unbreakable",
				cleanWork
						.and(cleanFinalQuench)
						.and(TemperaturePredicates.workableHitFractionAtLeast(0.85))
						.and(TemperaturePredicates.perfectStrikeFractionAtLeast(0.6))
						.and(TemperaturePredicates.perfectStrikeStreakAtLeast(5))
		);
		registerCondition("forgero:voidtouched",
				createDimensionPredicate("minecraft:the_end")
						.and(cleanWork)
						.and(preciseTiming)
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:netherborn",
				createDimensionPredicate("minecraft:the_nether")
						.and(TemperaturePredicates.hotHitFractionAtLeast(0.45))
						.and(preciseTiming)
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:rare",
				cleanWork
						.and(cleanFinalQuench)
						.and(TemperaturePredicates.workableHitFractionAtLeast(0.75))
						.and(TemperaturePredicates.perfectStrikeFractionAtLeast(0.5))
		);
		registerCondition("forgero:lucky",
				TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.OVERHEATED, 1)
						.and(TemperaturePredicates.perfectCoolingStrikesAtLeast(1))
						.and(TemperaturePredicates.missHitsAtMost(1))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:tempered",
				TemperaturePredicates.cleanWorkableRun()
						.and(TemperaturePredicates.workableHitFractionAtLeast(0.75))
						.and(preciseTiming)
						.and(cleanFinalQuench)
						.and(TemperaturePredicates.noInProgressQuenching())
						.and(TemperaturePredicates.noMisses())
						.and(TemperaturePredicates.noPoorStrikes())
		);
		registerCondition("forgero:hardened",
				usefulCooling
						.and(TemperaturePredicates.workableHitFractionAtLeast(0.5))
						.and(TemperaturePredicates.missHitsAtMost(1))
						.and(TemperaturePredicates.overheatedHitsAtMost(0))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:sturdy",
				TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.WORKABLE, 1)
						.and(TemperaturePredicates.workableHitFractionAtLeast(0.6))
						.and(TemperaturePredicates.missHitsAtMost(1))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:reinforced",
				TemperaturePredicates.noMisses()
						.and(TemperaturePredicates.workableHitFractionAtLeast(0.55))
						.and(TemperaturePredicates.skilledStrikeStreakAtLeast(3))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:resilient",
				TemperaturePredicates.totalQuenchSessionsAtLeast(2)
						.and(TemperaturePredicates.inProgressQuenchSessionsAtLeast(1))
						.and(TemperaturePredicates.reheatedAfterEveryQuench())
						.and(cleanFinalQuench)
						.and(TemperaturePredicates.poorStrikeFractionAtMost(0.2))
		);
		registerCondition("forgero:guarded",
				TemperaturePredicates.workableHitFractionAtLeast(0.6)
						.and(TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.WORKABLE, 1))
						.and(TemperaturePredicates.missHitsAtMost(1))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:honed",
				TemperaturePredicates.workableHitFractionAtLeast(0.5)
						.and(preciseTiming)
						.and(TemperaturePredicates.missHitsAtMost(1))
						.and(TemperaturePredicates.perfectStrikeStreakAtLeast(2))
		);
		registerCondition("forgero:sharp",
				TemperaturePredicates.hotHitFractionAtLeast(0.45)
						.and(
								TemperaturePredicates.goodStrikeFractionAtLeast(0.45)
										.or(TemperaturePredicates.perfectStrikeFractionAtLeast(0.25))
						)
		);
		registerCondition("forgero:mighty",
				TemperaturePredicates.hotHitFractionAtLeast(0.5)
						.and(TemperaturePredicates.skilledStrikeStreakAtLeast(3))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:trimmed",
				cleanWork
						.and(TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.WORKABLE, 2))
						.and(TemperaturePredicates.perfectCoolingStrikesAtLeast(1))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:lightweight",
				TemperaturePredicates.coolingMarkerHitsInStageAtLeast(TemperaturePredicates.WORKABLE, 1)
						.and(TemperaturePredicates.overheatedHitsAtMost(0))
						.and(TemperaturePredicates.poorStrikeFractionAtMost(0.15))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:nimble",
				cleanWork
						.and(TemperaturePredicates.skilledStrikeStreakAtLeast(4))
						.and(TemperaturePredicates.workableHitFractionAtLeast(0.55))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:swift",
				TemperaturePredicates.skilledStrikeStreakAtLeast(5)
						.and(TemperaturePredicates.noPoorStrikes())
						.and(TemperaturePredicates.hotHitFractionAtLeast(0.3))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:rapid",
				TemperaturePredicates.skilledStrikeStreakAtLeast(4)
						.and(TemperaturePredicates.goodStrikeFractionAtLeast(0.5))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:quick",
				TemperaturePredicates.skilledStrikeStreakAtLeast(3)
						.and(TemperaturePredicates.missHitsAtMost(1))
						.and(cleanFinalQuench)
		);
		registerCondition("forgero:engraved",
				TemperaturePredicates.noMisses()
						.and(TemperaturePredicates.noPoorStrikes())
						.and(TemperaturePredicates.perfectStrikeStreakAtLeast(3))
						.and(cleanFinalQuench)
		);
	}

	private static NamedCondition condition(String id) {
		return Conditions.INSTANCE.of(id).orElse(null);
	}

	public static NamedCondition getCondition(MatchContext context) {
		List<NamedCondition> conditions = getConditions(context);
		return conditions.isEmpty() ? null : conditions.get(0);
	}

	public static List<NamedCondition> getConditions(MatchContext context) {
		List<NamedCondition> conditions = new ArrayList<>();

		for (Predicate<MatchContext> predicate : PREDICATE_CONDITION_MAP.keySet()) {
			if (predicate.test(context)) {
				conditions.add(PREDICATE_CONDITION_MAP.get(predicate));
			}
		}

		return conditions;
	}

	public static void registerCondition(Predicate<MatchContext> predicate, NamedCondition condition) {
		if (condition != null) {
			PREDICATE_CONDITION_MAP.put(predicate, condition);
		}
	}

	private static void registerCondition(String id, Predicate<MatchContext> predicate) {
		registerCondition(predicate, condition(id));
	}

	public static Predicate<MatchContext> createDimensionPredicate(String dimensionId) {
		return context ->
				context.get(MinecraftContextKeys.WORLD)
						.map(world -> world.getRegistryKey().getValue().toString().equals(dimensionId))
						.orElse(false)
						|| context.get(MinecraftContextKeys.ENTITY)
						.map(entity -> entity.getWorld().getRegistryKey().getValue().toString().equals(dimensionId))
						.orElse(false);
	}

	public static Predicate<MatchContext> createEntityTypePredicate(String entityTypeId) {
		return context -> context.get(MinecraftContextKeys.ENTITY)
				.map(entity -> entity.getType().toString().equals(entityTypeId))
				.orElse(false);
	}

	public static Predicate<MatchContext> createAnyBiomePredicate(String... biomeIds) {
		Predicate<MatchContext> predicate = context -> false;

		for (String biomeId : biomeIds) {
			predicate = predicate.or(createBiomePredicate(biomeId));
		}

		return predicate;
	}

	public static Predicate<MatchContext> createBiomePredicate(String biomeId) {
		return context -> {
			boolean blockBiome = context.get(MinecraftContextKeys.WORLD)
					.flatMap(world -> context.get(MinecraftContextKeys.BLOCK_TARGET)
							.map(pos -> world.getBiome(pos).getKey()
									.map(key -> key.getValue().toString().equals(biomeId))
									.orElse(false)))
					.orElse(false);

			if (blockBiome) {
				return true;
			}

			return context.get(MinecraftContextKeys.ENTITY)
					.map(entity -> {
						var biome = entity.getWorld().getBiome(entity.getBlockPos());
						return biome.getKey().map(key -> key.getValue().toString().equals(biomeId)).orElse(false);
					})
					.orElse(false);
		};
	}
}
