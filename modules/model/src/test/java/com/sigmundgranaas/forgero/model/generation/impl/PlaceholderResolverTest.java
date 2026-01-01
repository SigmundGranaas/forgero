package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlaceholderResolver - critical template string resolution.
 * Tests placeholder patterns like {target.name}, {material.name}, etc.
 */
class PlaceholderResolverTest {

	private PlaceholderResolver resolver;
	private Map<String, Component> context;

	@BeforeEach
	void setUp() {
		resolver = new PlaceholderResolver();
		context = new HashMap<>();
	}

	@Test
	void testResolve_nullTemplate_returnsEmpty() {
		String result = resolver.resolve(null, context);
		assertEquals("", result);
	}

	@Test
	void testResolve_emptyTemplate_returnsEmpty() {
		String result = resolver.resolve("", context);
		assertEquals("", result);
	}

	@Test
	void testResolve_noPlaceholders_returnsOriginal() {
		String template = "forgero:item/iron_pickaxe";
		String result = resolver.resolve(template, context);
		assertEquals("forgero:item/iron_pickaxe", result);
	}

	@Test
	void testResolve_simplePlaceholder_resolvesToName() {
		// Mock component with simple name
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		context.put("material", material);

		String result = resolver.resolve("{material}", context);
		assertEquals("iron", result);
	}

	@Test
	void testResolve_placeholderWithName_resolvesToName() {
		// Test {material.name} pattern
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		context.put("material", material);

		String result = resolver.resolve("{material.name}", context);
		assertEquals("iron", result);
	}

	@Test
	void testResolve_unknownPlaceholder_keptAsIs() {
		// Unknown placeholders should be preserved
		String template = "{unknown_placeholder}";
		String result = resolver.resolve(template, context);
		assertEquals("{unknown_placeholder}", result);
	}

	@Test
	void testResolve_multiplePlaceholders_allResolved() {
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		Component target = mock(Component.class);
		when(target.id()).thenReturn(OpenIdentifier.of("forgero", "pickaxe_head"));

		context.put("material", material);
		context.put("target", target);

		String result = resolver.resolve("forgero:texture/{material}/{target}", context);
		assertEquals("forgero:texture/iron/pickaxe_head", result);
	}

	@Test
	void testResolve_mixedKnownUnknown_partialResolution() {
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		context.put("material", material);

		String result = resolver.resolve("{material}/{unknown}", context);
		assertEquals("iron/{unknown}", result);
	}

	@Test
	void testResolve_nestedComponent_traversesStructure() {
		// Create nested structure: head -> material
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		ComponentPart part = mock(ComponentPart.class);
		when(part.content()).thenReturn(material);
		when(part.getContent()).thenReturn(material);

		ComponentStructure structure = mock(ComponentStructure.class);
		when(structure.getPart(OpenIdentifier.of("forgero", "material")))
				.thenReturn(Optional.of(part));

		StructuredComponent head = mock(StructuredComponent.class);
		when(head.id()).thenReturn(OpenIdentifier.of("forgero", "pickaxe_head"));
		when(head.structure()).thenReturn(structure);

		context.put("head", head);

		String result = resolver.resolve("{head.material.name}", context);
		assertEquals("iron", result);
	}

	@Test
	void testResolve_shapeSuffix_stripped() {
		// Test that _shape suffix is removed for cleaner names
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron_shape"));

		context.put("material", material);

		String result = resolver.resolve("{material}", context);
		assertEquals("iron", result);
	}

	@Test
	void testResolve_complexTemplate_fullResolution() {
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "oak"));

		Component target = mock(Component.class);
		when(target.id()).thenReturn(OpenIdentifier.of("forgero", "handle"));

		context.put("material", material);
		context.put("target", target);

		String template = "forgero:texture_template/{target}/{material}";
		String result = resolver.resolve(template, context);
		assertEquals("forgero:texture_template/handle/oak", result);
	}

	@Test
	void testResolve_nonStructuredComponent_cannotTraverse() {
		// Non-structured components cannot be traversed
		Component simple = mock(Component.class);
		when(simple.id()).thenReturn(OpenIdentifier.of("forgero", "simple"));

		context.put("simple", simple);

		// Trying to traverse into non-structured component fails
		String result = resolver.resolve("{simple.material.name}", context);
		assertEquals("{simple.material.name}", result);
	}

	@Test
	void testResolve_missingNestedPart_keptAsIs() {
		// When traversal path doesn't exist, keep placeholder
		ComponentStructure structure = mock(ComponentStructure.class);
		when(structure.getPart(any())).thenReturn(Optional.empty());

		StructuredComponent head = mock(StructuredComponent.class);
		when(head.id()).thenReturn(OpenIdentifier.of("forgero", "head"));
		when(head.structure()).thenReturn(structure);

		context.put("head", head);

		String result = resolver.resolve("{head.missing.name}", context);
		assertEquals("{head.missing.name}", result);
	}

	@Test
	void testResolve_emptyContext_allPlaceholdersKept() {
		// Empty context means all placeholders stay as-is
		String template = "{material}/{target}/{unknown}";
		String result = resolver.resolve(template, new HashMap<>());
		assertEquals("{material}/{target}/{unknown}", result);
	}

	@Test
	void testResolve_adjacentPlaceholders_bothResolved() {
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		Component target = mock(Component.class);
		when(target.id()).thenReturn(OpenIdentifier.of("forgero", "pickaxe"));

		context.put("material", material);
		context.put("target", target);

		String result = resolver.resolve("{material}{target}", context);
		assertEquals("ironpickaxe", result);
	}

	@Test
	void testResolve_placeholderAtStart_resolved() {
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		context.put("material", material);

		String result = resolver.resolve("{material}_blade", context);
		assertEquals("iron_blade", result);
	}

	@Test
	void testResolve_placeholderAtEnd_resolved() {
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		context.put("material", material);

		String result = resolver.resolve("blade_{material}", context);
		assertEquals("blade_iron", result);
	}

	@Test
	void testResolve_realWorldExample_paletteResolution() {
		// Real-world palette resolution: "forgero:palette/{material.name}"
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		context.put("material", material);

		String result = resolver.resolve("forgero:palette/{material.name}", context);
		assertEquals("forgero:palette/iron", result);
	}

	@Test
	void testResolve_realWorldExample_textureOutput() {
		// Real-world texture output: "forgero:item/{material.name}-{target.name}"
		Component material = mock(Component.class);
		when(material.id()).thenReturn(OpenIdentifier.of("forgero", "iron"));

		Component target = mock(Component.class);
		when(target.id()).thenReturn(OpenIdentifier.of("forgero", "pickaxe_head"));

		context.put("material", material);
		context.put("target", target);

		String result = resolver.resolve("forgero:item/{material.name}-{target.name}", context);
		assertEquals("forgero:item/iron-pickaxe_head", result);
	}
}
