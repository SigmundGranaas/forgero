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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentGeneratorImpl implements ComponentGenerator {

	private final IdentifierFactory idFactory;

	public ComponentGeneratorImpl(IdentifierFactory idFactory) {
		this.idFactory = idFactory;
	}

	@Override
	public GeneratedState generate(NormalizedState normalizedState, TagGraph tagGraph) {
		// First, generate parts. The IdResolver for tools needs these generated parts.
		// Create a mutable map for generated parts so IdResolver can be built with it.
		Map<OpenIdentifier, GeneratedState.GeneratedPart> generatedPartsMutable = new HashMap<>();
		IdResolver idResolver = new IdResolver(normalizedState, generatedPartsMutable); // IdResolver is instantiated once per generation cycle

		for (NormalizedState.NormalizedPartTemplate template : normalizedState.partTemplates().values()) {
			List<NormalizedState.NormalizedMaterial> compatibleMaterials = normalizedState.materials().values().stream()
					.filter(material -> tagGraph.isTagged(material::tags, template.structure().slots().get("material").type()))
					.toList();

			List<NormalizedState.NormalizedShape> compatibleShapes = normalizedState.shapes().values().stream()
					.filter(shape -> tagGraph.isTagged(shape::tags, template.structure().slots().get("shape").type()))
					.toList();

			for (NormalizedState.NormalizedMaterial material : compatibleMaterials) {
				for (NormalizedState.NormalizedShape shape : compatibleShapes) {
					GeneratedState.GeneratedPart generatedPart = createGeneratedPart(material, shape, template, idResolver);
					generatedPartsMutable.put(generatedPart.id(), generatedPart);
				}
			}
		}

		Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generatedEquipment = generateEquipment(
				normalizedState.equipmentTemplates(),
				Stream.concat(
						normalizedState.staticParts().values().stream().map(sp -> new PartWrapper(sp.id(), sp.tags())),
						generatedPartsMutable.values().stream().map(gp -> new PartWrapper(gp.id(), gp.tags()))
				).collect(Collectors.toMap(PartWrapper::id, Function.identity())),
				tagGraph,
				idResolver
		);

		return new GeneratedState(Map.copyOf(generatedPartsMutable), Map.copyOf(generatedEquipment));
	}

	private GeneratedState.GeneratedPart createGeneratedPart(
			NormalizedState.NormalizedMaterial material,
			NormalizedState.NormalizedShape shape,
			NormalizedState.NormalizedPartTemplate template,
			IdResolver idResolver
	) {
		// Display name generation (fixed pattern as naming() is removed)
		String partName = String.format("%s %s %s", capitalize(material.name()), capitalize(shape.name()), capitalize(template.name()));


		Set<OpenIdentifier> combinedTags = Stream.of(material.tags(), shape.tags(), template.tags())
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());

		// Merge attributes by ID, with shape attributes overriding material attributes
		Map<OpenIdentifier, AttributeData> mergedAttributesMap = new HashMap<>();
		Optional.ofNullable(material.attributes()).orElse(Collections.emptyList()).forEach(attr -> mergedAttributesMap.put(attr.id(), attr));
		Optional.ofNullable(shape.attributes()).orElse(Collections.emptyList()).forEach(attr -> mergedAttributesMap.put(attr.id(), attr));
		List<AttributeData> combinedAttributes = mergedAttributesMap.isEmpty() ? null : new ArrayList<>(mergedAttributesMap.values());

		// Merge features by type, with shape features overriding material features
		Map<OpenIdentifier, FeatureData> mergedFeaturesMap = new HashMap<>();
		Optional.ofNullable(material.features()).orElse(Collections.emptyList()).forEach(feat -> mergedFeaturesMap.put(feat.type(), feat));
		Optional.ofNullable(shape.features()).orElse(Collections.emptyList()).forEach(feat -> mergedFeaturesMap.put(feat.type(), feat));
		List<FeatureData> combinedFeatures = mergedFeaturesMap.isEmpty() ? null : new ArrayList<>(mergedFeaturesMap.values());

		// Generate ID using the template from structure.id and IdResolver
		String idPattern = template.structure().id();
		Map<String, Object> idContext = new HashMap<>(); // Use mutable map for context
		idContext.put("material", material); // Pass DTOs directly
		idContext.put("shape", shape);
		idContext.put("part_template", template); // Pass template DTO for potential future use in pattern


		String resolvedIdPath = idResolver.resolveId(idPattern, idContext); // Use IdResolver's resolveId
		OpenIdentifier partId = idFactory.of(resolvedIdPath);

		return new GeneratedState.GeneratedPart(
				partId,
				combinedTags,
				material.id(),
				shape.id(),
				template.upgrades(),
				combinedAttributes,
				combinedFeatures
		);
	}

	private Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generateEquipment(
			Map<OpenIdentifier, NormalizedState.NormalizedEquipmentTemplate> equipmentTemplates,
			Map<OpenIdentifier, PartWrapper> allAvailableParts,
			TagGraph tagGraph,
			IdResolver idResolver // Receive IdResolver
	) {
		Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generatedEquipmentMap = new HashMap<>();

		for (NormalizedState.NormalizedEquipmentTemplate template : equipmentTemplates.values()) {
			List<Map<String, OpenIdentifier>> partCombinations = findEquipmentPartCombinations(template, allAvailableParts, tagGraph);

			for (Map<String, OpenIdentifier> combination : partCombinations) {
				GeneratedState.GeneratedEquipment generatedEquipment = createGeneratedEquipment(template, combination, idResolver, allAvailableParts);
				generatedEquipmentMap.put(generatedEquipment.id(), generatedEquipment);
			}
		}
		return generatedEquipmentMap;
	}

	private GeneratedState.GeneratedEquipment createGeneratedEquipment(
			NormalizedState.NormalizedEquipmentTemplate template,
			Map<String, OpenIdentifier> partCombination, // slotName -> concretePartId
			IdResolver idResolver, // Receive IdResolver
			Map<OpenIdentifier, PartWrapper> allAvailableParts // For display name resolution
	) {
		// Generate ID using the template from structure.id and IdResolver
		String idPattern = template.structure().id();
		// The IdResolver will handle looking up the actual DTOs from the OpenIdentifiers in 'partCombination'
		String resolvedIdPath = idResolver.resolveId(idPattern, new HashMap<>(partCombination)); // Pass combination as context to IdResolver
		OpenIdentifier equipmentId = idFactory.of(resolvedIdPath);

		return new GeneratedState.GeneratedEquipment(
				equipmentId,
				template.tags(),
				partCombination,
				template.upgrades()
		);
	}


	/**
	 * Helper for generating equipment, wraps a component's ID, tags, and name
	 * to simplify finding compatible parts without needing the full DTO.
	 */
	private record PartWrapper(OpenIdentifier id, Set<OpenIdentifier> tags) {
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

		for (Map.Entry<String, EquipmentTemplateSlotData> slotEntry : template.structure().slots().entrySet()) { // Access slots from the new structure DTO
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
