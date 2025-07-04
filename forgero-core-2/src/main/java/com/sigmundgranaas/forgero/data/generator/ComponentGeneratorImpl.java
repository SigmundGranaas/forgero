package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.v3.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.v3.dto.IdentifiedTopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.MaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateNamingData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.PartTemplateStructureMaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.SchematicData;
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.ToolTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.TopLevelData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentGeneratorImpl implements ComponentGenerator {

	private final IdentifierFactory idFactory;

	public ComponentGeneratorImpl(IdentifierFactory idFactory) {
		this.idFactory = idFactory;
	}

	@Override
	public Map<OpenIdentifier, TopLevelData> generate(Map<OpenIdentifier, TopLevelData> normalizedData, TagGraph tagGraph) {
		Map<OpenIdentifier, TopLevelData> generatedItems = new HashMap<>();

		// Segregate input data by type. These lists now contain the IdentifiedTopLevelData wrappers.
		List<IdentifiedTopLevelData> materials = normalizedData.values().stream()
				.filter(d -> d.unwrapAs(Object.class) instanceof MaterialData)
				.map(IdentifiedTopLevelData.class::cast)
				.toList();
		List<IdentifiedTopLevelData> partTemplates = normalizedData.values().stream()
				.filter(d -> d.unwrapAs(Object.class) instanceof PartTemplateData)
				.map(IdentifiedTopLevelData.class::cast)
				.toList();
		List<IdentifiedTopLevelData> toolTemplates = normalizedData.values().stream()
				.filter(d -> d.unwrapAs(Object.class) instanceof ToolTemplateData)
				.map(IdentifiedTopLevelData.class::cast)
				.toList();
		List<IdentifiedTopLevelData> staticParts = normalizedData.values().stream()
				.filter(d -> d.unwrapAs(Object.class) instanceof StaticPartData)
				.map(IdentifiedTopLevelData.class::cast)
				.toList();
		List<IdentifiedTopLevelData> schematics = normalizedData.values().stream()
				.filter(d -> d.unwrapAs(Object.class) instanceof SchematicData)
				.map(IdentifiedTopLevelData.class::cast)
				.toList();

		// Stage 3.1: Pass through Static Parts and Schematics (unchanged, as they are already IdentifiedTopLevelData)
		// No need to re-wrap, just add them to the generatedItems map.
		staticParts.forEach(data -> generatedItems.put(data.id(), data));
		schematics.forEach(data -> generatedItems.put(data.id(), data));

		// Keep track of newly generated parts for tool generation (wrapped in IdentifiedTopLevelData)
		Map<OpenIdentifier, IdentifiedTopLevelData> generatedPartsMap = new HashMap<>();

		// Stage 3.2: Generate Parts (Material + Part Template)
		for (IdentifiedTopLevelData materialWrapper : materials) { // Iterate over IdentifiedTopLevelData
			MaterialData material = materialWrapper.unwrapAs(MaterialData.class); // Unwrap for material-specific fields

			for (IdentifiedTopLevelData partTemplateWrapper : partTemplates) { // Iterate over IdentifiedTopLevelData
				PartTemplateData partTemplate = partTemplateWrapper.unwrapAs(PartTemplateData.class); // Unwrap for template-specific fields

				// Check if material is compatible with part template (based on material type tag)
				OpenIdentifier requiredMaterialType = partTemplate.structure().material().type();

				// `materialWrapper` is already IdentifiedTopLevelData and implements Taggable
				if (tagGraph.isTagged(materialWrapper, requiredMaterialType)) {
					// Generate new part ID and name
					String partName = generatePartName(material.name(), partTemplate.name(), partTemplate.naming());
					OpenIdentifier partId = idFactory.of(
							material.name().toLowerCase().replace(' ', '_') + "-" +
									partTemplate.name().toLowerCase().replace(' ', '_')
					);

					// Combine properties.
					// Use tags/attributes/features from the unwrapped DTOs
					List<OpenIdentifier> combinedTags = combineTags(material.tags(), partTemplate.tags());
					List<AttributeData> combinedAttributes = combinePartAttributes(material, partTemplate);
					List<FeatureData> combinedFeatures = combineFeatures(material.features(), partTemplate.features());

					// Construct a new PartTemplateStructureData that points to the CONCRETE material ID
					PartTemplateStructureMaterialData resolvedMaterialStructure = new PartTemplateStructureMaterialData(
							materialWrapper.id(), // Use the concrete material's ID from its wrapper!
							partTemplate.structure().material().count(),
							partTemplate.structure().material().description()
					);
					PartTemplateStructureData resolvedPartStructure = new PartTemplateStructureData(resolvedMaterialStructure);

					// Create the generated Part as a NEW PartTemplateData instance
					PartTemplateData generatedPartRaw = new PartTemplateData(
							partTemplate.type(), // Use PartTemplate's type
							partName,            // Use generated name
							null,                // Includes are resolved
							combinedTags,
							resolvedPartStructure, // Use the resolved structure here
							partTemplate.upgrades(),  // Upgrades remain from template
							partTemplate.naming(),    // Naming remains from template
							combinedAttributes,
							combinedFeatures
					);

					IdentifiedTopLevelData generatedIdentifiedPart = new IdentifiedTopLevelData(partId, generatedPartRaw);
					generatedPartsMap.put(partId, generatedIdentifiedPart);
				}
			}
		}
		generatedItems.putAll(generatedPartsMap);


		// Stage 3.3: Generate Tools (Tool Template + Parts)
		// `allAvailablePartsIdentified` correctly holds IdentifiedTopLevelData
		Map<OpenIdentifier, IdentifiedTopLevelData> allAvailablePartsIdentified = Stream.concat(
						staticParts.stream(), // These are already IdentifiedTopLevelData
						generatedPartsMap.values().stream()
				)
				.collect(Collectors.toMap(TopLevelData::id, d -> d, (existing, replacement) -> existing));

		for (IdentifiedTopLevelData toolTemplateWrapper : toolTemplates) { // Iterate over IdentifiedTopLevelData
			ToolTemplateData toolTemplate = toolTemplateWrapper.unwrapAs(ToolTemplateData.class); // Unwrap for tool-specific fields

			// Find all valid combinations of parts for this tool template
			List<Map<String, IdentifiedTopLevelData>> partCombinations = generateToolPartCombinations(toolTemplate, allAvailablePartsIdentified, tagGraph);

			for (Map<String, IdentifiedTopLevelData> combination : partCombinations) {
				// Generate tool ID and name
				String toolNameParts = combination.values().stream()
						.map(TopLevelData::name) // Get name from the wrapped DTO via TopLevelData interface
						.sorted()
						.collect(Collectors.joining("-"));
				String toolName = toolTemplate.name() + "-" + toolNameParts;

				OpenIdentifier toolId = idFactory.of(
						toolTemplate.name().toLowerCase().replace(' ', '_') + "-" +
								toolNameParts.toLowerCase().replace(' ', '_')
				);

				// Combine properties from tool template and parts
				Set<OpenIdentifier> combinedTagsSet = new HashSet<>();
				if (toolTemplate.tags() != null) combinedTagsSet.addAll(toolTemplate.tags());

				// Access getAttributesMap() from the unwrapped DTO, now that it's correctly implemented
				Map<OpenIdentifier, AttributeData> toolAttributesMap = new HashMap<>(toolTemplate.getAttributesMap());

				Map<OpenIdentifier, FeatureData> toolFeaturesMap = new HashMap<>(toolTemplate.getFeaturesMap());

				for (IdentifiedTopLevelData partWrapper : combination.values()) {
					// Use tags/attributes/features from the partWrapper (IdentifiedTopLevelData)
					if (partWrapper.tags() != null) combinedTagsSet.addAll(Objects.requireNonNull(partWrapper.tags()));
					toolAttributesMap.putAll(partWrapper.getAttributesMap());
					toolFeaturesMap.putAll(partWrapper.getFeaturesMap());
				}
				List<OpenIdentifier> combinedTags = combinedTagsSet.isEmpty() ? null : new ArrayList<>(combinedTagsSet);

				List<AttributeData> combinedToolAttributes = toolAttributesMap.isEmpty() ? null : new ArrayList<>(toolAttributesMap.values());
				List<FeatureData> combinedToolFeatures = toolFeaturesMap.isEmpty() ? null : new ArrayList<>(toolFeaturesMap.values());

				// Construct a new ToolTemplateStructureData that points to the CONCRETE part IDs
				Map<String, ToolTemplateSlotData> resolvedToolSlots = new HashMap<>();
				for (Map.Entry<String, ToolTemplateSlotData> slotEntry : toolTemplate.structure().slots().entrySet()) {
					String slotName = slotEntry.getKey();
					IdentifiedTopLevelData concretePartForSlot = combination.get(slotName);
					if (concretePartForSlot != null) {
						ToolTemplateSlotData resolvedSlot = new ToolTemplateSlotData(
								slotEntry.getValue().type(), // Keep the original slot type
								concretePartForSlot.id()     // Set the concrete component ID from its wrapper
						);
						resolvedToolSlots.put(slotName, resolvedSlot);
					} else {
						// This case should ideally not be reached for required slots if partCombinations are correctly formed.
						// If a slot is required and no concrete part was found for it in the combination,
						// then this combination is invalid or there's a logic error earlier.
						// For now, re-use original slot data (which might have null default)
						resolvedToolSlots.put(slotName, slotEntry.getValue());
					}
				}
				ToolTemplateStructureData resolvedToolStructure = new ToolTemplateStructureData(resolvedToolSlots);

				// Create the generated Tool as a NEW ToolTemplateData instance
				ToolTemplateData generatedToolRaw = new ToolTemplateData(
						toolTemplate.type(), // Type from ToolTemplate
						toolName,            // Generated name
						null,                // Includes are resolved
						combinedTags,
						resolvedToolStructure, // Use the resolved structure here
						toolTemplate.upgrades(),  // Upgrades from template
						combinedToolAttributes,
						combinedToolFeatures
				);

				IdentifiedTopLevelData generatedIdentifiedTool = new IdentifiedTopLevelData(toolId, generatedToolRaw);
				generatedItems.put(toolId, generatedIdentifiedTool);
			}
		}

		return Collections.unmodifiableMap(generatedItems);
	}

	private String generatePartName(String materialName, String partTemplateName, @Nullable PartTemplateNamingData naming) {
		if (naming != null && naming.pattern() != null) {
			String pattern = naming.pattern();
			String name = pattern.replace("{material_name}", capitalize(materialName));
			name = name.replace("{part_template_name}", capitalize(partTemplateName));
			return name;
		}
		return capitalize(materialName) + " " + capitalize(partTemplateName);
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	private List<OpenIdentifier> combineTags(@Nullable List<OpenIdentifier> tags1, @Nullable List<OpenIdentifier> tags2) {
		Set<OpenIdentifier> combined = new HashSet<>();
		if (tags1 != null) combined.addAll(tags1);
		if (tags2 != null) combined.addAll(tags2);
		return combined.isEmpty() ? null : new ArrayList<>(combined);
	}

	private List<AttributeData> combinePartAttributes(MaterialData material, PartTemplateData partTemplate) {
		Map<OpenIdentifier, AttributeData> resultAttributes = new HashMap<>();

		if (material.attributes() != null) {
			material.attributes().stream()
					.filter(attr -> attr.composite() == null)
					.forEach(attr -> resultAttributes.put(attr.id(), attr));
		}

		if (partTemplate.attributes() != null) {
			for (AttributeData templateAttr : partTemplate.attributes()) {
				if (templateAttr.composite() != null) {
					Optional<AttributeData> materialCompositeAttrOpt = material.attributes() != null ?
							material.attributes().stream()
									.filter(matAttr -> matAttr.composite() != null && Objects.equals(templateAttr.composite(), matAttr.composite()))
									.findFirst() : Optional.empty();

					if (materialCompositeAttrOpt.isPresent()) {
						AttributeData materialCompositeAttr = materialCompositeAttrOpt.get();
						ComputationData resolvedComputation = resolveCompositeComputation(materialCompositeAttr.computation(), templateAttr.computation());

						AttributeData resolvedAttr = new AttributeDataImpl(
								templateAttr.id(),
								templateAttr.type(),
								resolvedComputation,
								templateAttr.condition(),
								null
						);
						resultAttributes.put(resolvedAttr.id(), resolvedAttr);
					}
				} else {
					resultAttributes.put(templateAttr.id(), templateAttr);
				}
			}
		}
		return resultAttributes.isEmpty() ? null : new ArrayList<>(resultAttributes.values());
	}

	private List<FeatureData> combineFeatures(@Nullable List<FeatureData> features1, @Nullable List<FeatureData> features2) {
		Map<OpenIdentifier, FeatureData> merged = new HashMap<>();
		if (features1 != null) {
			features1.forEach(f -> merged.put(f.type(), f));
		}
		if (features2 != null) {
			features2.forEach(f -> merged.put(f.type(), f));
		}
		return merged.isEmpty() ? null : new ArrayList<>(merged.values());
	}


	private ComputationData resolveCompositeComputation(ComputationData base, ComputationData modifier) {
		float newValue;
		String newOperator = modifier.operator(); // Keep modifier's operator
		String newOrder = modifier.order(); // Keep modifier's order

		// Ensure order of operations is respected if both are present
		if (modifier.operator().equals(AttributeCodecs.ADDITION_OPERATOR)) {
			newValue = base.value() + modifier.value();
		} else if (modifier.operator().equals(AttributeCodecs.MULTIPLICATION_OPERATOR)) {
			newValue = base.value() * modifier.value();
		} else {
			// Fallback if an unknown operator is encountered, or simply default to addition
			newValue = base.value() + modifier.value();
		}
		return new ComputationData(newValue, newOperator, newOrder);
	}

	/**
	 * Generates all valid combinations of parts for a given tool template.
	 *
	 * @param toolTemplate The tool template.
	 * @param allAvailableParts A map of all parts (static and generated) wrapped in IdentifiedTopLevelData.
	 * @param tagGraph The tag graph for type checking.
	 * @return A list of maps, where each map represents a valid combination of parts for the tool,
	 *         keyed by slot name (e.g., "head", "handle"). Each entry is an IdentifiedTopLevelData.
	 */
	private List<Map<String, IdentifiedTopLevelData>> generateToolPartCombinations(
			ToolTemplateData toolTemplate,
			Map<OpenIdentifier, IdentifiedTopLevelData> allAvailableParts,
			TagGraph tagGraph
	) {
		List<Map<String, IdentifiedTopLevelData>> combinations = new ArrayList<>();
		combinations.add(new HashMap<>()); // Start with an empty combination

		for (Map.Entry<String, ToolTemplateSlotData> slotEntry : toolTemplate.structure().slots().entrySet()) {
			String slotName = slotEntry.getKey();
			ToolTemplateSlotData slotData = slotEntry.getValue();

			List<IdentifiedTopLevelData> potentialPartsForCurrentSlot = new ArrayList<>();

			if (slotData.defaultComponent() != null) {
				// If a default component is specified, use only that one, if it exists and matches type.
				IdentifiedTopLevelData defaultPartWrapper = allAvailableParts.get(slotData.defaultComponent());
				// `defaultPartWrapper` is already IdentifiedTopLevelData and implements Taggable
				if (defaultPartWrapper != null && tagGraph.isTagged(defaultPartWrapper, slotData.type())) {
					potentialPartsForCurrentSlot.add(defaultPartWrapper);
				} else {
					// If a default component is specified but not found or doesn't match type,
					// this slot cannot be filled, so no combinations are possible.
					return Collections.emptyList();
				}
			} else {
				// If no default, find all parts that match the slot's type tag.
				// `allAvailableParts.values()` are `IdentifiedTopLevelData` which implements `Taggable`.
				potentialPartsForCurrentSlot.addAll(tagGraph.findTagged(
						slotData.type(),
						allAvailableParts.values()
				));
			}

			if (potentialPartsForCurrentSlot.isEmpty()) {
				// If any slot cannot be filled, no valid combinations exist for this tool template.
				return Collections.emptyList();
			}

			List<Map<String, IdentifiedTopLevelData>> newCombinations = new ArrayList<>();
			for (Map<String, IdentifiedTopLevelData> existingCombination : combinations) {
				for (IdentifiedTopLevelData newPartForSlot : potentialPartsForCurrentSlot) {
					Map<String, IdentifiedTopLevelData> nextCombination = new HashMap<>(existingCombination);
					nextCombination.put(slotName, newPartForSlot);
					newCombinations.add(nextCombination);
				}
			}
			combinations = newCombinations;
		}

		return combinations;
	}
}
