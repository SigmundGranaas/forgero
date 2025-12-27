package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofSlot;
import com.sigmundgranaas.forgero.cof.dto.CofStructure;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.*;
import com.sigmundgranaas.forgero.data.pipeline.util.IdTemplateResolver;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates new CofComponents from templates by combining existing components.
 * This is the heart of the combinatorial item generation system.
 */
public class TemplateGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateGenerator.class);
	private final IdentifierFactory idFactory;
	private final TagResolver tagResolver;
	private final PropertyMerger propertyMerger;
	private final IdTemplateResolver idTemplateResolver;
	private final Map<OpenIdentifier, CofComponent> staticComponents;
	private final Map<OpenIdentifier, RawDefinition> rawDefinitions;
	private final List<CofComponent> generatedComponents = new ArrayList<>();
	private final Map<OpenIdentifier, HostData> generatedHostData = new HashMap<>();

	public record TemplateResult(List<CofComponent> components, Map<OpenIdentifier, HostData> hostData) {
	}

	public TemplateGenerator(IdentifierFactory idFactory, TagResolver tagResolver, PropertyMerger propertyMerger, IdTemplateResolver idTemplateResolver, Map<OpenIdentifier, CofComponent> staticComponents, Map<OpenIdentifier, RawDefinition> rawDefinitions) {
		this.idFactory = idFactory;
		this.tagResolver = tagResolver;
		this.propertyMerger = propertyMerger;
		this.idTemplateResolver = idTemplateResolver;
		this.staticComponents = staticComponents;
		this.rawDefinitions = rawDefinitions;
	}

	public TemplateResult generate() {
		// Generate parts from part templates
		rawDefinitions.values().stream()
				.filter(def -> def.data() instanceof PartTemplateData)
				.forEach(this::generatePartsFromTemplate);

		// Generate equipment from equipment templates
		// We need a map of all available parts for this step (static parts + newly generated parts)
		Map<OpenIdentifier, CofComponent> allParts = new HashMap<>(staticComponents);
		generatedComponents.forEach(comp -> allParts.put(comp.id(), comp));

		rawDefinitions.values().stream()
				.filter(def -> def.data() instanceof EquipmentTemplateData)
				.forEach(def -> generateEquipmentFromTemplate(def, allParts));

		return new TemplateResult(generatedComponents, generatedHostData);
	}

	private void generatePartsFromTemplate(RawDefinition templateDef) {
		PartTemplateData template = (PartTemplateData) templateDef.data();
		List<Map<String, CofComponent>> combinations = findCombinationsForPart(template.structure().slots(), template.generation());

		for (Map<String, CofComponent> combination : combinations) {
			CofComponent generatedPart = generatePart(templateDef, combination);
			generatedComponents.add(generatedPart);

			if (template.host_template() != null) {
				generateHostData(template.host_template(), generatedPart.id(), combination)
						.ifPresent(hostData -> generatedHostData.put(generatedPart.id(), hostData));
			}
		}
	}

	private void generateEquipmentFromTemplate(RawDefinition templateDef, Map<OpenIdentifier, CofComponent> availableParts) {
		EquipmentTemplateData template = (EquipmentTemplateData) templateDef.data();
		List<Map<String, CofComponent>> combinations = findCombinationsForEquipment(template.structure().slots(), availableParts);

		for (Map<String, CofComponent> combination : combinations) {
			CofComponent generatedEquipment = generateEquipment(templateDef, combination);
			generatedComponents.add(generatedEquipment);

			if (template.host_template() != null) {
				generateHostData(template.host_template(), generatedEquipment.id(), combination)
						.ifPresent(hostData -> generatedHostData.put(generatedEquipment.id(), hostData));
			}
		}
	}

	private CofComponent generatePart(RawDefinition templateDef, Map<String, CofComponent> combination) {
		PartTemplateData template = (PartTemplateData) templateDef.data();

		String idTemplate = Objects.requireNonNullElse(template.structure().id(), "{material.name}-{shape.name}");
		OpenIdentifier newId = OpenIdentifier.parse(idTemplateResolver.resolve(idTemplate, combination));

		List<DefinitionData> dtoList = new ArrayList<>();
		dtoList.add(template);
		combination.values().forEach(comp -> dtoList.add(rawDefinitions.get(comp.id()).data()));
		PropertyMerger.MergedResult merged = propertyMerger.merge(dtoList);

		Map<OpenIdentifier, CofSlot> newSlots = combination.entrySet().stream()
				.collect(Collectors.toMap(
						entry -> idFactory.of(entry.getKey()),
						entry -> {
							CofComponent content = entry.getValue();
							PartTemplateStructureSlotData slotInfo = template.structure().slots().get(entry.getKey());
							return new CofSlot(idFactory.of(entry.getKey()), slotInfo.type(), slotInfo.description(), content, null);
						}
				));
		CofStructure newStructure = new CofStructure(newSlots);

		OpenIdentifier componentType;
		CofUpgrades upgrades = null;
		if (template.upgrades() != null && !template.upgrades().isEmpty()) {
			componentType = ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_PART;
			upgrades = convertUpgrades(template.upgrades());
		} else {
			componentType = ComponentTypeRegistry.STRUCTURED_PART;
		}

		return new CofComponent(newId, componentType, merged.tags(), merged.properties(), newStructure, upgrades, 1);
	}

	private CofComponent generateEquipment(RawDefinition templateDef, Map<String, CofComponent> combination) {
		EquipmentTemplateData template = (EquipmentTemplateData) templateDef.data();

		String idTemplate = Objects.requireNonNullElse(template.structure().id(), "{head.material.name}-tool");
		OpenIdentifier newId = OpenIdentifier.parse(idTemplateResolver.resolve(idTemplate, combination));

		List<DefinitionData> rawPartsDtoList = combination.values().stream()
				.flatMap(comp -> getSourceDtosForComponent(comp).stream())
				.toList();

		List<DefinitionData> fullDtoList = new ArrayList<>();
		fullDtoList.add(template);
		fullDtoList.addAll(rawPartsDtoList);
		PropertyMerger.MergedResult merged = propertyMerger.merge(fullDtoList);

		Map<OpenIdentifier, CofSlot> newSlots = combination.entrySet().stream()
				.collect(Collectors.toMap(
						entry -> idFactory.of(entry.getKey()),
						entry -> {
							CofComponent content = entry.getValue();
							EquipmentTemplateSlotData slotInfo = template.structure().slots().get(entry.getKey());
							return new CofSlot(idFactory.of(entry.getKey()), slotInfo.type(), null, content, null);
						}
				));
		CofStructure newStructure = new CofStructure(newSlots);

		OpenIdentifier componentType;
		CofUpgrades upgrades = null;
		if (template.upgrades() != null && !template.upgrades().isEmpty()) {
			componentType = ComponentTypeRegistry.STRUCTURED_EXTENSIBLE_EQUIPMENT;
			upgrades = convertUpgrades(template.upgrades());
		} else {
			componentType = ComponentTypeRegistry.STRUCTURED_EQUIPMENT;
		}

		return new CofComponent(newId, componentType, merged.tags(), merged.properties(), newStructure, upgrades, 1);
	}

	private List<DefinitionData> getSourceDtosForComponent(CofComponent component) {
		if (component.structure() == null) { // It's a static component
			return List.of(rawDefinitions.get(component.id()).data());
		}
		// It's a generated part, recursively find its sources
		return component.structure().slots().values().stream()
				.flatMap(slot -> getSourceDtosForComponent(slot.content()).stream())
				.collect(Collectors.toList());
	}

	private List<Map<String, CofComponent>> findCombinationsForPart(Map<String, PartTemplateStructureSlotData> slots, @Nullable com.sigmundgranaas.forgero.data.loading.api.data.GenerationConfigData generationConfig) {
		return findCombinationsInternal(slots, staticComponents, PartTemplateStructureSlotData::type, generationConfig);
	}

	private List<Map<String, CofComponent>> findCombinationsForEquipment(Map<String, EquipmentTemplateSlotData> slots, Map<OpenIdentifier, CofComponent> partsPool) {
		return findCombinationsInternal(slots, partsPool, EquipmentTemplateSlotData::type, null);
	}

	private <T> List<Map<String, CofComponent>> findCombinationsInternal(Map<String, T> slots, Map<OpenIdentifier, CofComponent> componentPool, java.util.function.Function<T, OpenIdentifier> typeExtractor, @Nullable com.sigmundgranaas.forgero.data.loading.api.data.GenerationConfigData generationConfig) {
		if (slots.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, List<CofComponent>> compatibles = slots.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> {
							List<CofComponent> runtimeCompatible = findCompatibleComponents(typeExtractor.apply(entry.getValue()), componentPool);

							// Apply generation filter if present
							if (generationConfig != null && generationConfig.getFilterForSlot(entry.getKey()) != null) {
								return applyGenerationFilter(runtimeCompatible, generationConfig.getFilterForSlot(entry.getKey()));
							}
							return runtimeCompatible;
						}
				));

		List<Map<String, CofComponent>> combinations = new ArrayList<>();
		List<String> slotNames = new ArrayList<>(compatibles.keySet());
		buildCombinationsRecursive(0, slotNames, compatibles, new HashMap<>(), combinations);
		return combinations;
	}

	private void buildCombinationsRecursive(int slotIndex, List<String> slotNames, Map<String, List<CofComponent>> compatibles, Map<String, CofComponent> currentCombination, List<Map<String, CofComponent>> allCombinations) {
		if (slotIndex == slotNames.size()) {
			allCombinations.add(new HashMap<>(currentCombination));
			return;
		}

		String currentSlotName = slotNames.get(slotIndex);
		List<CofComponent> componentsForSlot = compatibles.get(currentSlotName);

		for (CofComponent component : componentsForSlot) {
			currentCombination.put(currentSlotName, component);
			buildCombinationsRecursive(slotIndex + 1, slotNames, compatibles, currentCombination, allCombinations);
			currentCombination.remove(currentSlotName); // backtrack
		}
	}

	private Optional<HostData> generateHostData(HostTemplateData template, OpenIdentifier newId, Map<String, CofComponent> combination) {
		if (template.create() == null) {
			return Optional.empty();
		}

		String resolvedIdStr = idTemplateResolver.resolve(template.create().id(), combination);
		String resolvedClassName = idTemplateResolver.resolve(template.create().className(), combination);
		String resolvedItemGroup = template.create().itemGroup() != null ? idTemplateResolver.resolve(template.create().itemGroup(), combination) : null;

		OpenIdentifier resolvedId = OpenIdentifier.parse(resolvedIdStr);
		return Optional.of(new HostData(null, new CreateData(resolvedId, resolvedClassName, resolvedItemGroup)));
	}

	/**
	 * Applies tag-based filtering to a list of components based on generation filter rules.
	 * Logs warnings if the filter excludes all components or has potentially conflicting rules.
	 *
	 * @param components The runtime-compatible components to filter
	 * @param filter     The generation filter rules to apply
	 * @return Filtered list of components that pass all filter criteria
	 */
	private List<CofComponent> applyGenerationFilter(List<CofComponent> components, com.sigmundgranaas.forgero.data.loading.api.data.SlotGenerationFilter filter) {
		if (filter.isEmpty()) {
			return components;
		}

		// Validate filter for potential issues before applying
		validateGenerationFilter(filter, components);

		// If explicit list is provided, it overrides all tag filters
		if (filter.explicitList() != null && !filter.explicitList().isEmpty()) {
			Set<OpenIdentifier> allowedIds = new HashSet<>(filter.explicitList());
			List<CofComponent> filtered = components.stream()
					.filter(comp -> allowedIds.contains(comp.id()))
					.toList();

			// Warn if explicit list excludes all components
			if (filtered.isEmpty() && !components.isEmpty()) {
				LOGGER.warn("Generation filter with explicit list excluded all components. Explicit IDs: {}, Available components: {}",
						filter.explicitList(), components.stream().map(c -> c.id().toString()).toList());
			}

			return filtered;
		}

		// Otherwise apply tag-based filters
		List<CofComponent> filtered = components.stream()
				.filter(comp -> {
					Set<OpenIdentifier> componentTags = comp.tags();
					if (componentTags == null || componentTags.isEmpty()) {
						return false;
					}

					// requireAllTags: Component must have ALL of these tags
					if (filter.requireAllTags() != null && !filter.requireAllTags().isEmpty()) {
						boolean hasAllRequired = filter.requireAllTags().stream()
								.allMatch(tag -> tagResolver.hasTag(() -> componentTags, tag));
						if (!hasAllRequired) {
							return false;
						}
					}

					// requireAnyTags: Component must have AT LEAST ONE of these tags
					if (filter.requireAnyTags() != null && !filter.requireAnyTags().isEmpty()) {
						boolean hasAnyRequired = filter.requireAnyTags().stream()
								.anyMatch(tag -> tagResolver.hasTag(() -> componentTags, tag));
						if (!hasAnyRequired) {
							return false;
						}
					}

					// excludeAnyTags: Component must NOT have ANY of these tags
					if (filter.excludeAnyTags() != null && !filter.excludeAnyTags().isEmpty()) {
						boolean hasAnyExcluded = filter.excludeAnyTags().stream()
								.anyMatch(tag -> tagResolver.hasTag(() -> componentTags, tag));
						if (hasAnyExcluded) {
							return false;
						}
					}

					// excludeAllTags: Component must NOT have ALL of these tags (can have some)
					if (filter.excludeAllTags() != null && !filter.excludeAllTags().isEmpty()) {
						boolean hasAllExcluded = filter.excludeAllTags().stream()
								.allMatch(tag -> tagResolver.hasTag(() -> componentTags, tag));
						if (hasAllExcluded) {
							return false;
						}
					}

					return true;
				})
				.toList();

		// Warn if filter excluded all components
		if (filtered.isEmpty() && !components.isEmpty()) {
			LOGGER.warn("Generation filter excluded all components. Filter: {}, Available components: {}{}",
					formatFilter(filter),
					components.stream().map(c -> c.id().toString()).limit(5).toList(),
					components.size() > 5 ? " (and " + (components.size() - 5) + " more)" : "");
		}

		return filtered;
	}

	/**
	 * Validates a generation filter for potential issues and logs warnings.
	 *
	 * @param filter     The filter to validate
	 * @param components The components that will be filtered
	 */
	private void validateGenerationFilter(com.sigmundgranaas.forgero.data.loading.api.data.SlotGenerationFilter filter, List<CofComponent> components) {
		// Check for conflicting requireAllTags and excludeAnyTags
		if (filter.requireAllTags() != null && filter.excludeAnyTags() != null) {
			Set<OpenIdentifier> intersection = new HashSet<>(filter.requireAllTags());
			intersection.retainAll(filter.excludeAnyTags());
			if (!intersection.isEmpty()) {
				LOGGER.warn("Generation filter has conflicting rules: tags {} are both required (requireAllTags) and excluded (excludeAnyTags). This will exclude all components.",
						intersection);
			}
		}

		// Check for potentially ineffective requireAnyTags with excludeAnyTags
		if (filter.requireAnyTags() != null && filter.excludeAnyTags() != null) {
			Set<OpenIdentifier> allRequired = new HashSet<>(filter.requireAnyTags());
			Set<OpenIdentifier> allExcluded = new HashSet<>(filter.excludeAnyTags());
			if (allExcluded.containsAll(allRequired)) {
				LOGGER.warn("Generation filter excludes all required tags (excludeAnyTags contains all requireAnyTags). Required: {}, Excluded: {}",
						filter.requireAnyTags(), filter.excludeAnyTags());
			}
		}

		// Check for explicit list with other filters (explicit list overrides tag filters)
		if (filter.explicitList() != null && !filter.explicitList().isEmpty()) {
			boolean hasTagFilters = (filter.requireAllTags() != null && !filter.requireAllTags().isEmpty()) ||
					(filter.requireAnyTags() != null && !filter.requireAnyTags().isEmpty()) ||
					(filter.excludeAnyTags() != null && !filter.excludeAnyTags().isEmpty()) ||
					(filter.excludeAllTags() != null && !filter.excludeAllTags().isEmpty());
			if (hasTagFilters) {
				LOGGER.info("Generation filter has both explicit list and tag filters. Explicit list will override tag filters. Consider removing unused tag filters.");
			}
		}
	}

	/**
	 * Formats a filter for logging purposes.
	 *
	 * @param filter The filter to format
	 * @return A string representation of the filter
	 */
	private String formatFilter(com.sigmundgranaas.forgero.data.loading.api.data.SlotGenerationFilter filter) {
		List<String> parts = new java.util.ArrayList<>();
		if (filter.requireAllTags() != null && !filter.requireAllTags().isEmpty()) {
			parts.add("requireAllTags=" + filter.requireAllTags());
		}
		if (filter.requireAnyTags() != null && !filter.requireAnyTags().isEmpty()) {
			parts.add("requireAnyTags=" + filter.requireAnyTags());
		}
		if (filter.excludeAnyTags() != null && !filter.excludeAnyTags().isEmpty()) {
			parts.add("excludeAnyTags=" + filter.excludeAnyTags());
		}
		if (filter.excludeAllTags() != null && !filter.excludeAllTags().isEmpty()) {
			parts.add("excludeAllTags=" + filter.excludeAllTags());
		}
		if (filter.explicitList() != null && !filter.explicitList().isEmpty()) {
			parts.add("explicitList=" + filter.explicitList());
		}
		return "{" + String.join(", ", parts) + "}";
	}

	private List<CofComponent> findCompatibleComponents(OpenIdentifier typeTag, Map<OpenIdentifier, CofComponent> pool) {
		return pool.values().stream()
				.filter(comp -> comp.tags() != null && tagResolver.hasTag(comp::tags, typeTag))
				.toList();
	}

	private CofUpgrades convertUpgrades(@Nullable List<UpgradeSlotData> upgradeDataList) {
		if (upgradeDataList == null || upgradeDataList.isEmpty()) {
			return null;
		}
		var slots = upgradeDataList.stream()
				.map(upgrade -> new CofSlot(upgrade.id(), upgrade.type(), upgrade.description(), null, upgrade.tags()))
				.toList();
		return new CofUpgrades(slots);
	}
}
