package com.sigmundgranaas.forgero.data.processing.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.*;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.processing.api.DataProcessor;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.data.processing.api.RawDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DataProcessorImpl implements DataProcessor {

	private final Map<OpenIdentifier, DefinitionBuilder.MergedProperties> processedCache = new HashMap<>();
	private final Set<OpenIdentifier> recursionStack = new HashSet<>();
	private Map<OpenIdentifier, RawDefinition> rawSourceData;

	@Override
	public NormalizedState normalize(@NotNull Map<OpenIdentifier, RawDefinition> rawDefinitions) {
		this.rawSourceData = rawDefinitions;
		processedCache.clear();
		recursionStack.clear();

		Map<OpenIdentifier, NormalizedState.NormalizedMaterial> normalizedMaterials = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedShape> normalizedShapes = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedSchematic> normalizedSchematics = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedPartTemplate> normalizedPartTemplates = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedEquipmentTemplate> normalizedEquipmentTemplates = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedStaticPart> normalizedStaticParts = new HashMap<>();


		for (RawDefinition rawDef : rawDefinitions.values()) {
			OpenIdentifier id = rawDef.id();
			DefinitionBuilder.MergedProperties mergedProps = resolveAndMergePropertiesRecursive(id);

			Object originalDto = rawDef.data();
			if (originalDto instanceof MaterialData m) {
				normalizedMaterials.put(id, new NormalizedState.NormalizedMaterial(
						id, m.name(), mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(), m.host(), mergedProps.properties()
				));
			} else if (originalDto instanceof ShapeData s) {
				normalizedShapes.put(id, new NormalizedState.NormalizedShape(
						id, s.name(), mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(), s.host(), mergedProps.properties()
				));
			} else if (originalDto instanceof SchematicData s) {
				normalizedSchematics.put(id, new NormalizedState.NormalizedSchematic(
						id, s.name(), s.target(), s.craftingMaterial(),
						mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(), s.host(), mergedProps.properties()
				));
			} else if (originalDto instanceof PartTemplateData p) {
				normalizedPartTemplates.put(id, new NormalizedState.NormalizedPartTemplate(
						id, p.name(), mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						p.structure(), p.host_template(), p.upgrades(), mergedProps.properties()
				));
			} else if (originalDto instanceof EquipmentTemplateData t) {
				normalizedEquipmentTemplates.put(id, new NormalizedState.NormalizedEquipmentTemplate(
						id, t.name(), mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						t.structure(), t.host_template(), t.upgrades(), mergedProps.properties()
				));
			} else if (originalDto instanceof StaticPartData sp) {
				normalizedStaticParts.put(id, new NormalizedState.NormalizedStaticPart(
						id, sp.name(), mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						sp.host(), sp.upgrades(), mergedProps.properties()
				));
			} else {
				System.err.println("Warning: Unrecognized DTO type encountered during normalization: " + originalDto.getClass().getName());
			}
		}

		return new NormalizedState(
				Map.copyOf(normalizedMaterials), Map.copyOf(normalizedShapes), Map.copyOf(normalizedSchematics),
				Map.copyOf(normalizedPartTemplates), Map.copyOf(normalizedEquipmentTemplates), Map.copyOf(normalizedStaticParts)
		);
	}

	private DefinitionBuilder.MergedProperties resolveAndMergePropertiesRecursive(OpenIdentifier id) {
		if (processedCache.containsKey(id)) {
			return processedCache.get(id);
		}
		if (recursionStack.contains(id)) {
			throw new IllegalStateException("Cyclic include dependency detected involving: " + id);
		}

		recursionStack.add(id);

		RawDefinition currentRawDef = rawSourceData.get(id);
		if (currentRawDef == null) {
			recursionStack.remove(id);
			throw new IllegalArgumentException("Included definition not found: " + id);
		}

		DefinitionBuilder builder = new DefinitionBuilder();

		List<OpenIdentifier> includes = extractIncludes(currentRawDef.data());
		if (includes != null) {
			for (OpenIdentifier includedId : includes) {
				DefinitionBuilder.MergedProperties includedProps = resolveAndMergePropertiesRecursive(includedId);
				builder.merge(includedProps);
			}
		}

		mergeRawDtoPropertiesIntoBuilder(currentRawDef.data(), builder);

		recursionStack.remove(id);

		DefinitionBuilder.MergedProperties finalMergedProps = builder.buildMergedProperties();
		processedCache.put(id, finalMergedProps);
		return finalMergedProps;
	}

	private List<OpenIdentifier> extractIncludes(Object dto) {
		if (dto instanceof MaterialData m) return m.include();
		if (dto instanceof ShapeData s) return s.include();
		if (dto instanceof SchematicData s) return s.include();
		if (dto instanceof PartTemplateData p) return p.include();
		if (dto instanceof EquipmentTemplateData t) return t.include();
		if (dto instanceof StaticPartData sp) return sp.include();
		return null;
	}

	private void mergeRawDtoPropertiesIntoBuilder(Object dto, DefinitionBuilder builder) {
		if (dto instanceof PropertyContainer container) {
			if (container instanceof MaterialData m) builder.mergeTags(m.tags());
			if (container instanceof ShapeData s) builder.mergeTags(s.tags());
			if (container instanceof SchematicData s) builder.mergeTags(s.tags());
			if (container instanceof PartTemplateData p) builder.mergeTags(p.tags());
			if (container instanceof EquipmentTemplateData e) builder.mergeTags(e.tags());
			if (container instanceof StaticPartData s) builder.mergeTags(s.tags());

			// Merge custom properties. Local properties override inherited ones.
			builder.mergeProperties(container.properties());

			// Consolidate dedicated fields into the properties map. This will merge
			// with any existing "forgero:attributes" or "forgero:features" from includes.
			builder.consolidateAttributes(container.attributes());
			builder.consolidateFeatures(container.features());
		} else {
			System.err.println("Warning: DTO type " + dto.getClass().getName() + " does not implement PropertyContainer and cannot be merged.");
		}
	}
}
