package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.data.definition.GeneratedState;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateSlotData;

import java.util.*;
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
		Map<OpenIdentifier, GeneratedState.GeneratedPart> generatedPartsMutable = new HashMap<>();
		IdResolver idResolver = new IdResolver(normalizedState, generatedPartsMutable);

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

		Map<OpenIdentifier, PartWrapper> allAvailableParts = Stream.concat(
				normalizedState.staticParts().values().stream().map(sp -> new PartWrapper(sp.id(), sp.tags())),
				generatedPartsMutable.values().stream().map(gp -> new PartWrapper(gp.id(), gp.tags()))
		).collect(Collectors.toMap(PartWrapper::id, Function.identity()));


		Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generatedEquipment = generateEquipment(
				normalizedState.equipmentTemplates(),
				allAvailableParts,
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
		Set<OpenIdentifier> combinedTags = Stream.of(material.tags(), shape.tags(), template.tags())
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());

		Map<OpenIdentifier, AttributeData> mergedAttributesMap = new HashMap<>();
		Optional.ofNullable(material.attributes()).orElse(Collections.emptyList()).forEach(attr -> mergedAttributesMap.put(attr.id(), attr));
		Optional.ofNullable(shape.attributes()).orElse(Collections.emptyList()).forEach(attr -> mergedAttributesMap.put(attr.id(), attr));
		List<AttributeData> combinedAttributes = mergedAttributesMap.isEmpty() ? null : new ArrayList<>(mergedAttributesMap.values());

		Map<OpenIdentifier, FeatureData> mergedFeaturesMap = new HashMap<>();
		Optional.ofNullable(material.features()).orElse(Collections.emptyList()).forEach(feat -> mergedFeaturesMap.put(feat.type(), feat));
		Optional.ofNullable(shape.features()).orElse(Collections.emptyList()).forEach(feat -> mergedFeaturesMap.put(feat.type(), feat));
		List<FeatureData> combinedFeatures = mergedFeaturesMap.isEmpty() ? null : new ArrayList<>(mergedFeaturesMap.values());

		String idPattern = template.structure().id();
		Map<String, Object> idContext = new HashMap<>();
		idContext.put("material", material);
		idContext.put("shape", shape);
		idContext.put("part_template", template);

		String resolvedIdPath = idResolver.resolveId(idPattern, idContext);
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
			IdResolver idResolver
	) {
		Map<OpenIdentifier, GeneratedState.GeneratedEquipment> generatedEquipmentMap = new HashMap<>();
		for (NormalizedState.NormalizedEquipmentTemplate template : equipmentTemplates.values()) {
			List<Map<String, OpenIdentifier>> partCombinations = findEquipmentPartCombinations(template, allAvailableParts, tagGraph);
			for (Map<String, OpenIdentifier> combination : partCombinations) {
				GeneratedState.GeneratedEquipment generatedEquipment = createGeneratedEquipment(template, combination, idResolver);
				generatedEquipmentMap.put(generatedEquipment.id(), generatedEquipment);
			}
		}
		return generatedEquipmentMap;
	}

	private GeneratedState.GeneratedEquipment createGeneratedEquipment(
			NormalizedState.NormalizedEquipmentTemplate template,
			Map<String, OpenIdentifier> partCombination,
			IdResolver idResolver
	) {
		String idPattern = template.structure().id();
		String resolvedIdPath = idResolver.resolveId(idPattern, new HashMap<>(partCombination));
		OpenIdentifier equipmentId = idFactory.of(resolvedIdPath);
		return new GeneratedState.GeneratedEquipment(equipmentId, template.tags(), partCombination, template.upgrades());
	}

	private record PartWrapper(OpenIdentifier id, Set<OpenIdentifier> tags) {}

	private List<Map<String, OpenIdentifier>> findEquipmentPartCombinations(
			NormalizedState.NormalizedEquipmentTemplate template,
			Map<OpenIdentifier, PartWrapper> allAvailableParts,
			TagGraph tagGraph
	) {
		List<Map<String, OpenIdentifier>> combinations = new ArrayList<>();
		combinations.add(new HashMap<>());

		// Use sorted list of slot names to ensure deterministic order
		for (String slotName : template.structure().slots().keySet().stream().sorted().toList()) {
			EquipmentTemplateSlotData slotData = template.structure().slots().get(slotName);
			List<PartWrapper> potentialParts = new ArrayList<>();

			if (slotData.defaultComponent() != null) {
				// Case 1: A single, concrete component ID is specified
				PartWrapper part = allAvailableParts.get(slotData.defaultComponent());
				if (part != null && tagGraph.isTagged(part::tags, slotData.type())) {
					potentialParts.add(part);
				}
			} else if (slotData.defaultTag() != null) {
				// Case 2: A tag is specified, creating a pool of default parts
				potentialParts = allAvailableParts.values().stream()
						.filter(part -> part.tags() != null)
						.filter(part -> tagGraph.isTagged(part::tags, slotData.type()))
						.filter(part -> tagGraph.isTagged(part::tags, slotData.defaultTag()))
						.toList();
			}

			if (potentialParts.isEmpty()) {
				// If any slot cannot be filled with a default part, no default tools can be generated for this template.
				return Collections.emptyList();
			}

			combinations = expandCombinations(combinations, slotName, potentialParts);
		}
		return combinations;
	}

	private List<Map<String, OpenIdentifier>> expandCombinations(
			List<Map<String, OpenIdentifier>> currentCombinations,
			String slotName,
			List<PartWrapper> newParts
	) {
		List<Map<String, OpenIdentifier>> newCombinations = new ArrayList<>();
		for (Map<String, OpenIdentifier> existingCombination : currentCombinations) {
			for (PartWrapper newPart : newParts) {
				Map<String, OpenIdentifier> nextCombination = new HashMap<>(existingCombination);
				nextCombination.put(slotName, newPart.id());
				newCombinations.add(nextCombination);
			}
		}
		return newCombinations;
	}

	private String capitalize(String str) {
		return (str == null || str.isEmpty()) ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
