package com.sigmundgranaas.forgero.data.pipeline.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies symmetric identity-merge: two independent definitions sharing an id are composed (no base
 * pack required), with id-keyed attribute override, host union, type-clash detection, and priority
 * ordering.
 */
class DefinitionMergerTest {

	private DefinitionMerger merger;
	private IdentifierFactory idFactory;

	@BeforeEach
	void setUp() {
		merger = new DefinitionMerger();
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
	}

	private OpenIdentifier id(String id) {
		return idFactory.of(id);
	}

	private AttributeData attr(String attrId, String type, float value) {
		return new AttributeDataImpl(
				Optional.of(id(attrId)), id(type), new ComputationData(value, null, null),
				Optional.empty(), Optional.empty());
	}

	private ResourceData material(String name, List<OpenIdentifier> tags, List<AttributeData> attrs, HostData host) {
		return new ResourceData(id("forgero:material"), name, null, tags, null, host, attrs, null, null);
	}

	private HostData host(String itemId) {
		return new HostData(List.of(new IdentifierEntry("item", id(itemId))), null);
	}

	private RawDefinition raw(String id, ResourceData data, int priority) {
		return new RawDefinition(id(id), data, priority);
	}

	@Test
	void singleDefinitionIsReturnedUnchanged() {
		RawDefinition only = raw("forgero:iron", material("iron", List.of(id("forgero:metal")), null, null), 0);
		RawDefinition result = merger.mergeGroup(id("forgero:iron"), List.of(only));
		assertSame(only, result);
	}

	@Test
	void mergesTagsAttributesAndHostFromTwoPacks() {
		// Pack A: the material's stats + host.
		ResourceData base = material("iron",
				List.of(id("forgero:metal")),
				List.of(attr("forgero:iron-durability", "forgero:durability", 250f)),
				host("minecraft:iron_ingot"));
		// Pack B: the "binding role" content + an extra host claim, fully standalone.
		ResourceData binding = material("iron",
				List.of(id("forgero:materials/roles/upgrade_material")),
				List.of(attr("forgero:iron-binding-durability", "forgero:durability", 100f)),
				host("minecraft:leather"));

		ResourceData merged = (ResourceData) merger.mergeGroup(id("forgero:iron"),
				List.of(raw("forgero:iron", base, 0), raw("forgero:iron", binding, 0))).data();

		assertTrue(merged.tags().contains(id("forgero:metal")), "keeps base tag");
		assertTrue(merged.tags().contains(id("forgero:materials/roles/upgrade_material")), "unions binding role tag");
		assertEquals(2, merged.attributes().size(), "appends distinct attributes");
		// Host identifiers unioned across packs.
		assertTrue(merged.host().identifiers().stream().anyMatch(e -> e.id().equals(id("minecraft:iron_ingot"))));
		assertTrue(merged.host().identifiers().stream().anyMatch(e -> e.id().equals(id("minecraft:leather"))));
	}

	@Test
	void matchingAttributeIdIsOverriddenByHigherPriority() {
		ResourceData low = material("iron", null,
				List.of(attr("forgero:iron-durability", "forgero:durability", 100f)), null);
		ResourceData high = material("iron", null,
				List.of(attr("forgero:iron-durability", "forgero:durability", 999f)), null);

		// Provide in the "wrong" order to prove priority (not input order) decides the winner.
		ResourceData merged = (ResourceData) merger.mergeGroup(id("forgero:iron"),
				List.of(raw("forgero:iron", high, 10), raw("forgero:iron", low, 0))).data();

		assertEquals(1, merged.attributes().size(), "same attribute id overrides, not appends");
		assertEquals(999f, merged.attributes().get(0).computation().value(), 0.001f,
				"higher-priority definition wins the override");
	}

	@Test
	void listValuedPropertiesConcatenateAndObjectsDeepMerge() {
		com.google.gson.JsonArray fire = new com.google.gson.JsonArray();
		fire.add(new com.google.gson.JsonPrimitive("fire"));
		com.google.gson.JsonArray lightning = new com.google.gson.JsonArray();
		lightning.add(new com.google.gson.JsonPrimitive("lightning"));

		com.google.gson.JsonObject objA = new com.google.gson.JsonObject();
		objA.addProperty("a", 1);
		com.google.gson.JsonObject objB = new com.google.gson.JsonObject();
		objB.addProperty("b", 2);

		ResourceData a = new ResourceData(id("forgero:material"), "iron", null, null, null, null, null, null,
				java.util.Map.of("minecraft:on_hit", fire, "forgero:cfg", objA));
		ResourceData b = new ResourceData(id("forgero:material"), "iron", null, null, null, null, null, null,
				java.util.Map.of("minecraft:on_hit", lightning, "forgero:cfg", objB));

		ResourceData merged = (ResourceData) merger.mergeGroup(id("forgero:iron"),
				List.of(raw("forgero:iron", a, 0), raw("forgero:iron", b, 0))).data();

		// List-valued property (e.g. on_hit) concatenates across packs.
		assertEquals(2, merged.properties().get("minecraft:on_hit").getAsJsonArray().size(),
				"on_hit effects from both packs should be combined");
		// Object-valued property deep-merges.
		com.google.gson.JsonObject cfg = merged.properties().get("forgero:cfg").getAsJsonObject();
		assertTrue(cfg.has("a") && cfg.has("b"), "object properties merge key-wise across packs");
	}

	@Test
	void conflictingTypesForSameIdFailFast() {
		ResourceData asMaterial = material("iron", null, null, null);
		ResourceData asPart = new ResourceData(id("forgero:static_part"), "iron", null, null, null, null, null, null, null);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
				merger.mergeGroup(id("forgero:iron"),
						List.of(raw("forgero:iron", asMaterial, 0), raw("forgero:iron", asPart, 0))));
		assertTrue(ex.getMessage().contains("conflicting types"), "clear clash message");
	}
}
