package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.data.definition.GeneratedState;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateSlotData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
	public GeneratedState generate(NormalizedState normalizedState, TagGraph tagGraph) {
		Map<OpenIdentifier, GeneratedState.GeneratedPart> generatedParts = generateParts(
				normalizedState.materials(),
				normalizedState.shapes(),
				normalizedState.partTemplates(),
				tagGraph
		);

		Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generatedEquipment = generateEquipment(
				normalizedState.equipmentTemplates(),
				Stream.concat(
						normalizedState.staticParts().values().stream().map(sp -> new PartWrapper(sp.id(), sp.tags(), sp.name())),
						generatedParts.values().stream().map(gp -> new PartWrapper(gp.id(), gp.tags(), gp.name()))
				).collect(Collectors.toMap(PartWrapper::id, p -> p)),
				tagGraph
		);

		return new GeneratedState(Map.copyOf(generatedParts), Map.copyOf(generatedEquipment));
	}

	private Map<OpenIdentifier, GeneratedState.GeneratedPart> generateParts(
			Map<OpenIdentifier, NormalizedState.NormalizedMaterial> materials,
			Map<OpenIdentifier, NormalizedState.NormalizedShape> shapes,
			Map<OpenIdentifier, NormalizedState.NormalizedPartTemplate> partTemplates,
			TagGraph tagGraph
	) {
		Map<OpenIdentifier, GeneratedState.GeneratedPart> generatedPartsMap = new HashMap<>();

		for (NormalizedState.NormalizedPartTemplate template : partTemplates.values()) {
			List<NormalizedState.NormalizedMaterial> compatibleMaterials = materials.values().stream()
					.filter(material -> tagGraph.isTagged(() -> material.tags(), template.materialType()))
					.toList();

			List<NormalizedState.NormalizedShape> compatibleShapes = shapes.values().stream()
					.filter(shape -> tagGraph.isTagged(() -> shape.tags(), template.shapeType()))
					.toList();

			for (NormalizedState.NormalizedMaterial material : compatibleMaterials) {
				for (NormalizedState.NormalizedShape shape : compatibleShapes) {
					GeneratedState.GeneratedPart generatedPart = createGeneratedPart(material, shape, template);
					// The ID is now derived from the combined name, which includes material, shape, and template parts.
					// This means the ID factory will canonicalize a more complex string.
					OpenIdentifier partId = idFactory.of(generatedPart.name().toLowerCase().replace(' ', '_'));
					generatedPartsMap.put(partId, generatedPart);
				}
			}
		}
		return generatedPartsMap;
	}

	private GeneratedState.GeneratedPart createGeneratedPart(
			NormalizedState.NormalizedMaterial material,
			NormalizedState.NormalizedShape shape,
			NormalizedState.NormalizedPartTemplate template
	) {
		// Example naming pattern (from architecture): "{material_name} {shape_name} {part_template_name}"
		String partName = String.format("%s %s %s", capitalize(material.name()), capitalize(shape.name()), capitalize(template.name()));

		Set<OpenIdentifier> combinedTags = Stream.of(material.tags(), shape.tags(), template.tags())
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());

		// Merge attributes by ID, with shape attributes overriding material attributes
		Map<OpenIdentifier, AttributeData> mergedAttributesMap = new HashMap<>();
		Optional.ofNullable(material.attributes()).orElse(Collections.emptyList()).forEach(attr -> mergedAttributesMap.put(attr.id(), attr));
		Optional.ofNullable(shape.attributes()).orElse(Collections.emptyList()).forEach(attr -> mergedAttributesMap.put(attr.id(), attr));
		// If template attributes are to be merged, they would come from NormalizedPartTemplate attributes.
		// As per original design, PartTemplateData doesn't carry attributes itself that are directly merged into GeneratedPart.
		// They are mainly defined for material/shape.

		List<AttributeData> combinedAttributes = mergedAttributesMap.isEmpty() ? null : new ArrayList<>(mergedAttributesMap.values());

		// Merge features by type, with shape features overriding material features
		Map<OpenIdentifier, FeatureData> mergedFeaturesMap = new HashMap<>();
		Optional.ofNullable(material.features()).orElse(Collections.emptyList()).forEach(feat -> mergedFeaturesMap.put(feat.type(), feat));
		Optional.ofNullable(shape.features()).orElse(Collections.emptyList()).forEach(feat -> mergedFeaturesMap.put(feat.type(), feat));
		List<FeatureData> combinedFeatures = mergedFeaturesMap.isEmpty() ? null : new ArrayList<>(mergedFeaturesMap.values());


		return new GeneratedState.GeneratedPart(
				idFactory.of(partName.toLowerCase().replace(' ', '_')), // Canonical ID for the generated part
				partName,
				combinedTags,
				material.id(), // Concrete material ID
				shape.id(),    // Concrete shape ID
				template.upgrades(), // Upgrades come from the part template
				combinedAttributes,
				combinedFeatures
		);
	}

	private Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generateEquipment(
			Map<OpenIdentifier, NormalizedState.NormalizedEquipmentTemplate> equipmentTemplates,
			Map<OpenIdentifier, PartWrapper> allAvailableParts,
			TagGraph tagGraph
	) {
		Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generatedEquipmentMap = new HashMap<>();

		for (NormalizedState.NormalizedEquipmentTemplate template : equipmentTemplates.values()) {
			List<Map<String, OpenIdentifier>> partCombinations = findEquipmentPartCombinations(template, allAvailableParts, tagGraph);

			for (Map<String, OpenIdentifier> combination : partCombinations) {
				GeneratedState.GeneratedEquipment generatedEquipment = createGeneratedEquipment(template, combination, allAvailableParts);

				String equipmentIdString = template.name().toLowerCase().replace(' ', '_') + "-" +
						combination.values().stream()
								.sorted(Comparator.comparing(OpenIdentifier::path)) // Consistent sorting for ID generation
								.map(OpenIdentifier::path)
								.collect(Collectors.joining("-"));

				OpenIdentifier equipmentId = idFactory.of(equipmentIdString);
				generatedEquipmentMap.put(equipmentId, generatedEquipment);
			}
		}
		return generatedEquipmentMap;
	}

	private GeneratedState.GeneratedEquipment createGeneratedEquipment(
			NormalizedState.NormalizedEquipmentTemplate template,
			Map<String, OpenIdentifier> partCombination,
			Map<OpenIdentifier, PartWrapper> allAvailableParts
	) {
		// Name generation for equipment: Template name + sorted part names
		String equipmentNameParts = partCombination.entrySet().stream()
				.sorted(Map.Entry.comparingByKey()) // Sort by slot name for consistent ordering
				.map(entry -> allAvailableParts.get(entry.getValue()).name())
				.collect(Collectors.joining("-"));
		String equipmentName = template.name() + "-" + equipmentNameParts;

		// The structure in GeneratedEquipment should contain the concrete part IDs
		return new GeneratedState.GeneratedEquipment(
				idFactory.of(equipmentName.toLowerCase().replace(' ', '_')), // Canonical ID
				equipmentName,
				template.tags(), // Equipment ONLY gets tags from its template. Part tags are not merged at this level.
				partCombination, // This already contains the concrete IDs (slotName -> concretePartId)
				template.upgrades() // Upgrades from the equipment template
		);
	}


	/**
	 * Helper for generating equipment, wraps a component's ID, tags, and name
	 * to simplify finding compatible parts without needing the full DTO.
	 */
	private record PartWrapper(OpenIdentifier id, Set<OpenIdentifier> tags, String name) {
	}

	/**
	 * Finds all valid combinations of parts for an equipment template.
	 *
	 * @param template          The equipment template.
	 * @param allAvailableParts A map of all normalized and generated parts available.
	 * @param tagGraph          The tag graph for compatibility checks.
	 * @return A list of maps, where each map represents a valid combination of slotName -> concretePartId.
	 */
	private List<Map<String, OpenIdentifier>> findEquipmentPartCombinations(
			NormalizedState.NormalizedEquipmentTemplate template,
			Map<OpenIdentifier, PartWrapper> allAvailableParts,
			TagGraph tagGraph
	) {
		List<Map<String, OpenIdentifier>> combinations = new ArrayList<>();
		combinations.add(new HashMap<>()); // Start with an empty combination

		for (Map.Entry<String, EquipmentTemplateSlotData> slotEntry : template.structure().entrySet()) {
			String slotName = slotEntry.getKey();
			EquipmentTemplateSlotData slotData = slotEntry.getValue();

			List<PartWrapper> potentialParts = new ArrayList<>();

			if (slotData.defaultComponent() != null) {
				// If a default is specified, try to use it
				PartWrapper defaultPart = allAvailableParts.get(slotData.defaultComponent());
				if (defaultPart != null && tagGraph.isTagged(() -> defaultPart.tags(), slotData.type())) {
					potentialParts.add(defaultPart);
				} else {
					// Default component is invalid or missing, no combinations possible for this template
					return Collections.emptyList();
				}
			} else {
				// If no default, find all parts matching the required slot type tag
				potentialParts = allAvailableParts.values().stream()
						.filter(part -> tagGraph.isTagged(() -> part.tags(), slotData.type()))
						.toList();
				if (potentialParts.isEmpty()) {
					// No compatible parts found for this combinatorial slot, no combinations possible
					return Collections.emptyList();
				}
			}
			combinations = expandCombinations(combinations, slotName, potentialParts);
		}
		return combinations;
	}

	/**
	 * Expands the current list of combinations by adding new parts for a specific slot.
	 */
	private List<Map<String, OpenIdentifier>> expandCombinations(
			List<Map<String, OpenIdentifier>> currentCombinations,
			String slotName,
			List<PartWrapper> newParts
	) {
		List<Map<String, OpenIdentifier>> newCombinations = new ArrayList<>();
		for (Map<String, OpenIdentifier> existingCombination : currentCombinations) {
			for (PartWrapper newPart : newParts) {
				Map<String, OpenIdentifier> nextCombination = new HashMap<>(existingCombination);
				nextCombination.put(slotName, newPart.id()); // Store the concrete ID
				newCombinations.add(nextCombination);
			}
		}
		return newCombinations;
	}


	// Utility for capitalization
	private String capitalize(String str) {
		return (str == null || str.isEmpty()) ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
