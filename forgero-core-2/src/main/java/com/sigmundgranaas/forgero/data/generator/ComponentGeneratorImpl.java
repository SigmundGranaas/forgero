package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.v3.dto.*;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

		List<IdentifiedTopLevelData> materials = filterData(normalizedData, MaterialData.class);
		List<IdentifiedTopLevelData> partTemplates = filterData(normalizedData, PartTemplateData.class);
		List<IdentifiedTopLevelData> toolTemplates = filterData(normalizedData, ToolTemplateData.class);
		List<IdentifiedTopLevelData> staticParts = filterData(normalizedData, StaticPartData.class);
		List<IdentifiedTopLevelData> schematics = filterData(normalizedData, SchematicData.class);

		staticParts.forEach(data -> generatedItems.put(data.id(), data));
		schematics.forEach(data -> generatedItems.put(data.id(), data));

		Map<OpenIdentifier, IdentifiedTopLevelData> generatedPartsMap = generateParts(materials, partTemplates, tagGraph);
		generatedItems.putAll(generatedPartsMap);

		Map<OpenIdentifier, IdentifiedTopLevelData> allAvailableParts = Stream.concat(
						staticParts.stream(),
						generatedPartsMap.values().stream()
				)
				.collect(Collectors.toMap(TopLevelData::id, d -> d));

		generatedItems.putAll(generateTools(toolTemplates, allAvailableParts, tagGraph));

		return Collections.unmodifiableMap(generatedItems);
	}

	private Map<OpenIdentifier, IdentifiedTopLevelData> generateParts(List<IdentifiedTopLevelData> materials, List<IdentifiedTopLevelData> partTemplates, TagGraph tagGraph) {
		Map<OpenIdentifier, IdentifiedTopLevelData> generatedPartsMap = new HashMap<>();
		for (IdentifiedTopLevelData materialWrapper : materials) {
			MaterialData material = materialWrapper.unwrapAs(MaterialData.class);
			for (IdentifiedTopLevelData partTemplateWrapper : partTemplates) {
				PartTemplateData partTemplate = partTemplateWrapper.unwrapAs(PartTemplateData.class);

				if (isMaterialCompatible(materialWrapper, partTemplate, tagGraph)) {
					GeneratedPartData generatedPart = createGeneratedPart(material, partTemplate, materialWrapper.id());
					OpenIdentifier partId = idFactory.of(
							material.name().toLowerCase().replace(' ', '_') + "-" +
									partTemplate.name().toLowerCase().replace(' ', '_')
					);
					generatedPartsMap.put(partId, new IdentifiedTopLevelData(partId, generatedPart));
				}
			}
		}
		return generatedPartsMap;
	}

	private Map<OpenIdentifier, IdentifiedTopLevelData> generateTools(List<IdentifiedTopLevelData> toolTemplates, Map<OpenIdentifier, IdentifiedTopLevelData> allAvailableParts, TagGraph tagGraph) {
		Map<OpenIdentifier, IdentifiedTopLevelData> generatedToolsMap = new HashMap<>();
		for (IdentifiedTopLevelData toolTemplateWrapper : toolTemplates) {
			ToolTemplateData toolTemplate = toolTemplateWrapper.unwrapAs(ToolTemplateData.class);
			List<Map<String, IdentifiedTopLevelData>> partCombinations = findPartCombinations(toolTemplate, allAvailableParts, tagGraph);

			for (Map<String, IdentifiedTopLevelData> combination : partCombinations) {
				GeneratedEquipmentData generatedTool = createGeneratedTool(toolTemplate, combination);

				String toolNameParts = combination.values().stream()
						.map(TopLevelData::name)
						.sorted()
						.collect(Collectors.joining("-"));

				String toolIdString = toolTemplate.name().toLowerCase().replace(' ', '_') + "-" +
						toolNameParts.toLowerCase().replace(' ', '_');

				OpenIdentifier toolId = idFactory.of(toolIdString);
				generatedToolsMap.put(toolId, new IdentifiedTopLevelData(toolId, generatedTool));
			}
		}
		return generatedToolsMap;
	}

	private GeneratedPartData createGeneratedPart(MaterialData material, PartTemplateData template, OpenIdentifier materialId) {
		String partName = generatePartName(material.name(), template.name(), template.naming());
		PartTemplateStructureData resolvedPartStructure = new PartTemplateStructureData(
				new PartTemplateStructureMaterialData(materialId, template.structure().material().count(), template.structure().material().description())
		);

		List<AttributeData> combinedAttributes = Stream.concat(
				Optional.ofNullable(material.attributes()).stream().flatMap(List::stream),
				Optional.ofNullable(template.attributes()).stream().flatMap(List::stream)
		).toList();

		List<OpenIdentifier> combinedTags = Stream.concat(
				Optional.ofNullable(material.tags()).stream().flatMap(List::stream),
				Optional.ofNullable(template.tags()).stream().flatMap(List::stream)
		).distinct().toList();

		return new GeneratedPartData(
				template.type(), partName, combinedTags, resolvedPartStructure,
				template.upgrades(), combinedAttributes.isEmpty() ? null : combinedAttributes, template.features()
		);
	}

	private GeneratedEquipmentData createGeneratedTool(ToolTemplateData template, Map<String, IdentifiedTopLevelData> partCombination) {
		String toolNameParts = partCombination.values().stream()
				.map(TopLevelData::name)
				.sorted()
				.collect(Collectors.joining("-"));
		String toolName = template.name() + "-" + toolNameParts;

		Map<String, ToolTemplateSlotData> resolvedSlots = new HashMap<>();
		for (Map.Entry<String, ToolTemplateSlotData> slotEntry : template.structure().slots().entrySet()) {
			resolvedSlots.put(slotEntry.getKey(), new ToolTemplateSlotData(slotEntry.getValue().type(), partCombination.get(slotEntry.getKey()).id()));
		}
		ToolTemplateStructureData resolvedStructure = new ToolTemplateStructureData(resolvedSlots);

		// A generated tool ONLY gets properties from its template.
		// Child properties are resolved at runtime.
		return new GeneratedEquipmentData(
				template.type(), toolName, template.tags(), resolvedStructure,
				template.upgrades(), template.attributes(), template.features()
		);
	}

	// Helper methods (unchanged from previous correct version)
	private <T> List<IdentifiedTopLevelData> filterData(Map<OpenIdentifier, TopLevelData> data, Class<T> type) {
		return data.values().stream()
				.filter(d -> type.isInstance(d.unwrapAs(Object.class)))
				.map(IdentifiedTopLevelData.class::cast)
				.toList();
	}

	private boolean isMaterialCompatible(IdentifiedTopLevelData materialWrapper, PartTemplateData partTemplate, TagGraph tagGraph) {
		OpenIdentifier requiredMaterialType = partTemplate.structure().material().type();
		return tagGraph.isTagged(materialWrapper, requiredMaterialType);
	}

	private List<Map<String, IdentifiedTopLevelData>> findPartCombinations(ToolTemplateData toolTemplate, Map<OpenIdentifier, IdentifiedTopLevelData> allAvailableParts, TagGraph tagGraph) {
		List<Map<String, IdentifiedTopLevelData>> combinations = new ArrayList<>();
		combinations.add(new HashMap<>());
		for (Map.Entry<String, ToolTemplateSlotData> slotEntry : toolTemplate.structure().slots().entrySet()) {
			List<IdentifiedTopLevelData> potentialParts = findPotentialParts(slotEntry.getValue(), allAvailableParts, tagGraph);
			if (potentialParts.isEmpty()) return Collections.emptyList();
			combinations = expandCombinations(combinations, slotEntry.getKey(), potentialParts);
		}
		return combinations;
	}

	private List<IdentifiedTopLevelData> findPotentialParts(ToolTemplateSlotData slotData, Map<OpenIdentifier, IdentifiedTopLevelData> allAvailableParts, TagGraph tagGraph) {
		if (slotData.defaultComponent() != null) {
			IdentifiedTopLevelData defaultPart = allAvailableParts.get(slotData.defaultComponent());
			return (defaultPart != null && tagGraph.isTagged(defaultPart, slotData.type())) ? List.of(defaultPart) : Collections.emptyList();
		}
		return tagGraph.findTagged(slotData.type(), allAvailableParts.values());
	}

	private List<Map<String, IdentifiedTopLevelData>> expandCombinations(List<Map<String, IdentifiedTopLevelData>> currentCombinations, String slotName, List<IdentifiedTopLevelData> newParts) {
		List<Map<String, IdentifiedTopLevelData>> newCombinations = new ArrayList<>();
		for (Map<String, IdentifiedTopLevelData> existingCombination : currentCombinations) {
			for (IdentifiedTopLevelData newPart : newParts) {
				Map<String, IdentifiedTopLevelData> nextCombination = new HashMap<>(existingCombination);
				nextCombination.put(slotName, newPart);
				newCombinations.add(nextCombination);
			}
		}
		return newCombinations;
	}

	private String generatePartName(String materialName, String partTemplateName, @Nullable PartTemplateNamingData naming) {
		if (naming != null && naming.pattern() != null) {
			String pattern = naming.pattern();
			return pattern.replace("{material_name}", capitalize(materialName))
					.replace("{part_template_name}", capitalize(partTemplateName));
		}
		return capitalize(materialName) + " " + capitalize(partTemplateName);
	}

	private String capitalize(String str) {
		return (str == null || str.isEmpty()) ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
