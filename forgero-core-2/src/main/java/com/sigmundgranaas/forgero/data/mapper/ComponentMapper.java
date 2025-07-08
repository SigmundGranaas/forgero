package com.sigmundgranaas.forgero.data.mapper;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.variant.*;
import com.sigmundgranaas.forgero.core.data.definition.GeneratedState;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
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
import com.sigmundgranaas.forgero.data.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.dto.condition.AndPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.ConditionData;
import com.sigmundgranaas.forgero.data.dto.condition.InSlotTypePredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.NotPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.OrPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.PredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.SlotContainsPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.TagMatchPredicateData;
import com.sigmundgranaas.forgero.data.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.dto.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.dto.template.UpgradeSlotData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ComponentMapper {

	private final IdentifierFactory identifierFactory;
	// Cache for already mapped components to prevent infinite recursion and redundant work
	private final Map<OpenIdentifier, Component> componentCache = new HashMap<>();

	public ComponentMapper(IdentifierFactory identifierFactory) {
		this.identifierFactory = identifierFactory;
	}

	// General map method for basic components
	public Component map(NormalizedState.NormalizedMaterial material) {
		if (componentCache.containsKey(material.id())) return componentCache.get(material.id());
		List<Property> properties = mapProperties(material.attributes(), material.features());
		Component component = new StaticComponent(material.id(), material.tags(), properties);
		componentCache.put(material.id(), component);
		return component;
	}

	public Component map(NormalizedState.NormalizedShape shape) {
		if (componentCache.containsKey(shape.id())) return componentCache.get(shape.id());
		List<Property> properties = mapProperties(shape.attributes(), shape.features());
		Component component = new StaticComponent(shape.id(), shape.tags(), properties);
		componentCache.put(shape.id(), component);
		return component;
	}

	public Component map(NormalizedState.NormalizedSchematic schematic, NormalizedState normalizedState) {
		if (componentCache.containsKey(schematic.id())) return componentCache.get(schematic.id());
		// Schematics themselves are simple static components; their "target" is metadata for crafting.
		Component component = new StaticComponent(schematic.id(), schematic.tags(), Collections.emptyList());
		componentCache.put(schematic.id(), component);
		return component;
	}

	public Component map(NormalizedState.NormalizedStaticPart staticPart) {
		if (componentCache.containsKey(staticPart.id())) return componentCache.get(staticPart.id());
		List<Property> properties = mapProperties(staticPart.attributes(), staticPart.features());
		ComponentUpgrades upgrades = mapUpgradeSlots(staticPart.upgrades());

		Component component;
		if (!upgrades.slots().isEmpty()) {
			component = new ExtensiblePart(staticPart.id(), staticPart.tags(), properties, upgrades);
		} else {
			component = new StaticComponent(staticPart.id(), staticPart.tags(), properties);
		}
		componentCache.put(staticPart.id(), component);
		return component;
	}

	// Mapping for GeneratedPart (composed of Material and Shape)
	public Component map(GeneratedState.GeneratedPart generatedPart, NormalizedState normalizedState) {
		if (componentCache.containsKey(generatedPart.id())) return componentCache.get(generatedPart.id());

		List<Property> properties = mapProperties(generatedPart.attributes(), generatedPart.features());
		ComponentUpgrades upgrades = mapUpgradeSlots(generatedPart.upgrades());

		// Recursively map the material and shape components
		Component materialComponent = Optional.ofNullable(componentCache.get(generatedPart.materialId()))
				.or(() -> Optional.ofNullable(normalizedState.materials().get(generatedPart.materialId())).map(this::map))
				.orElseThrow(() -> new IllegalStateException("Material " + generatedPart.materialId() + " not found or mapped for generated part " + generatedPart.id()));

		Component shapeComponent = Optional.ofNullable(componentCache.get(generatedPart.shapeId()))
				.or(() -> Optional.ofNullable(normalizedState.shapes().get(generatedPart.shapeId())).map(this::map))
				.orElseThrow(() -> new IllegalStateException("Shape " + generatedPart.shapeId() + " not found or mapped for generated part " + generatedPart.id()));

		// Construct the structure for the part
		List<StructureSlot> structureSlots = List.of(
				new StructureSlot(identifierFactory.of("material"), identifierFactory.of("forgero:material_slot_type"), "Material slot", materialComponent),
				new StructureSlot(identifierFactory.of("shape"), identifierFactory.of("forgero:shape_slot_type"), "Shape slot", shapeComponent)
		);
		ComponentStructure structure = new ComponentStructure(structureSlots);

		Component component;
		if (!upgrades.slots().isEmpty()) {
			component = new StructuredExtensiblePart(generatedPart.id(), generatedPart.tags(), properties, structure, upgrades);
		} else {
			component = new StructuredPart(generatedPart.id(), generatedPart.tags(), properties, structure);
		}
		componentCache.put(generatedPart.id(), component);
		return component;
	}

	// Mapping for GeneratedEquipment (composed of various parts)
	public Component map(GeneratedState.GeneratedEquipment generatedEquipment, GeneratedState generatedState) {
		if (componentCache.containsKey(generatedEquipment.id())) return componentCache.get(generatedEquipment.id());

		// GeneratedEquipment DTOs don't carry attributes/features directly (they come from the template)
		// We'll rely on attributes being resolved from the template in Stage 2/3 and then passed here.
		// If they were to be merged dynamically, it would be done here from children.
		// For now, assume properties for equipment are primarily from its template definition (which is mapped to attributes/features field in GeneratedEquipment).
		ComponentUpgrades upgrades = mapUpgradeSlots(generatedEquipment.upgrades());

		// Recursively map the parts within the equipment's structure
		List<StructureSlot> structureSlots = new ArrayList<>();
		for (Map.Entry<String, OpenIdentifier> slotEntry : generatedEquipment.structure().entrySet()) {
			String slotName = slotEntry.getKey();
			OpenIdentifier partId = slotEntry.getValue();

			// Attempt to find in cached components first
			Component partComponent = componentCache.get(partId);

			if (partComponent == null) {
				// If not in cache, this part might be a GeneratedPart that wasn't mapped yet (e.g. if ordering wasn't strictly correct)
				// Or a NormalizedStaticPart. We need to handle this.
				// For the `ForgeroDataInitializer`, ensure `map(staticPart)` and `map(generatedPart)` calls
				// happen before `map(generatedEquipment)`. If it's still missing, it's an error.
				throw new IllegalStateException("Part " + partId + " not found in component cache for equipment " + generatedEquipment.id() + ". Ensure all parts are mapped before equipment.");
			}

			// The slot type for the StructureSlot is based on the *type* that the original EquipmentTemplateSlot expected.
			// This information is not directly in `GeneratedEquipment`. It needs to be inferred or passed down.
			// For simplicity here, we'll use a generic type or make an assumption.
			// A robust solution would involve passing the `NormalizedEquipmentTemplate` or a mapping from it.
			// For now, let's use a generic 'equipment_slot_type' or the part's first tag as a fallback.
			OpenIdentifier slotTypeTag = partComponent.getTags().stream().findFirst().orElse(identifierFactory.of("forgero:equipment_slot_type"));

			structureSlots.add(new StructureSlot(identifierFactory.of(slotName), slotTypeTag, "Slot for " + slotName, partComponent));
		}
		ComponentStructure structure = new ComponentStructure(structureSlots);

		Component component;
		if (!upgrades.slots().isEmpty()) {
			component = new StructuredExtensibleEquipment(generatedEquipment.id(), generatedEquipment.tags(), Collections.emptyList(), structure, upgrades);
		} else {
			component = new StructuredEquipment(generatedEquipment.id(), generatedEquipment.tags(),  Collections.emptyList(), structure);
		}
		componentCache.put(generatedEquipment.id(), component);
		return component;
	}


	private List<Property> mapProperties(@Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features) {
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
		Operator operator = switch (data.computation().operator()) {
			case AttributeCodecs.ADDITION_OPERATOR -> AdditionOperator.getInstance();
			case AttributeCodecs.MULTIPLICATION_OPERATOR -> MultiplicationOperator.getInstance();
			default -> AdditionOperator.getInstance();
		};

		int group = mapOrderToLevel(data.computation().order());
		Condition condition = mapCondition(data.condition());

		if (data.composite() != null) {
			return Optional.of(new CompositeAttributeComponent(data.type(), data.computation().value(), operator, group, data.composite()));
		} else {
			return Optional.of(new SimpleAttribute(data.type(), data.computation().value(), operator, group, condition));
		}
	}

	private int mapOrderToLevel(String order) {
		return switch (order) {
			case AttributeCodecs.BASE_ORDER -> 0;
			case AttributeCodecs.MIDDLE_ORDER -> 1;
			case AttributeCodecs.END_ORDER -> 2;
			default -> 1;
		};
	}

	private Optional<Feature> mapFeature(FeatureData data) {
		Condition condition = mapCondition(data.condition());
		if (data instanceof VeinMiningFeatureData veinMiningData) {
			return Optional.of(new Feature(veinMiningData.type(), 1, condition));
		}
		return Optional.empty();
	}

	private Condition mapCondition(@Nullable ConditionData conditionData) {
		if (conditionData == null || conditionData.predicates().isEmpty()) {
			return Condition.ALWAYS_TRUE;
		}

		List<StaticCondition> staticConditions = new ArrayList<>();
		List<DynamicCondition> dynamicConditions = new ArrayList<>();

		for (PredicateData predicateData : conditionData.predicates()) {
			mapStaticPredicate(predicateData).ifPresent(staticConditions::add);
			mapDynamicPredicate(predicateData).ifPresent(dynamicConditions::add);
		}
		return new Condition(staticConditions, dynamicConditions);
	}

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

	private Optional<DynamicCondition> mapDynamicPredicate(PredicateData data) {
		return Optional.empty();
	}

	private ComponentUpgrades mapUpgradeSlots(@Nullable List<UpgradeSlotData> upgradeSlotDtos) {
		if (upgradeSlotDtos == null || upgradeSlotDtos.isEmpty()) {
			return new ComponentUpgrades(Collections.emptyList());
		}
		List<UpgradeSlot> slots = new ArrayList<>();
		for (UpgradeSlotData dto : upgradeSlotDtos) {
			Predicate<Component> validator = comp -> {
				if (dto.tags() == null || dto.tags().isEmpty()) {
					return true;
				}
				Set<OpenIdentifier> requiredTags = new HashSet<>(dto.tags());
				return comp.getTags().containsAll(requiredTags);
			};

			slots.add(new UpgradeSlot(
					dto.id(),
					dto.type(),
					dto.description() != null ? dto.description() : "Upgrade Slot",
					validator,
					Optional.empty()
			));
		}
		return new ComponentUpgrades(slots);
	}
}
