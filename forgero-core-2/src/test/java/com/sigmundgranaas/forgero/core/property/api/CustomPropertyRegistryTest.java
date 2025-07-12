package com.sigmundgranaas.forgero.core.property.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.StaticConditions;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.ConditionData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.TagMatchPredicateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import com.sigmundgranaas.forgero.data.mapper.api.ComponentMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatIdentifierData;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatIdentifierEngine;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatModuleInitializer;
import com.sigmundgranaas.forgero.property.bettercombat.DefaultBetterCombatKeys;
import com.sigmundgranaas.forgero.property.namereplacement.DefaultNameReplacementKeys;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementData;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementEngine;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementModuleInitializer;
import com.sigmundgranaas.forgero.property.tooltip.DefaultTooltipKeys;
import com.sigmundgranaas.forgero.property.tooltip.TooltipData;
import com.sigmundgranaas.forgero.property.tooltip.TooltipEngine;
import com.sigmundgranaas.forgero.property.tooltip.TooltipModuleInitializer;
import com.sigmundgranaas.forgero.property.tooltip.TooltipProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.data.Utils.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomPropertyRegistryTest {

	private ComponentMapper componentMapper;
	private ResolverEngine resolver;
	private IdentifierFactory idFactory;

	@BeforeEach
	void setUp() {
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
		PropertyRegistry.getInstance().reset(); // Resets and initializes core codecs

		// Initialize custom property modules. These also register their codecs.
		BetterCombatModuleInitializer.initialize();
		TooltipModuleInitializer.initialize();
		NameReplacementModuleInitializer.initialize();

		componentMapper = new ComponentMapper(idFactory);
		resolver = new ResolverEngine();
	}

	private Component createComponentWithProperties(String name, Map<String, List<PropertyData>> properties) {
		NormalizedState.NormalizedStaticPart staticPartData = new NormalizedState.NormalizedStaticPart(
				id("forgero:" + name),
				name,
				Set.of(id("forgero:test_component")),
				new HostData(List.of(new IdentifierEntry("id", id("forgero:" + name))), null),
				List.of(),
				properties
		);
		return componentMapper.map(staticPartData);
	}

	private JsonObject parseJson(String json) {
		return JsonParser.parseString(json).getAsJsonObject ();
	}

	@Test
	@DisplayName("PropertyRegistry contains expected codecs and engines after module initialization")
	void testRegistryPopulation() {
		// Assert that the core codecs/engines are present (added by DefaultPropertyRegistry)
		assertTrue(PropertyRegistry.getInstance().getPropertyCodecs().stream()
				.anyMatch(b -> b.getPropertyType().equals("forgero:attributes")), "Registry should contain AttributeCodec");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals("attributes")), "Registry should contain AttributeEngine");

		// Assert that the custom codecs/engines are present (added by module initializers)
		assertTrue(PropertyRegistry.getInstance().getPropertyCodecs().stream()
				.anyMatch(b -> b.getPropertyType().equals(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString())), "Registry should contain TooltipCodec");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals(DefaultTooltipKeys.TOOLTIPS.id().path())), "Registry should contain TooltipEngine");

		assertTrue(PropertyRegistry.getInstance().getPropertyCodecs().stream()
				.anyMatch(b -> b.getPropertyType().equals(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString())), "Registry should contain NameReplacementCodec");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals(DefaultNameReplacementKeys.NAME_REPLACEMENT.id().path())), "Registry should contain NameReplacementEngine");

		assertTrue(PropertyRegistry.getInstance().getPropertyCodecs().stream()
				.anyMatch(b -> b.getPropertyType().equals(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString())), "Registry should contain BetterCombatIdentifierCodec");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals(BetterCombatIdentifierEngine.KEY.id().path())), "Registry should contain BetterCombatIdentifierEngine");
	}

	@Test
	@DisplayName("Tooltip property resolves correctly via registry, respecting conditions")
	void testTooltipResolutionViaRegistry() {
		String propertiesJson = """
                {
                  "forgero:tooltip_sections": [
                    {
                      "key": "forgero:ingredient_count",
                      "value": "3",
                      "format": "NUMERIC",
                      "condition": { "type": "forgero:root_has_tag", "tag": "forgero:test_component" }
                    },
                    {
                      "key": "forgero:always_show_tooltip",
                      "value": "Always Active",
                      "condition": { "type": "forgero:self_has_tag", "tag": "forgero:test_component" }
                    }
                  ]
                }
                """;
		var json = parseJson(propertiesJson);
		var tooltipJson = json.get(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString());

		// Parse the JSON into List<TooltipData> and then wrap it into List<PropertyData>
		List<TooltipData> parsedTooltipDataList = TooltipData.CODEC.listOf().parse(JsonOps.INSTANCE, tooltipJson).result().orElseThrow();
		Map<String, List<PropertyData>> propsMap = new HashMap<>();
		propsMap.put(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString(), new ArrayList<>(parsedTooltipDataList));

		Component testComponent = createComponentWithProperties("test_item_root", propsMap);
		Component childComponent = createComponentWithProperties("test_item_child", new HashMap<>()); // No properties

		// Scenario 1: test_item_root is resolved. Both conditions should pass.
		List<TooltipProperty> resolvedTooltipsRoot = resolver.resolve(testComponent, TooltipEngine.KEY)
				.orElse(List.of());

		assertEquals(2, resolvedTooltipsRoot.size(), "Root component should resolve both tooltips.");
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:ingredient_count")) && t.value().equals("3")));
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:always_show_tooltip")) && t.value().equals("Always Active")));

		// Scenario 2: childComponent is resolved. It has no properties, so it should resolve to an empty list.
		List<TooltipProperty> resolvedTooltipsChild = resolver.resolve(childComponent, TooltipEngine.KEY)
				.orElse(List.of());

		assertTrue(resolvedTooltipsChild.isEmpty(), "Child component with no properties should resolve to an empty tooltip list.");
	}

	@Test
	@DisplayName("Name replacement property resolves correctly via registry")
	void testNameReplacementResolutionViaRegistry() {
		String propertiesJson = """
                {
                  "forgero:name_replacement": {
                    "from": "sword",
                    "to": "broadsword"
                  }
                }
                """;
		var json = parseJson(propertiesJson);
		var nameReplacementJson = json.get(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString());

		// Parse the JSON into NameReplacementData and then wrap it into List<PropertyData>
		NameReplacementData parsedNameReplacementData = NameReplacementData.CODEC.parse(JsonOps.INSTANCE, nameReplacementJson).result().orElseThrow();
		Map<String, List<PropertyData>> propsMap = new HashMap<>();
		propsMap.put(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString(), List.of(parsedNameReplacementData));

		Component testComponent = createComponentWithProperties("test_sword", propsMap);

		Optional<String> resolvedName = resolver.resolve(testComponent, NameReplacementEngine.KEY)
				.orElse(Optional.empty());

		assertTrue(resolvedName.isPresent(), "Name replacement should resolve");
		assertEquals("broadsword", resolvedName.get());
	}

	@Test
	@DisplayName("Better Combat identifier property resolves correctly via registry")
	void testBetterCombatIdentifierResolutionViaRegistry() {
		String propertiesJson = """
                {
                  "better_combat:attribute_container": {
                    "value": "forgero:claymore"
                  }
                }
                """;
		var json = parseJson(propertiesJson);
		var betterCombatJson = json.get(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString());

		// Parse the JSON into BetterCombatIdentifierData and then wrap it into List<PropertyData>
		BetterCombatIdentifierData parsedBetterCombatData = BetterCombatIdentifierData.CODEC.parse(JsonOps.INSTANCE, betterCombatJson).result().orElseThrow();
		Map<String, List<PropertyData>> propsMap = new HashMap<>();
		propsMap.put(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString(), List.of(parsedBetterCombatData));

		Component testComponent = createComponentWithProperties("test_claymore", propsMap);

		Optional<OpenIdentifier> resolvedIdentifier = resolver.resolve(testComponent, BetterCombatIdentifierEngine.KEY)
				.orElse(Optional.empty());

		assertTrue(resolvedIdentifier.isPresent(), "Better Combat identifier should resolve");
		assertEquals(id("forgero:claymore"), resolvedIdentifier.get());
	}

	@Test
	@DisplayName("TooltipProperty can be round-tripped through Codec and PropertyData")
	void testTooltipPropertyRoundTripSerialization() {
		// 1. Programmatically create an original TooltipProperty
		OpenIdentifier originalKey = idFactory.of("forgero:test_tooltip");
		String originalValue = "Test Value";
		String originalFormat = "RAW";
		Condition originalCondition = new Condition(List.of(StaticConditions.selfHasTag("test_tag")), Collections.emptyList(),
				new ConditionData(List.of(new TagMatchPredicateData(idFactory.of("forgero:self_has_tag"), idFactory.of("forgero:test_tag"))))
		);
		TooltipProperty originalProperty = new TooltipProperty(originalKey, originalValue, originalFormat, originalCondition);

		// 2. Get the TooltipCodec from the registry
		@SuppressWarnings("unchecked")
		PropertyCodec<TooltipData> tooltipCodec = (PropertyCodec<TooltipData>) PropertyRegistry.getInstance().getPropertyCodecs().stream()
				.filter(codec -> codec.getPropertyType().equals(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("TooltipCodec not found in registry."));

		// 3. Convert Property to PropertyData (forward mapping)
		TooltipData originalData = tooltipCodec.toData(originalProperty);
		assertNotNull(originalData, "Property should convert to PropertyData");
		assertEquals(originalKey, originalData.key());
		assertEquals(originalValue, originalData.value());
		assertEquals(originalFormat, originalData.format());
		assertNotNull(originalData.condition(), "Condition data should be present");
		// Further assert condition data details if needed

		// 4. Serialize PropertyData to JsonElement (will be a JsonArray as it's Codec<List<T>>)
		Codec<List<TooltipData>> codec = tooltipCodec.getCodec();
		var serializedElement = codec.encodeStart(JsonOps.INSTANCE, List.of(originalData))
				.resultOrPartial(error -> { throw new RuntimeException("Serialization error: " + error); })
				.orElseThrow();

		assertNotNull(serializedElement, "Serialized JSON should not be null");
		assertTrue(serializedElement.isJsonArray(), "Serialized element should be a JSON array");
		assertEquals(1, serializedElement.getAsJsonArray().size());
		JsonObject jsonObject = serializedElement.getAsJsonArray().get(0).getAsJsonObject(); // Get the single object

		// Check some basic fields in the JSON
		assertEquals(originalKey.toString(), jsonObject.get("key").getAsString());
		assertEquals(originalValue, jsonObject.get("value").getAsString());
		assertEquals(originalFormat, jsonObject.get("format").getAsString());
		assertTrue(jsonObject.has("condition"), "Condition should be serialized");


		// 5. Deserialize JsonElement back to PropertyData (list of data)
		List<TooltipData> deserializedDataList = codec.parse(JsonOps.INSTANCE, serializedElement)
				.resultOrPartial(error -> { throw new RuntimeException("Deserialization error: " + error); })
				.orElseThrow();
		assertEquals(1, deserializedDataList.size(), "Deserialized list should contain one item");
		TooltipData deserializedData = deserializedDataList.get(0);

		assertNotNull(deserializedData, "Deserialized data should not be null");
		assertEquals(originalData.key(), deserializedData.key());
		assertEquals(originalData.value(), deserializedData.value());
		assertEquals(originalData.format(), deserializedData.format());
		assertNotNull(deserializedData.condition(), "Deserialized condition data should be present");
		// Assert condition data equality (deep comparison might be needed for complex conditions)
		assertEquals(originalData.condition().predicates().get(0).type(), deserializedData.condition().predicates().get(0).type());


		// 6. Convert PropertyData back to Property (backward mapping)
		List<Property> roundTrippedProperties = tooltipCodec.build(deserializedDataList);
		assertEquals(1, roundTrippedProperties.size(), "Round-tripped properties list should contain one item");
		Property roundTrippedProperty = roundTrippedProperties.get(0);

		assertTrue(roundTrippedProperty instanceof TooltipProperty, "Round-tripped property should be TooltipProperty");
		TooltipProperty roundTrippedTooltip = (TooltipProperty) roundTrippedProperty;

		// 7. Assert that original and round-tripped properties are equivalent
		assertEquals(originalProperty.key(), roundTrippedTooltip.key(), "Keys should match after round trip");
		assertEquals(originalProperty.value(), roundTrippedTooltip.value(), "Values should match after round trip");
		assertEquals(originalProperty.format(), roundTrippedTooltip.format(), "Formats should match after round trip");
		// For conditions, direct equality might not work due to object identity,
		// but we can check if their underlying logic or source data matches.
		// Since Condition.sourceData is now available, we can compare that.
		assertNotNull(originalProperty.condition(), "Original property should have a condition");
		assertNotNull(roundTrippedTooltip.condition(), "Round-tripped property should have a condition");
		assertEquals(originalProperty.condition().sourceData(), roundTrippedTooltip.condition().sourceData(), "Condition source data should match after round trip");
	}
}
