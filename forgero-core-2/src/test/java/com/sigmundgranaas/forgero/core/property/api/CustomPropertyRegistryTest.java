package com.sigmundgranaas.forgero.core.property.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.core.property.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodec;
import com.sigmundgranaas.forgero.data.mapper.api.ComponentMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatIdentifierCodec;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatIdentifierData;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatIdentifierEngine;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatModuleInitializer;
import com.sigmundgranaas.forgero.property.bettercombat.DefaultBetterCombatKeys;
import com.sigmundgranaas.forgero.property.namereplacement.DefaultNameReplacementKeys;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementCodec;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementData;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementEngine;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementModuleInitializer;
import com.sigmundgranaas.forgero.property.tooltip.DefaultTooltipKeys;
import com.sigmundgranaas.forgero.property.tooltip.TooltipCodec;
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
	private Codec<Condition> conditionCodec;
	private IdentifierFactory idFactory;

	@BeforeEach
	void setUp() {
		idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
		PropertyRegistry.getInstance().reset(); // Resets and initializes core codecs

		// Setup the master condition codec with some basic static predicates for testing
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		staticCodecs.put("forgero:self_has_tag", TagMatchCondition.CODEC);
		staticCodecs.put("forgero:root_has_tag", TagMatchCondition.CODEC);
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		this.conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		// Initialize custom property modules. These also register their codecs.
		BetterCombatModuleInitializer.initialize();
		TooltipModuleInitializer.initialize();
		NameReplacementModuleInitializer.initialize();

		// Manually initialize the codecs in the registry with the condition codec
		PropertyRegistry.getInstance().getPropertyCodecs().forEach(codec -> {
			if (codec instanceof BetterCombatIdentifierCodec c) c.initialize(conditionCodec);
			if (codec instanceof NameReplacementCodec c) c.initialize(conditionCodec);
			if (codec instanceof TooltipCodec c) c.initialize(conditionCodec);
		});


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

	private JsonElement parseJson(String json) {
		return JsonParser.parseString(json);
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
		JsonElement tooltipJson = parseJson(propertiesJson).getAsJsonObject().get(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString());

		List<TooltipData> parsedTooltipDataList = TooltipData.createCodec(conditionCodec).listOf().parse(JsonOps.INSTANCE, tooltipJson).result().orElseThrow();
		Map<String, List<PropertyData>> propsMap = new HashMap<>();
		propsMap.put(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString(), new ArrayList<>(parsedTooltipDataList));

		Component testComponent = createComponentWithProperties("test_item_root", propsMap);

		List<TooltipProperty> resolvedTooltipsRoot = resolver.resolve(testComponent, TooltipEngine.KEY)
				.orElse(List.of());

		assertEquals(2, resolvedTooltipsRoot.size(), "Root component should resolve both tooltips.");
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:ingredient_count")) && t.value().equals("3")));
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:always_show_tooltip")) && t.value().equals("Always Active")));
	}

	@Test
	@DisplayName("Name replacement property resolves correctly via registry")
	void testNameReplacementResolutionViaRegistry() {
		String propertiesJson = """
                {
                  "forgero:name_replacement": [
                    {
                      "from": "sword",
                      "to": "broadsword"
                    }
                  ]
                }
                """;
		JsonElement nameReplacementJson = parseJson(propertiesJson).getAsJsonObject().get(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString());

		List<NameReplacementData> parsedData = NameReplacementData.createCodec(conditionCodec).listOf().parse(JsonOps.INSTANCE, nameReplacementJson).result().orElseThrow();
		Map<String, List<PropertyData>> propsMap = new HashMap<>();
		propsMap.put(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString(), new ArrayList<>(parsedData));

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
                  "better_combat:attribute_container": [
                    {
                      "value": "forgero:claymore"
                    }
                  ]
                }
                """;
		JsonElement betterCombatJson = parseJson(propertiesJson).getAsJsonObject().get(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString());

		List<BetterCombatIdentifierData> parsedData = BetterCombatIdentifierData.createCodec(conditionCodec).listOf().parse(JsonOps.INSTANCE, betterCombatJson).result().orElseThrow();
		Map<String, List<PropertyData>> propsMap = new HashMap<>();
		propsMap.put(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString(), new ArrayList<>(parsedData));

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
		StaticCondition selfHasTag = new TagMatchCondition(idFactory.of("forgero:self_has_tag"), idFactory.of("forgero:test_tag"));
		Condition originalCondition = new Condition(List.of(selfHasTag), Collections.emptyList());
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
		// Get the single object and inspect its condition structure
		JsonElement conditionElement = serializedElement.getAsJsonArray().get(0).getAsJsonObject().get("condition");
		assertNotNull(conditionElement);
		assertTrue(conditionElement.isJsonArray(), "Condition should be serialized as an array");
		assertEquals(1, conditionElement.getAsJsonArray().size());
		assertEquals(originalKey.toString(), serializedElement.getAsJsonArray().get(0).getAsJsonObject().get("key").getAsString());


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
		assertNotNull(deserializedData.condition(), "Deserialized condition should be present");
		// Assert condition content
		assertEquals(originalData.condition().staticConditions().size(), deserializedData.condition().staticConditions().size());
		assertEquals(originalData.condition().staticConditions().get(0).type(), deserializedData.condition().staticConditions().get(0).type());


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
		assertNotNull(originalProperty.condition(), "Original property should have a condition");
		assertNotNull(roundTrippedTooltip.condition(), "Round-tripped property should have a condition");
		assertEquals(originalProperty.condition().staticConditions().size(), roundTrippedTooltip.condition().staticConditions().size());
		assertEquals(originalProperty.condition().staticConditions().get(0).type(), roundTrippedTooltip.condition().staticConditions().get(0).type());
		assertEquals(((TagMatchCondition)originalProperty.condition().staticConditions().get(0)).tag(), ((TagMatchCondition)roundTrippedTooltip.condition().staticConditions().get(0)).tag());
	}
}
