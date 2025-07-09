package com.sigmundgranaas.forgero.data.generation.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.generation.api.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;

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
			var materialSlotType = template.structure().slots().get("material").type();
			List<NormalizedState.NormalizedMaterial> compatibleMaterials = normalizedState.materials().values().stream()
					.filter(material -> tagGraph.isTagged(material::tags, materialSlotType))
					.toList();

			var shapeSlotType = template.structure().slots().get("shape").type();
			List<NormalizedState.NormalizedShape> compatibleShapes = normalizedState.shapes().values().stream()
					.filter(shape -> tagGraph.isTagged(shape::tags, shapeSlotType))
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

		Map<String, JsonElement> combinedProperties = new HashMap<>();

		var materialProps = Optional.ofNullable(material.properties()).orElse(Collections.emptyMap());
		var shapeProps = Optional.ofNullable(shape.properties()).orElse(Collections.emptyMap());
		var templateProps = Optional.ofNullable(template.properties()).orElse(Collections.emptyMap());

		// Merge general properties. Precedence: template > shape > material
		combinedProperties.putAll(materialProps);
		combinedProperties.putAll(shapeProps);
		combinedProperties.putAll(templateProps);

		// Special handling for attributes to merge them correctly.
		// Precedence is handled by LinkedHashMap: last one seen for a given ID wins.
		Map<OpenIdentifier, AttributeData> mergedAttributesMap = new LinkedHashMap<>();
		Stream.of(materialProps, shapeProps, templateProps)
				.map(p -> p.get("forgero:attributes"))
				.filter(Objects::nonNull)
				.flatMap(json -> AttributeCodecs.ATTRIBUTE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, json).result().orElse(List.of()).stream())
				.forEach(attr -> mergedAttributesMap.put(attr.id(), attr));

		if (!mergedAttributesMap.isEmpty()) {
			JsonArray finalAttributesArray = new JsonArray();
			mergedAttributesMap.values().forEach(attr ->
					AttributeCodecs.ATTRIBUTE_DATA_CODEC.encodeStart(JsonOps.INSTANCE, attr).result().ifPresent(finalAttributesArray::add)
			);
			combinedProperties.put("forgero:attributes", finalAttributesArray);
		}


		// Special handling for features: simply append all lists together.
		List<FeatureData> allFeatures = Stream.of(materialProps, shapeProps, templateProps)
				.map(p -> p.get("forgero:features"))
				.filter(Objects::nonNull)
				.flatMap(json -> FeatureCodecs.FEATURE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, json).result().orElse(List.of()).stream())
				.toList();

		if (!allFeatures.isEmpty()) {
			JsonArray finalFeaturesArray = new JsonArray();
			allFeatures.forEach(feature ->
					FeatureCodecs.FEATURE_DATA_CODEC.encodeStart(JsonOps.INSTANCE, feature).result().ifPresent(finalFeaturesArray::add)
			);
			combinedProperties.put("forgero:features", finalFeaturesArray);
		}


		String idPattern = template.structure().id();
		Map<String, Object> idContext = Map.of("material", material, "shape", shape);
		String resolvedIdPath = idResolver.resolveId(idPattern, idContext);
		OpenIdentifier partId = idFactory.of(resolvedIdPath);

		return new GeneratedState.GeneratedPart(
				partId,
				combinedTags,
				material.id(),
				shape.id(),
				template.upgrades(),
				combinedProperties.isEmpty() ? null : combinedProperties
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

		// Per the architectural plan, properties for equipment come ONLY from the template.
		// Properties from constituent parts are NOT merged into the equipment DTO.
		// They are resolved at runtime by traversing the component tree.
		Map<String, JsonElement> equipmentProperties = template.properties();

		return new GeneratedState.GeneratedEquipment(
				equipmentId,
				template.tags(),
				partCombination,
				template.upgrades(),
				equipmentProperties
		);
	}

	private record PartWrapper(OpenIdentifier id, Set<OpenIdentifier> tags) {
	}

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
			List<PartWrapper> potentialParts;

			if (slotData.defaultComponent() != null) {
				PartWrapper part = allAvailableParts.get(slotData.defaultComponent());
				potentialParts = (part != null && tagGraph.isTagged(part::tags, slotData.type()))
						? List.of(part)
						: Collections.emptyList();
			} else if (slotData.defaultTag() != null) {
				potentialParts = allAvailableParts.values().stream()
						.filter(part -> part.tags() != null)
						.filter(part -> tagGraph.isTagged(part::tags, slotData.type()))
						.filter(part -> tagGraph.isTagged(part::tags, slotData.defaultTag()))
						.toList();
			} else {
				potentialParts = Collections.emptyList();
			}

			if (potentialParts.isEmpty()) {
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
}
