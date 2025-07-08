package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.variant.*;
import com.sigmundgranaas.forgero.core.feature.api.Feature;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.Operator;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticConditions;
import com.sigmundgranaas.forgero.data.v3.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.v3.dto.*;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.condition.*;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ComponentMapper {

	private final IdentifierFactory identifierFactory;
	private final Map<OpenIdentifier, Component> mappedComponentCache = new HashMap<>(); // Cache for mapped components
	private final Map<OpenIdentifier, TopLevelData> allProcessedData;

	public ComponentMapper(IdentifierFactory identifierFactory, Map<OpenIdentifier, TopLevelData> allProcessedData) {
		this.identifierFactory = identifierFactory;
		this.allProcessedData = allProcessedData;
	}

	/**
	 * Maps a TopLevelData DTO (identified by its ID) into a concrete Forgero Component object.
	 * This method recursively maps child components as needed.
	 *
	 * @param id The OpenIdentifier of the DTO to map.
	 * @return An Optional containing the mapped Component, or empty if mapping fails or DTO not found.
	 */
	public Optional<Component> map(OpenIdentifier id) {
		if (mappedComponentCache.containsKey(id)) {
			return Optional.of(mappedComponentCache.get(id));
		}

		TopLevelData data = allProcessedData.get(id);
		if (data == null) {
			// This should ideally not happen if the data pipeline is correct.
			// Log warning: Tried to map a component DTO that doesn't exist in processed data.
			return Optional.empty();
		}

		Component component = null;
		Object rawDto = data.unwrapAs(Object.class);
		Set<OpenIdentifier> tags = data.tags(); // Use the getTags from Taggable interface
		List<Property> properties = mapProperties(data.attributes(), data.features());

		// Dispatch based on the raw DTO type
		if (rawDto instanceof MaterialData || rawDto instanceof StaticPartData || rawDto instanceof SchematicData) {
			// Materials, StaticParts, and Schematics are mapped to StaticComponents
			// Schematics are typically consumed by crafting systems, but can exist as items.
			component = new StaticComponent(data.id(), tags, properties);
		} else if (rawDto instanceof GeneratedPartData generatedPart) {
			// GeneratedPartData (represents a generated part like iron-pickaxe_head)
			// Its structure now contains a concrete material ID.
			ComponentStructure structure = mapPartStructure(generatedPart.structure());
			ComponentUpgrades upgrades = mapUpgradeSlots(generatedPart.upgrades());

			// Determine concrete Component type based on presence of structure and upgrades
			if (!structure.slots().isEmpty() && !upgrades.slots().isEmpty()) {
				component = new StructuredExtensiblePart(data.id(), tags, properties, structure, upgrades);
			} else if (!structure.slots().isEmpty()) {
				component = new StructuredPart(data.id(), tags, properties, structure);
			} else if (!upgrades.slots().isEmpty()) {
				component = new ExtensiblePart(data.id(), tags, properties, upgrades);
			} else {
				component = new StaticComponent(data.id(), tags, properties); // Fallback, e.g., an empty template
			}
		} else if (rawDto instanceof GeneratedEquipmentData generatedEquipment) {
			// GeneratedEquipmentData (represents a generated tool like iron-pickaxe-oak-handle)
			// Its structure now contains concrete part IDs.
			ComponentStructure structure = mapToolStructure(generatedEquipment.structure());
			ComponentUpgrades upgrades = mapUpgradeSlots(generatedEquipment.upgrades());

			// Determine concrete Component type based on presence of structure and upgrades
			if (!structure.slots().isEmpty() && !upgrades.slots().isEmpty()) {
				component = new StructuredExtensibleEquipment(data.id(), tags, properties, structure, upgrades);
			} else if (!structure.slots().isEmpty()) {
				component = new StructuredEquipment(data.id(), tags, properties, structure);
			} else if (!upgrades.slots().isEmpty()) {
				component = new ExtensibleEquipment(data.id(), tags, properties, upgrades);
			} else {
				component = new StaticEquipment(data.id(), tags, properties); // Fallback
			}
		} else if (rawDto instanceof PartTemplateData || rawDto instanceof ToolTemplateData) {
			// Explicitly ignore raw templates, they are not mapped to components directly
			return Optional.empty();
		}

		if (component != null) {
			mappedComponentCache.put(id, component); // Cache the mapped component
		}
		return Optional.ofNullable(component);
	}

	private List<Property> mapProperties(List<AttributeData> attributes, List<FeatureData> features) {
		List<Property> properties = new ArrayList<>();
		if (attributes != null) {
			attributes.stream()
					.map(this::mapAttribute)
					.flatMap(Optional::stream)
					.forEach(properties::add);
		}
		if (features != null) {
			features.stream().map(this::mapFeature).flatMap(Optional::stream).forEach(properties::add);
		}
		return properties;
	}

	private Optional<Property> mapAttribute(AttributeData data) {
		// Map computation operator
		Operator operator = switch (data.computation().operator()) {
			case AttributeCodecs.ADDITION_OPERATOR -> AdditionOperator.getInstance();
			case AttributeCodecs.MULTIPLICATION_OPERATOR -> MultiplicationOperator.getInstance();
			default -> AdditionOperator.getInstance(); // Sensible default
		};

		// Map computation order to a numerical group for sorting in ComputationChain
		int group = mapOrderToLevel(data.computation().order());

		// Map condition DTO to core Condition object
		Condition condition = mapCondition(data.condition());

		// Check if the attribute has a composite key
		if (data.composite() != null) {
			// If it has a composite key, map it to a CompositeAttributeComponent
			return Optional.of(new CompositeAttributeComponent(
					data.type(),
					data.computation().value(),
					operator,
					group,
					data.composite()
			));
		} else {
			// Otherwise, map it to a SimpleAttribute
			return Optional.of(new SimpleAttribute(data.type(), data.computation().value(), operator, group, condition));
		}
	}

	private int mapOrderToLevel(String order) {
		return switch (order) {
			case AttributeCodecs.BASE_ORDER -> 0; // Base/Default values
			case AttributeCodecs.MIDDLE_ORDER -> 1; // Middle
			case AttributeCodecs.END_ORDER -> 2; // End
			default -> 1; // Default group if order is not specified or unknown
		};
	}

	private Optional<Feature> mapFeature(FeatureData data) {
		Condition condition = mapCondition(data.condition());

		// This assumes a simple Feature mapping, extend as needed for specific FeatureData types
		if (data instanceof VeinMiningFeatureData veinMiningData) {
			// The actual VeinMining logic would be handled by the FeatureEngine at runtime.
			// Here, we just map the DTO to a generic Feature object for the engine to use.
			return Optional.of(new Feature(veinMiningData.type(), 1, condition)); // Level 1 for features, can be dynamic
		}
		// Add more specific FeatureData mappings here as they are introduced.
		return Optional.empty();
	}

	private Condition mapCondition(@Nullable ConditionData conditionData) {
		if (conditionData == null || conditionData.predicates().isEmpty()) {
			return Condition.ALWAYS_TRUE;
		}

		List<StaticCondition> staticConditions = new ArrayList<>();
		List<DynamicCondition> dynamicConditions = new ArrayList<>();

		// Iterate through all predicate DTOs and map them
		for (PredicateData predicateData : conditionData.predicates()) {
			// Attempt to map to a StaticCondition
			mapStaticPredicate(predicateData).ifPresent(staticConditions::add);
			// Attempt to map to a DynamicCondition (if any are defined)
			mapDynamicPredicate(predicateData).ifPresent(dynamicConditions::add);
		}
		return new Condition(staticConditions, dynamicConditions);
	}

	// Maps a PredicateData DTO to a StaticCondition (evaluated at bake time)
	private Optional<StaticCondition> mapStaticPredicate(PredicateData data) {
		if (data instanceof TagMatchPredicateData tagMatch) {
			if (tagMatch.type().path().equals("self_has_tag")) {
				return Optional.of(StaticConditions.selfHasTag(tagMatch.tag().path()));
			} else if (tagMatch.type().path().equals("root_has_tag")) {
				return Optional.of(StaticConditions.rootHasTag(tagMatch.tag().path()));
			}
		} else if (data instanceof InSlotTypePredicateData inSlotType) {
			return Optional.of(StaticConditions.selfInSlot(inSlotType.slotType()));
		} else if (data instanceof SlotContainsPredicateData slotContains) {
			return Optional.of(StaticConditions.slotContains(identifierFactory.of("forgero", slotContains.slot()), slotContains.tag().path()));
		} else if (data instanceof AndPredicateData andData) {
			List<StaticCondition> children = andData.predicates().stream()
					.flatMap(pred -> mapStaticPredicate(pred).stream())
					.toList();
			return Optional.of(ctx -> children.stream().allMatch(c -> c.test(ctx)));
		} else if (data instanceof OrPredicateData orData) {
			List<StaticCondition> children = orData.predicates().stream()
					.flatMap(pred -> mapStaticPredicate(pred).stream())
					.toList();
			return Optional.of(ctx -> children.stream().anyMatch(c -> c.test(ctx)));
		} else if (data instanceof NotPredicateData notData) {
			return mapStaticPredicate(notData.predicate())
					.map(pred -> (StaticCondition) ctx -> !pred.test(ctx));
		}
		return Optional.empty();
	}

	// Maps a PredicateData DTO to a DynamicCondition (evaluated at apply time)
	private Optional<DynamicCondition> mapDynamicPredicate(PredicateData data) {
		// Example: If you had a DTO for checking target tags
		// if (data instanceof TargetTagPredicateData targetTag) {
		//     return Optional.of(ctx -> ctx.get(ContextKeys.TARGET_TAGS).map(tags -> tags.contains(targetTag.tag())).orElse(false));
		// }
		// The provided DTOs don't include dynamic predicates, so this will be empty for now.
		return Optional.empty();
	}

	// Maps PartTemplate's structure (which has material.type as CONCRETE material ID now)
	private ComponentStructure mapPartStructure(PartTemplateStructureData structureData) {
		List<StructureSlot> slots = new ArrayList<>();
		if (structureData.material() != null) {
			// `structureData.material().type()` is now the concrete ID of the material component
			OpenIdentifier materialComponentId = structureData.material().type();
			Optional<Component> materialComponent = map(materialComponentId); // Recursive call to map the material
			// Assuming a fixed slot ID and type for the material slot within a part's structure.
			materialComponent.ifPresent(component -> slots.add(new StructureSlot(
					identifierFactory.of("material_slot"), // Standard slot ID within a part template
					identifierFactory.of("forgero:material_slot_type"), // Standard type tag for a material slot
					structureData.material().description() != null ? structureData.material().description() : "Required material",
					component
			)));
		}
		return new ComponentStructure(slots);
	}

	// Maps ToolTemplate's structure (which has concrete part IDs now)
	private ComponentStructure mapToolStructure(ToolTemplateStructureData structureData) {
		List<StructureSlot> slots = new ArrayList<>();
		for (Map.Entry<String, ToolTemplateSlotData> entry : structureData.slots().entrySet()) {
			String slotName = entry.getKey();
			ToolTemplateSlotData slotDto = entry.getValue();

			// `slotDto.defaultComponent()` now holds the concrete ID of the chosen part for this slot
			OpenIdentifier componentId = slotDto.defaultComponent();
			if (componentId != null) {
				Optional<Component> component = map(componentId); // Recursive call to map the part
				component.ifPresent(c -> {
					// Create a StructureSlot using the concrete part and the original slot type
					slots.add(new StructureSlot(
							identifierFactory.of(slotName), // Slot ID in the structure (e.g., "head", "handle")
							slotDto.type(),                 // Type tag of component accepted by this slot (e.g., pickaxe_head_type)
							"Slot for " + slotName,         // Simple description
							c
					));
				});
			}
		}
		return new ComponentStructure(slots);
	}

	// Maps UpgradeSlotData to core UpgradeSlot objects
	private ComponentUpgrades mapUpgradeSlots(@Nullable List<UpgradeSlotData> upgradeSlotDtos) {
		if (upgradeSlotDtos == null || upgradeSlotDtos.isEmpty()) {
			return new ComponentUpgrades(Collections.emptyList());
		}
		List<UpgradeSlot> slots = new ArrayList<>();
		for (UpgradeSlotData dto : upgradeSlotDtos) {
			// The validator needs to check if the incoming component has the required tags.
			// The tags list in UpgradeSlotData is a list of OpenIdentifier tags.
			Predicate<Component> validator = comp -> {
				if (dto.tags() == null || dto.tags().isEmpty()) {
					return true; // No tags specified, any component is valid.
				}
				Set<OpenIdentifier> requiredTags = new HashSet<>(dto.tags());
				// Assuming Component.getTags() returns the direct tags of a component,
				// and for inherited checks, the system using UpgradeSlot (e.g., ComponentMutater)
				// would use TagGraph.isTagged(). For this direct validator, we check direct tags.
				return comp.getTags().containsAll(requiredTags);
			};

			slots.add(new UpgradeSlot(
					dto.id(),
					dto.type(),
					dto.description() != null ? dto.description() : "Upgrade Slot",
					validator,
					Optional.empty() // Upgrade slots start empty by default
			));
		}
		return new ComponentUpgrades(slots);
	}
}
