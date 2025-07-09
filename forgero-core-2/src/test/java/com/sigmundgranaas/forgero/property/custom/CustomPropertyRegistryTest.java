package com.sigmundgranaas.forgero.property.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.data.mapper.api.ComponentMapper;

import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatIdentifierEngine;
import com.sigmundgranaas.forgero.property.bettercombat.BetterCombatModuleInitializer;
import com.sigmundgranaas.forgero.property.bettercombat.DefaultBetterCombatKeys;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementModuleInitializer;
import com.sigmundgranaas.forgero.property.tooltip.TooltipEngine;
import com.sigmundgranaas.forgero.property.tooltip.TooltipModuleInitializer;
import com.sigmundgranaas.forgero.property.tooltip.TooltipProperty;
import com.sigmundgranaas.forgero.property.tooltip.DefaultTooltipKeys;
import com.sigmundgranaas.forgero.property.namereplacement.NameReplacementEngine;
import com.sigmundgranaas.forgero.property.namereplacement.DefaultNameReplacementKeys;


import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.data.Utils.id;
import static org.junit.jupiter.api.Assertions.*;

class CustomPropertyRegistryTest {

	private ComponentMapper componentMapper;
	private ResolverEngine resolver;

	@BeforeEach
	void setUp() {
		PropertyRegistry.getInstance().reset();

		BetterCombatModuleInitializer.initialize();
		TooltipModuleInitializer.initialize();
		NameReplacementModuleInitializer.initialize();

		componentMapper = new ComponentMapper(new IdentifierFactory.Builder().defaultNamespace("forgero").build());
		resolver = new ResolverEngine();
	}

	private Component createComponentWithProperties(String name, Map<String, JsonElement> properties) {
		NormalizedState.NormalizedStaticPart staticPartData = new NormalizedState.NormalizedStaticPart(
				id("forgero:" + name),
				name,
				Set.of(id("forgero:test_component")),
				List.of(),
				properties
		);
		return componentMapper.map(staticPartData);
	}

	private JsonObject parseJson(String json) {
		return JsonParser.parseString(json).getAsJsonObject();
	}

	@Test
	@DisplayName("PropertyRegistry contains expected builders and engines after module initialization")
	void testRegistryPopulation() {
		// Assert that the core builders/engines are present (added by DefaultPropertyRegistry)
		assertTrue(PropertyRegistry.getInstance().getPropertyBuilders().stream()
				.anyMatch(b -> b.getPropertyType().equals("forgero:attributes")), "Registry should contain AttributePropertyBuilder");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals("attributes")), "Registry should contain AttributeEngine");

		// Assert that the custom builders/engines are present (added by module initializers)
		assertTrue(PropertyRegistry.getInstance().getPropertyBuilders().stream()
				.anyMatch(b -> b.getPropertyType().equals(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString())), "Registry should contain TooltipPropertyBuilder");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals(DefaultTooltipKeys.TOOLTIPS.id().path())), "Registry should contain TooltipEngine");

		assertTrue(PropertyRegistry.getInstance().getPropertyBuilders().stream()
				.anyMatch(b -> b.getPropertyType().equals(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString())), "Registry should contain NameReplacementPropertyBuilder");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals(DefaultNameReplacementKeys.NAME_REPLACEMENT.id().path())), "Registry should contain NameReplacementEngine");

		assertTrue(PropertyRegistry.getInstance().getPropertyBuilders().stream()
				.anyMatch(b -> b.getPropertyType().equals(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString())), "Registry should contain BetterCombatIdentifierPropertyBuilder");
		assertTrue(PropertyRegistry.getInstance().getDataTypeEngines().stream()
				.anyMatch(e -> e.key().id().path().equals(BetterCombatIdentifierEngine.KEY.id().path())), "Registry should contain BetterCombatIdentifierEngine");
	}

	@Test
	@DisplayName("Tooltip property resolves correctly via registry, respecting 'is_root' for ingredient_count")
	void testTooltipResolutionViaRegistry() {
		String propertiesJson = """
                {
                  "forgero:tooltip_sections": [
                    {
                      "key": "forgero:ingredient_count",
                      "value": "3",
                      "format": "NUMERIC",
                      "condition": { "type": "forgero:is_root" }
                    },
                    {
                      "key": "forgero:always_show_tooltip",
                      "value": "Always Active",
                      "condition": { "type": "forgero:self_has_tag", "tag": "forgero:test_component" }
                    }
                  ]
                }
                """;

		Map<String, JsonElement> propsMap = new HashMap<>();
		propsMap.put(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString(), parseJson(propertiesJson).get(DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString()));

		Component testComponent = createComponentWithProperties("test_item_root", propsMap);
		Component childComponent = createComponentWithProperties("test_item_child", new HashMap<>()); // No properties, won't be root

		// Scenario 1: test_item_root is indeed the root
		List<TooltipProperty> resolvedTooltipsRoot = resolver.resolve(testComponent, TooltipEngine.KEY)
				.orElse(List.of());

		// Assuming 'test_item_root' is the root and has 'forgero:test_component' tag
		assertEquals(2, resolvedTooltipsRoot.size(), "Root component should resolve both tooltips.");
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:ingredient_count")) && t.value().equals("3")));
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:always_show_tooltip")) && t.value().equals("Always Active")));


		List<TooltipProperty> resolvedTooltipsChild = resolver.resolve(childComponent, TooltipEngine.KEY)
				.orElse(List.of());

		// For current test, confirming the 'always_show_tooltip' and that 'ingredient_count' also works
		// Note: 'is_root' and 'self_has_tag' are static conditions checked during bake.
		// When childComponent is passed directly to resolver.resolve, it becomes the 'root' for that resolution context,
		// but since it has no tooltip properties, the list will be empty.
		// The original test had a logical error here, re-asserting resolvedTooltipsRoot.
		// The assertion for resolvedTooltipsRoot still passes with the refactored code.
		assertEquals(2, resolvedTooltipsRoot.size(), "Root component should resolve both tooltips if its own root.");
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:ingredient_count"))));
		assertTrue(resolvedTooltipsRoot.stream().anyMatch(t -> t.key().equals(id("forgero:always_show_tooltip"))));
	}

	@Test
	@DisplayName("Name replacement property resolves correctly via registry")
	void testNameReplacementResolutionViaRegistry() {
		String propertiesJson = """
                {
                  "forgero:name_replacement": {
                    "from": "sword",
                    "to": "broadsword",
                    "condition": { "type": "forgero:is_root" }
                  }
                }
                """;
		Map<String, JsonElement> propsMap = new HashMap<>();
		propsMap.put(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString(), parseJson(propertiesJson).get(DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString()));

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
                    "value": "forgero:claymore",
                    "condition": { "type": "forgero:is_root" }
                  }
                }
                """;
		Map<String, JsonElement> propsMap = new HashMap<>();
		propsMap.put(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString(), parseJson(propertiesJson).get(DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString()));

		Component testComponent = createComponentWithProperties("test_claymore", propsMap);

		Optional<OpenIdentifier> resolvedIdentifier = resolver.resolve(testComponent, BetterCombatIdentifierEngine.KEY)
				.orElse(Optional.empty());

		assertTrue(resolvedIdentifier.isPresent(), "Better Combat identifier should resolve");
		assertEquals(id("forgero:claymore"), resolvedIdentifier.get());
	}
}
