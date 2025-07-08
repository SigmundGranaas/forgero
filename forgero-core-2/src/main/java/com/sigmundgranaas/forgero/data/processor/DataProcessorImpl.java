package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.IdentifiedTopLevelData; // NEW: Use the wrapper
import com.sigmundgranaas.forgero.data.v3.dto.MaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.SchematicData;
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.TopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DataProcessorImpl implements DataProcessor {

	// Cache for already processed definitions to prevent redundant work and detect cycles
	private final Map<OpenIdentifier, TopLevelData> processedCache = new HashMap<>();
	// The raw data provided as input
	private Map<OpenIdentifier, TopLevelData> rawSourceData; // Now expects IdentifiedTopLevelData wrapped DTOs
	// Stack to detect cyclic dependencies during processing
	private final Set<OpenIdentifier> recursionStack = new HashSet<>();

	@Override
	public Map<OpenIdentifier, TopLevelData> process(@NotNull Map<OpenIdentifier, TopLevelData> rawData) {
		this.rawSourceData = rawData; // Input is already wrapped in IdentifiedTopLevelData
		processedCache.clear();
		recursionStack.clear(); // Ensure clean state for each new processing call

		// Process each top-group definition that exists in the raw data
		rawData.keySet().forEach(this::processDefinition);

		return Map.copyOf(processedCache);
	}

	/**
	 * Recursively processes a single definition, resolving its includes.
	 * This method is memoized using `processedCache`.
	 *
	 * @param id The OpenIdentifier of the definition to process.
	 * @return The fully processed and merged TopLevelData.
	 * @throws IllegalArgumentException if an included ID refers to a non-existent definition.
	 * @throws IllegalStateException if a cyclic include dependency is detected.
	 */
	private TopLevelData processDefinition(OpenIdentifier id) {
		if (processedCache.containsKey(id)) {
			return processedCache.get(id); // Already processed, return from cache
		}
		if (recursionStack.contains(id)) {
			// Cycle detected!
			throw new IllegalStateException("Cyclic include dependency detected involving: " + id);
		}

		recursionStack.add(id);

		TopLevelData currentIdentifiedData = rawSourceData.get(id);
		if (currentIdentifiedData == null) {
			recursionStack.remove(id); // Clean up stack before throwing
			throw new IllegalArgumentException("Included definition not found: " + id);
		}

		// Initialize with empty mutable collections for accumulation.
		// These will be populated by merging includes and then the current data.
		Set<OpenIdentifier> accumulatedTags = new LinkedHashSet<>();
		Map<OpenIdentifier, AttributeData> accumulatedAttributes = new HashMap<>();
		Map<OpenIdentifier, FeatureData> accumulatedFeatures = new HashMap<>();

		// 1. Process and apply properties from includes (later includes override earlier ones)
		List<OpenIdentifier> includes = currentIdentifiedData.include();
		if (includes != null && !includes.isEmpty()) {
			for (OpenIdentifier includedId : includes) {
				TopLevelData includedProcessedData = processDefinition(includedId); // Recursive call for included DTOs

				// Apply properties from the included data
				// Tags are additive (set union)
				if (includedProcessedData.tags() != null) {
					accumulatedTags.addAll(Objects.requireNonNull(includedProcessedData.tags()));
				}
				// Attributes/Features: put() handles overrides by ID/Type
				includedProcessedData.getAttributesMap().forEach(accumulatedAttributes::put);
				includedProcessedData.getFeaturesMap().forEach(accumulatedFeatures::put);
			}
		}

		// 2. Apply properties from the current raw data (these override all previously accumulated properties from includes)
		// Access data from the raw DTO, not the wrapper's `tags()` method
		Object rawDto = currentIdentifiedData. unwrapAs(Object.class); // Get the raw DTO for direct field access
		if (rawDto instanceof MaterialData m) {
			if (m.tags() != null) accumulatedTags.addAll(m.tags());
			m.getAttributesMap().forEach(accumulatedAttributes::put);
			m.getFeaturesMap().forEach(accumulatedFeatures::put);
		} else if (rawDto instanceof PartTemplateData p) {
			if (p.tags() != null) accumulatedTags.addAll(p.tags());
			p.getAttributesMap().forEach(accumulatedAttributes::put);
			p.getFeaturesMap().forEach(accumulatedFeatures::put);
		} else if (rawDto instanceof ToolTemplateData t) {
			if (t.tags() != null) accumulatedTags.addAll(t.tags());
			t.getAttributesMap().forEach(accumulatedAttributes::put);
			t.getFeaturesMap().forEach(accumulatedFeatures::put);
		} else if (rawDto instanceof SchematicData s) {
			if (s.tags() != null) accumulatedTags.addAll(s.tags());
			// Schematics don't have attributes/features for merging in this context, maps are empty.
		} else if (rawDto instanceof StaticPartData sp) {
			if (sp.tags() != null) accumulatedTags.addAll(sp.tags());
			sp.getAttributesMap().forEach(accumulatedAttributes::put);
			sp.getFeaturesMap().forEach(accumulatedFeatures::put);
		} else {
			throw new IllegalStateException("Unsupported raw DTO type for property extraction: " + rawDto.getClass().getName());
		}

		recursionStack.remove(id);

		// Reconstruct a new DTO of the original type with merged properties, then wrap it in IdentifiedTopLevelData.
		Object mergedRawDto = createMergedDto(
				rawDto, // The original DTO (MaterialData, PartTemplateData etc.)
				null, // Include list is always null in the merged DTO
				accumulatedTags.isEmpty() ? null : new ArrayList<>(accumulatedTags), // Convert Set to List
				accumulatedAttributes.isEmpty() ? null : new ArrayList<>(accumulatedAttributes.values()),
				accumulatedFeatures.isEmpty() ? null : new ArrayList<>(accumulatedFeatures.values())
		);

		IdentifiedTopLevelData finalProcessedData = new IdentifiedTopLevelData(id, mergedRawDto);
		processedCache.put(id, finalProcessedData);
		return finalProcessedData;
	}

	/**
	 * Helper to create a new DTO instance (e.g., MaterialData, PartTemplateData) with merged properties.
	 * This method acts as a factory, reconstructing the original DTO type with updated lists.
	 *
	 * @param originalDto The original raw DTO object to derive type and non-merged fields from.
	 * @param newIncludes The (null) list of includes for the new DTO.
	 * @param newTags The merged list of tags.
	 * @param newAttributes The merged list of attributes.
	 * @param newFeatures The merged list of features.
	 * @return A new instance of the original DTO's type, with merged property lists.
	 * @throws IllegalArgumentException if an unsupported DTO type is provided.
	 */
	private Object createMergedDto(
			Object originalDto,
			@Nullable List<OpenIdentifier> newIncludes,
			@Nullable List<OpenIdentifier> newTags,
			@Nullable List<AttributeData> newAttributes,
			@Nullable List<FeatureData> newFeatures
	) {
		if (originalDto instanceof MaterialData m) {
			return new MaterialData(m.type(), m.name(), newIncludes, newTags, newAttributes, newFeatures);
		} else if (originalDto instanceof PartTemplateData p) {
			return new PartTemplateData(p.type(), p.name(), newIncludes, newTags, p.structure(), p.upgrades(), p.naming(), newAttributes, newFeatures);
		} else if (originalDto instanceof ToolTemplateData t) {
			return new ToolTemplateData(t.type(), t.name(), newIncludes, newTags, t.structure(), t.upgrades(), newAttributes, newFeatures);
		} else if (originalDto instanceof SchematicData s) {
			return new SchematicData(s.type(), s.name(), newIncludes, newTags, s.target(), s.craftingMaterial());
		} else if (originalDto instanceof StaticPartData sp) {
			return new StaticPartData(sp.type(), sp.name(), newIncludes, newTags, newAttributes, newFeatures);
		} else {
			throw new IllegalArgumentException("Unsupported DTO type for merging in createMergedDto: " + originalDto.getClass().getName());
		}
	}
}
