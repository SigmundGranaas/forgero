package com.sigmundgranaas.forgero.data.pipeline.util;

import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofSlot;
import com.sigmundgranaas.forgero.cof.dto.CofStructure;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IdTemplateResolver to ensure correct template resolution logic.
 */
class IdTemplateResolverTest {

	private IdentifierFactory idFactory;
	private Map<OpenIdentifier, RawDefinition> rawDefinitions;
	private IdTemplateResolver resolver;

	@BeforeEach
	void setUp() {
		idFactory = CodecConstants.IDENTIFIER_FACTORY;
		rawDefinitions = new HashMap<>();
		resolver = new IdTemplateResolver(idFactory, rawDefinitions);
	}

	@Test
	void testSimpleTemplate() {
		// Create components
		CofComponent material = createComponent("iron", null);
		CofComponent shape = createComponent("pickaxe_head_shape", null);

		Map<String, CofComponent> combination = Map.of(
				"material", material,
				"shape", shape
		);

		String result = resolver.resolve("{material.name}-{shape.name}", combination);
		assertEquals("iron-pickaxe_head", result);
	}

	@Test
	void testShapeNameStripsShapeSuffix() {
		CofComponent shape = createComponent("pickaxe_head_shape", null);

		Map<String, CofComponent> combination = Map.of("shape", shape);

		// Using {shape.name} should strip _shape suffix
		String result = resolver.resolve("{shape.name}", combination);
		assertEquals("pickaxe_head", result);
	}

	@Test
	void testShapeNameProperty() {
		CofComponent shape = createComponent("pickaxe_head_shape", null);

		Map<String, CofComponent> combination = Map.of("shape", shape);

		// Using {shape.shape_name} should also strip _shape suffix
		String result = resolver.resolve("{shape.shape_name}", combination);
		assertEquals("pickaxe_head", result);
	}

	@Test
	void testShapeNameWithoutSuffix() {
		CofComponent shape = createComponent("pickaxe_head", null);

		Map<String, CofComponent> combination = Map.of("shape", shape);

		String result = resolver.resolve("{shape.name}", combination);
		assertEquals("pickaxe_head", result);
	}

	@Test
	void testNestedPropertyAccess() {
		// Create nested structure: head contains material
		CofComponent material = createComponent("iron", null);
		CofComponent head = createComponent("pickaxe_head", Map.of(
				idFactory.of("material"), new CofSlot(
						idFactory.of("material"),
						idFactory.of("tool_material"),
						null,
						material,
						null
				)
		));

		Map<String, CofComponent> combination = Map.of("head", head);

		String result = resolver.resolve("{head.material.name}-tool", combination);
		assertEquals("iron-tool", result);
	}

	@Test
	void testDeeplyNestedPropertyAccess() {
		// Create deeply nested structure
		CofComponent iron = createComponent("iron", null);
		CofComponent binding = createComponent("binding", Map.of(
				idFactory.of("material"), new CofSlot(
						idFactory.of("material"),
						idFactory.of("binding_material"),
						null,
						iron,
						null
				)
		));
		CofComponent head = createComponent("pickaxe_head", Map.of(
				idFactory.of("binding"), new CofSlot(
						idFactory.of("binding"),
						idFactory.of("binding_slot"),
						null,
						binding,
						null
				)
		));

		Map<String, CofComponent> combination = Map.of("head", head);

		String result = resolver.resolve("{head.binding.material.name}", combination);
		assertEquals("iron", result);
	}

	@Test
	void testMissingComponentKeepsPlaceholder() {
		Map<String, CofComponent> combination = Map.of();

		String result = resolver.resolve("{material.name}-tool", combination);
		assertEquals("{material.name}-tool", result);
	}

	@Test
	void testMissingNestedSlotKeepsPlaceholder() {
		CofComponent head = createComponent("pickaxe_head", null);

		Map<String, CofComponent> combination = Map.of("head", head);

		// Try to access material slot that doesn't exist
		String result = resolver.resolve("{head.material.name}", combination);
		assertEquals("{head.material.name}", result);
	}

	@Test
	void testInvalidPlaceholderFormatKeepsPlaceholder() {
		CofComponent material = createComponent("iron", null);

		Map<String, CofComponent> combination = Map.of("material", material);

		// Invalid format (no property name)
		String result = resolver.resolve("{material}", combination);
		assertEquals("{material}", result);
	}

	@Test
	void testMultiplePlaceholdersInTemplate() {
		CofComponent material = createComponent("iron", null);
		CofComponent shape = createComponent("pickaxe_head_shape", null);
		CofComponent handle = createComponent("oak_handle", null);

		Map<String, CofComponent> combination = Map.of(
				"material", material,
				"shape", shape,
				"handle", handle
		);

		String result = resolver.resolve("{material.name}-{shape.name}+{handle.name}", combination);
		assertEquals("iron-pickaxe_head+oak_handle", result);
	}

	@Test
	void testTemplateWithNoPlaceholders() {
		Map<String, CofComponent> combination = Map.of();

		String result = resolver.resolve("static-id", combination);
		assertEquals("static-id", result);
	}

	@Test
	void testEmptyStructureSlotKeepsPlaceholder() {
		// Create head with empty material slot
		CofComponent head = createComponent("pickaxe_head", Map.of(
				idFactory.of("material"), new CofSlot(
						idFactory.of("material"),
						idFactory.of("tool_material"),
						null,
						null,  // Empty slot
						null
				)
		));

		Map<String, CofComponent> combination = Map.of("head", head);

		String result = resolver.resolve("{head.material.name}", combination);
		assertEquals("{head.material.name}", result);
	}

	@Test
	void testShapeNameFromInclude() {
		// Create a shape definition with includes
		OpenIdentifier shapeId = idFactory.of("custom_pickaxe_head_shape");
		ResourceData shapeData = new ResourceData(
				idFactory.of("forgero:shape"), // type as OpenIdentifier
				"custom_pickaxe_head_shape",   // name
				List.of(idFactory.of("base_pickaxe_head_shape")), // include
				null, // tags
				null, // localTags
				null, // host
				null, // attributes
				null, // localAttributes
				null  // properties
		);
		rawDefinitions.put(shapeId, new RawDefinition(shapeId, shapeData));

		CofComponent shape = createComponent("custom_pickaxe_head_shape", null);
		Map<String, CofComponent> combination = Map.of("shape", shape);

		String result = resolver.resolve("{shape.shape_name}", combination);
		// Should use the include name and strip suffix
		assertEquals("base_pickaxe_head", result);
	}

	@Test
	void testComplexRealWorldTemplate() {
		// Simulate a real-world template: "{head.material.name}-{head.shape.name}"
		CofComponent ironMaterial = createComponent("iron", null);
		CofComponent pickaxeShape = createComponent("pickaxe_head_shape", null);
		CofComponent head = createComponent("generated_pickaxe_head", Map.of(
				idFactory.of("material"), new CofSlot(
						idFactory.of("material"),
						idFactory.of("tool_material"),
						null,
						ironMaterial,
						null
				),
				idFactory.of("shape"), new CofSlot(
						idFactory.of("shape"),
						idFactory.of("head_shape"),
						null,
						pickaxeShape,
						null
				)
		));

		Map<String, CofComponent> combination = Map.of("head", head);

		String result = resolver.resolve("{head.material.name}-{head.shape.name}", combination);
		assertEquals("iron-pickaxe_head", result);
	}

	// Helper methods

	private CofComponent createComponent(String name, Map<OpenIdentifier, CofSlot> slots) {
		OpenIdentifier id = idFactory.of(name);
		CofStructure structure = slots != null ? new CofStructure(slots) : null;
		return new CofComponent(
				id,
				idFactory.of("static_component"),
				Set.of(),
				Map.of(),
				structure,
				null,
				1
		);
	}
}
