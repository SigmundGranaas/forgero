package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.pipeline.util.IdTemplateResolver;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateGeneratorMinimalTest {

	@Test
	void minimalTest() {
		// Setup
		IdentifierFactory idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

		TagGraphBuilder tagGraphBuilder = new TagGraphBuilder();
		tagGraphBuilder.add(idFactory.of("material"), Set.of());
		tagGraphBuilder.add(idFactory.of("shape"), Set.of());
		TagResolver tagResolver = tagGraphBuilder.build();

		PropertyMerger propertyMerger = new PropertyMerger(Map.of());
		Map<OpenIdentifier, CofComponent> staticComponents = new HashMap<>();
		Map<OpenIdentifier, RawDefinition> rawDefinitions = new HashMap<>();
		IdTemplateResolver idTemplateResolver = new IdTemplateResolver(idFactory, rawDefinitions);

		// Create iron material
		OpenIdentifier ironId = idFactory.of("iron");
		Set<OpenIdentifier> ironTags = Set.of(idFactory.of("material"));
		CofComponent iron = new CofComponent(ironId, ComponentTypeRegistry.STATIC_COMPONENT,
			Optional.of(ironTags), Optional.of(Map.of()), Optional.empty(), Optional.empty(), Optional.of(1));
		staticComponents.put(ironId, iron);

		ResourceData ironData = new ResourceData(
			idFactory.of("forgero:static"), "iron", null, List.of(idFactory.of("material")),
			null, null, null, null, null);
		rawDefinitions.put(ironId, new RawDefinition(ironId, ironData));

		// Create shape
		OpenIdentifier shapeId = idFactory.of("pickaxe_head_shape");
		Set<OpenIdentifier> shapeTags = Set.of(idFactory.of("shape"));
		CofComponent shape = new CofComponent(shapeId, ComponentTypeRegistry.STATIC_COMPONENT,
			Optional.of(shapeTags), Optional.of(Map.of()), Optional.empty(), Optional.empty(), Optional.of(1));
		staticComponents.put(shapeId, shape);

		ResourceData shapeData = new ResourceData(
			idFactory.of("forgero:static"), "pickaxe_head_shape", null, List.of(idFactory.of("shape")),
			null, null, null, null, null);
		rawDefinitions.put(shapeId, new RawDefinition(shapeId, shapeData));

		// Create template
		PartTemplateData template = new PartTemplateData(
			idFactory.of("part_template"), "Part template", null, null, null,
			new PartTemplateStructureData(
				"forgero:{material.name}-{shape.name}",
				Map.of(
					"material", new PartTemplateStructureSlotData(idFactory.of("material"), null, "Material"),
					"shape", new PartTemplateStructureSlotData(idFactory.of("shape"), null, "Shape")
				)
			),
			null, null, null, null
		);
		rawDefinitions.put(idFactory.of("part_template"), new RawDefinition(idFactory.of("part_template"), template));

		// Generate
		TemplateGenerator generator;
		TemplateGenerator.TemplateResult result;
		try {
			generator = new TemplateGenerator(idFactory, tagResolver, propertyMerger,
				idTemplateResolver, staticComponents, rawDefinitions);
			result = generator.generate();
		} catch (Exception e) {
			System.err.println("Exception during generation: " + e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		// Assert
		System.out.println("Generated " + result.components().size() + " components:");
		result.components().forEach(comp -> {
			System.out.println("  - " + comp.id() + " (type: " + comp.componentType() + ")");
		});

		assertEquals(1, result.components().size(), "Should generate exactly 1 component");
		CofComponent generated = result.components().get(0);
		System.out.println("Expected ID: " + idFactory.of("iron-pickaxe_head"));
		System.out.println("Actual ID: " + generated.id());
		assertEquals(idFactory.of("iron-pickaxe_head"), generated.id());
	}
}
