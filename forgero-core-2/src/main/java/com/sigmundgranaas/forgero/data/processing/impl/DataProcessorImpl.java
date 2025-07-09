package com.sigmundgranaas.forgero.data.processing.impl;

import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.data.processing.api.RawDefinition;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.SchematicData;
import com.sigmundgranaas.forgero.data.loading.api.data.ShapeData;
import com.sigmundgranaas.forgero.data.loading.api.data.StaticPartData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.processing.api.DataProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataProcessorImpl implements DataProcessor {

	// Cache for already processed DefinitionBuilders to prevent redundant work and detect cycles
	private final Map<OpenIdentifier, DefinitionBuilder.MergedProperties> processedCache = new HashMap<>();
	// Stack to detect cyclic dependencies during processing
	private final Set<OpenIdentifier> recursionStack = new HashSet<>();
	// The raw data provided as input for the current processing run
	private Map<OpenIdentifier, RawDefinition> rawSourceData;

	@Override
	public NormalizedState normalize(@NotNull Map<OpenIdentifier, RawDefinition> rawDefinitions) {
		this.rawSourceData = rawDefinitions;
		processedCache.clear();
		recursionStack.clear(); // Ensure clean state for each new processing call

		Map<OpenIdentifier, NormalizedState.NormalizedMaterial> normalizedMaterials = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedShape> normalizedShapes = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedSchematic> normalizedSchematics = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedPartTemplate> normalizedPartTemplates = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedEquipmentTemplate> normalizedEquipmentTemplates = new HashMap<>();
		Map<OpenIdentifier, NormalizedState.NormalizedStaticPart> normalizedStaticParts = new HashMap<>();


		for (RawDefinition rawDef : rawDefinitions.values()) {
			OpenIdentifier id = rawDef.id();
			DefinitionBuilder.MergedProperties mergedProps = resolveAndMergePropertiesRecursive(id);

			// Now, create the specific NormalizedX record based on the original DTO type
			Object originalDto = rawDef.data();
			if (originalDto instanceof MaterialData m) {
				normalizedMaterials.put(id, new NormalizedState.NormalizedMaterial(
						id,
						m.name(),
						mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						mergedProps.attributes(),
						mergedProps.features()
				));
			} else if (originalDto instanceof ShapeData s) {
				normalizedShapes.put(id, new NormalizedState.NormalizedShape(
						id,
						s.name(),
						mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						mergedProps.attributes(),
						mergedProps.features()
				));
			} else if (originalDto instanceof SchematicData s) {
				normalizedSchematics.put(id, new NormalizedState.NormalizedSchematic(
						id,
						s.name(),
						s.target(),
						s.craftingMaterial(),
						mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>()
				));
			} else if (originalDto instanceof PartTemplateData p) {
				normalizedPartTemplates.put(id, new NormalizedState.NormalizedPartTemplate(
						id,
						p.name(),
						mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						p.structure(),
						p.upgrades()
				));
			} else if (originalDto instanceof EquipmentTemplateData t) {
				normalizedEquipmentTemplates.put(id, new NormalizedState.NormalizedEquipmentTemplate(
						id,
						t.name(),
						mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						t.structure(),
						t.upgrades()
				));
			} else if (originalDto instanceof StaticPartData sp) {
				normalizedStaticParts.put(id, new NormalizedState.NormalizedStaticPart(
						id,
						sp.name(),
						mergedProps.tags() != null ? mergedProps.tags() : new HashSet<>(),
						mergedProps.attributes(),
						mergedProps.features(),
						sp.upgrades()
				));
			} else {
				System.err.println("Warning: Unrecognized DTO type encountered during normalization: " + originalDto.getClass().getName());
			}
		}

		return new NormalizedState(
				Map.copyOf(normalizedMaterials),
				Map.copyOf(normalizedShapes),
				Map.copyOf(normalizedSchematics),
				Map.copyOf(normalizedPartTemplates),
				Map.copyOf(normalizedEquipmentTemplates),
				Map.copyOf(normalizedStaticParts)
		);
	}

	/**
	 * Recursively resolves includes and merges properties for a given definition.
	 * The result is cached to prevent redundant work and detect cycles.
	 *
	 * @param id The OpenIdentifier of the definition to process.
	 * @return A MergedProperties record containing all resolved and merged properties.
	 * @throws IllegalArgumentException if an included ID refers to a non-existent definition.
	 * @throws IllegalStateException    if a cyclic include dependency is detected.
	 */
	private DefinitionBuilder.MergedProperties resolveAndMergePropertiesRecursive(OpenIdentifier id) {
		if (processedCache.containsKey(id)) {
			return processedCache.get(id); // Already processed, return from cache
		}
		if (recursionStack.contains(id)) {
			// Cycle detected!
			throw new IllegalStateException("Cyclic include dependency detected involving: " + id);
		}

		recursionStack.add(id);

		RawDefinition currentRawDef = rawSourceData.get(id);
		if (currentRawDef == null) {
			recursionStack.remove(id); // Clean up stack before throwing
			throw new IllegalArgumentException("Included definition not found: " + id);
		}

		DefinitionBuilder builder = new DefinitionBuilder();

		// 1. Recursively process and apply properties from includes (later includes override earlier ones)
		List<OpenIdentifier> includes = extractIncludes(currentRawDef.data());
		if (includes != null) {
			for (OpenIdentifier includedId : includes) {
				DefinitionBuilder.MergedProperties includedProps = resolveAndMergePropertiesRecursive(includedId); // Recursive call
				builder.mergeTags(includedProps.tags());
				builder.mergeAttributes(includedProps.attributes());
				builder.mergeFeatures(includedProps.features());
			}
		}

		// 2. Apply properties from the current raw data (these override all previously accumulated properties)
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
		if (dto instanceof MaterialData m) {
			builder.mergeTags(m.tags());
			builder.mergeAttributes(m.attributes());
			builder.mergeFeatures(m.features());
		} else if (dto instanceof ShapeData s) {
			builder.mergeTags(s.tags());
			builder.mergeAttributes(s.attributes());
			builder.mergeFeatures(s.features());
		} else if (dto instanceof SchematicData s) {
			builder.mergeTags(s.tags());
			// Schematics typically don't have attributes/features for merging
		} else if (dto instanceof PartTemplateData p) {
			builder.mergeTags(p.tags());
			builder.mergeAttributes(p.attributes());
			builder.mergeFeatures(p.features());
		} else if (dto instanceof EquipmentTemplateData t) {
			builder.mergeTags(t.tags());
			builder.mergeAttributes(t.attributes());
			builder.mergeFeatures(t.features());
		} else if (dto instanceof StaticPartData sp) {
			builder.mergeTags(sp.tags());
			builder.mergeAttributes(sp.attributes());
			builder.mergeFeatures(sp.features());
		} else {
			// This case should ideally not be reached if all DTO types correctly implement the `include` and property access patterns.
			// Or if the initial loading (Stage 1) filters out unknown types.
			// If it's a generated DTO type, it won't have includes, so it's skipped for merge.
			// Currently, RawDefinition can wrap `Generated...Data` if the file structure implies it (e.g. pre-generated data files).
			// But for *this* process, we assume RawDefinition wraps original JSON DTOs only.
			// We can log a warning or throw for truly unexpected types.
			System.err.println("Warning: DTO type " + dto.getClass().getName() + " does not contribute mergeable properties or is not expected in raw data processing.");
		}
	}
}
