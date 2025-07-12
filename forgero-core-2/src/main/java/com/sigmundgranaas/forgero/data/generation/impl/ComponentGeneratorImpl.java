package com.sigmundgranaas.forgero.data.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.generation.api.ComponentGenerator;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.CreateTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.IdentifierTemplateEntry;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import org.jetbrains.annotations.Nullable;

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
					// Shapes now also need to be checked for tags
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

	private void mergeProperties(Map<String, List<PropertyData>> target, Map<String, List<PropertyData>> source) {
		if (source == null) {
			return;
		}
		source.forEach((key, sourceList) -> {
			List<PropertyData> targetList = target.computeIfAbsent(key, k -> new ArrayList<>());

			if (key.equals("forgero:attributes")) {
				// Special handling for attributes: merge by ID
				Map<OpenIdentifier, AttributeData> mergedAttributes = new LinkedHashMap<>();
				// Add existing attributes from target
				targetList.stream()
						.map(AttributeData.class::cast)
						.forEach(attr -> mergedAttributes.put(attr.id(), attr));
				// Add/overwrite with new attributes from source
				sourceList.stream()
						.map(AttributeData.class::cast)
						.forEach(attr -> mergedAttributes.put(attr.id(), attr));

				targetList.clear();
				targetList.addAll(mergedAttributes.values());
			} else {
				// Default behavior: just append
				targetList.addAll(sourceList);
			}
		});
	}

	private GeneratedState.GeneratedPart createGeneratedPart(
			NormalizedState.NormalizedMaterial material,
			NormalizedState.NormalizedShape shape,
			NormalizedState.NormalizedPartTemplate template,
			IdResolver idResolver
	) {
		Map<String, Object> idContext = Map.of("material", material, "shape", shape);
		String idPattern = template.structure().id();
		String resolvedIdPath = idResolver.resolveId(idPattern, idContext);
		OpenIdentifier partId = idFactory.of(resolvedIdPath);

		Set<OpenIdentifier> combinedTags = Stream.of(material.tags(), shape.tags(), template.tags())
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());

		Map<String, List<PropertyData>> combinedProperties = new HashMap<>();
		mergeProperties(combinedProperties, material.properties());
		mergeProperties(combinedProperties, shape.properties());
		mergeProperties(combinedProperties, template.properties());

		HostData hostData = createHostData(template.host_template(), idContext, partId, "forgero:part_item", idResolver);

		return new GeneratedState.GeneratedPart(
				partId,
				combinedTags,
				material.id(),
				shape.id(),
				hostData,
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
		Map<String, Object> idContext = new HashMap<>(partCombination);

		String idPattern = template.structure().id();
		String resolvedIdPath = idResolver.resolveId(idPattern, idContext);
		OpenIdentifier equipmentId = idFactory.of(resolvedIdPath);

		Map<String, List<PropertyData>> equipmentProperties = template.properties();
		HostData hostData = createHostData(template.host_template(), idContext, equipmentId, "forgero:tool_item", idResolver);


		return new GeneratedState.GeneratedEquipment(
				equipmentId,
				template.tags(),
				partCombination,
				hostData,
				template.upgrades(),
				equipmentProperties
		);
	}

	private HostData createHostData(@Nullable HostTemplateData template, Map<String, Object> context, OpenIdentifier defaultId, String defaultClass, IdResolver idResolver) {
		if (template == null) {
			// Default behavior: create a new item with the component's ID
			return new HostData(null, new CreateData(defaultId, defaultClass, null));
		}

		List<IdentifierEntry> identifiers = null;
		if (template.identifiers() != null) {
			identifiers = template.identifiers().stream()
					.map(entryTemplate -> {
						String resolvedIdStr = idResolver.resolveId(entryTemplate.id(), context);
						return new IdentifierEntry(entryTemplate.type(), idFactory.of(resolvedIdStr));
					})
					.toList();
		}

		CreateData create = null;
		if (template.create() != null) {
			CreateTemplateData createTemplate = template.create();
			String resolvedIdStr = idResolver.resolveId(createTemplate.id(), context);
			// ClassName is not resolved by IdResolver, it's a direct string.
			create = new CreateData(idFactory.of(resolvedIdStr), createTemplate.className(), createTemplate.item_group());
		}

		// Fallback to default creation if template exists but doesn't specify creation.
		if (identifiers == null && create == null) {
			return new HostData(null, new CreateData(defaultId, defaultClass, null));
		}

		return new HostData(identifiers, create);
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
