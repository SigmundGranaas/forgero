package com.sigmundgranaas.forgero.core.property.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.core.feature.api.Feature;
import com.sigmundgranaas.forgero.core.feature.impl.FeatureEngine;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticConditions;
import com.sigmundgranaas.forgero.core.property.context.ContextKeys;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class FeatureResolverTest extends ForgeroTest {
	private Resolver resolver;

	@BeforeEach
	void setUp() {
		List<DataTypeEngine<?, ?>> engines = List.of(new FeatureEngine());
		resolver = new ResolverEngine(engines);
	}

	@Test
	void resolvesAndAggregatesFeatures() {
		var part1 = part(idFactory.of("part1"), METAL_TAG, List.of(new Feature(idFactory.of("fire_aspect"))));
		var part2 = part(idFactory.of("part2"), WOOD_TAG, List.of(new Feature(idFactory.of("splintering"))));
		var structure = new ComponentStructure(List.of(slot(idFactory.of("slot1"), idFactory.of("p1_type"), part1), slot(idFactory.of("slot2"), idFactory.of("p2_type"), part2)));
		var assembly = new StructuredPart(idFactory.of("assembly"), Set.of(), List.of(), structure);

		List<Feature> features = resolver.resolve(assembly, FeatureEngine.KEY).orElse(Collections.emptyList());

		assertEquals(2, features.size());
		assertTrue(features.stream().anyMatch(f -> f.type().path().equals("fire_aspect")));
		assertTrue(features.stream().anyMatch(f -> f.type().path().equals("splintering")));
	}

	@Test
	void resolvesAndAppliesStaticAndDynamicConditions() {
		// A static feature that is always active on a pickaxe
		var staticFeature = new Feature(idFactory.of("vein_miner"), new Condition(List.of(StaticConditions.rootHasTag("pickaxe")), Collections.emptyList()));

		// A dynamic feature that is only active against undead
		DynamicCondition undeadSlayerPredicate = (ctx) -> ctx.get(ContextKeys.TARGET_TAGS)
				.map(tags -> tags.contains(UNDEAD_TAG))
				.orElse(false);
		var dynamicFeature = new Feature(idFactory.of("smite"), new Condition(Collections.emptyList(), List.of(undeadSlayerPredicate)));

		var part1 = part(idFactory.of("part1"), METAL_TAG, List.of(staticFeature));
		var part2 = part(idFactory.of("part2"), GEM_TAG, List.of(dynamicFeature));
		var structure = new ComponentStructure(List.of(slot(idFactory.of("slot1"), idFactory.of("p1_type"), part1), slot(idFactory.of("slot2"), idFactory.of("p2_type"), part2)));
		var pickaxe = new StructuredPart(PICKAXE_ID, Set.of(idFactory.of("pickaxe")), List.of(), structure);

		DynamicContext undeadContext = new DynamicContext.Builder().put(ContextKeys.TARGET_TAGS, Set.of(UNDEAD_TAG)).build();
		DynamicContext humanContext = new DynamicContext.Builder().put(ContextKeys.TARGET_TAGS, Set.of(idFactory.of("human"))).build();

		// Against Undead: Vein Miner (static) and Smite (dynamic) should be active
		List<Feature> activeFeaturesUndead = resolver.resolve(pickaxe, FeatureEngine.KEY, undeadContext).orElseThrow();
		assertEquals(2, activeFeaturesUndead.size());
		assertTrue(activeFeaturesUndead.stream().anyMatch(f -> f.type().path().equals("vein_miner")));
		assertTrue(activeFeaturesUndead.stream().anyMatch(f -> f.type().path().equals("smite")));

		// Against Human: Only Vein Miner (static) should be active
		List<Feature> activeFeaturesHuman = resolver.resolve(pickaxe, FeatureEngine.KEY, humanContext).orElseThrow();
		assertEquals(1, activeFeaturesHuman.size());
		assertEquals("vein_miner", activeFeaturesHuman.get(0).type().path());
	}
}
