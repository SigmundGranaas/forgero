package com.sigmundgranaas.forgero.data.mapper.api;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.ConditionMapper;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.OperatorMapper;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ComponentMapper {

	private final IdentifierFactory identifierFactory;
	private final Map<OpenIdentifier, Component> componentCache = new HashMap<>();
	private final List<PropertyBuilder> propertyBuilders;
	private final ConditionMapper conditionMapper;
	private final OperatorMapper operatorMapper;

	/**
	 * Constructs a new ComponentMapper. It now automatically retrieves all registered
	 * PropertyBuilders from the {@link PropertyRegistry}.
	 *
	 * @param identifierFactory The factory for creating OpenIdentifier instances.
	 */
	public ComponentMapper(IdentifierFactory identifierFactory) {
		this.identifierFactory = identifierFactory;
		// Fetch property builders from the central registry
		this.propertyBuilders = PropertyRegistry.getInstance().getPropertyBuilders();
		this.conditionMapper = new ConditionMapper(identifierFactory);
		this.operatorMapper = new OperatorMapper();
	}

	// Maps a DTO's consolidated properties into a list of runtime Property objects.
	private List<Property> mapProperties(@Nullable Map<String, JsonElement> propertiesMap) {
		if (propertiesMap == null || propertiesMap.isEmpty()) {
			return Collections.emptyList();
		}

		List<Property> properties = new ArrayList<>();
		for (PropertyBuilder builder : propertyBuilders) {
			properties.addAll(builder.build(propertiesMap, conditionMapper, operatorMapper));
		}
		return properties;
	}

	public Component map(NormalizedState.NormalizedMaterial material) {
		if (componentCache.containsKey(material.id())) return componentCache.get(material.id());
		List<Property> properties = mapProperties(material.properties());
		Component component = new StaticComponent(material.id(), material.tags(), properties);
		componentCache.put(material.id(), component);
		return component;
	}

	public Component map(NormalizedState.NormalizedShape shape) {
		if (componentCache.containsKey(shape.id())) return componentCache.get(shape.id());
		List<Property> properties = mapProperties(shape.properties());
		Component component = new StaticComponent(shape.id(), shape.tags(), properties);
		componentCache.put(shape.id(), component);
		return component;
	}

	public Component map(NormalizedState.NormalizedSchematic schematic) {
		if (componentCache.containsKey(schematic.id())) return componentCache.get(schematic.id());
		// Schematics themselves are simple static components; their "target" is metadata for crafting.
		List<Property> properties = mapProperties(schematic.properties());
		Component component = new StaticComponent(schematic.id(), schematic.tags(), properties);
		componentCache.put(schematic.id(), component);
		return component;
	}

	public Component map(NormalizedState.NormalizedStaticPart staticPart) {
		if (componentCache.containsKey(staticPart.id())) return componentCache.get(staticPart.id());
		List<Property> properties = mapProperties(staticPart.properties());
		ComponentUpgrades upgrades = mapUpgradeSlots(staticPart.upgrades());

		Component component;
		if (upgrades != null && !upgrades.slots().isEmpty()) {
			component = new ExtensiblePart(staticPart.id(), staticPart.tags(), properties, upgrades);
		} else {
			component = new StaticComponent(staticPart.id(), staticPart.tags(), properties);
		}
		componentCache.put(staticPart.id(), component);
		return component;
	}

	public Component map(GeneratedState.GeneratedPart generatedPart, NormalizedState normalizedState) {
		if (componentCache.containsKey(generatedPart.id())) return componentCache.get(generatedPart.id());

		List<Property> properties = mapProperties(generatedPart.properties());
		ComponentUpgrades upgrades = mapUpgradeSlots(generatedPart.upgrades());

		// Recursively map the material and shape components (these are NormalizedState DTOs)
		Component materialComponent = Optional.ofNullable(normalizedState.materials().get(generatedPart.materialId())).map(this::map)
				.orElseThrow(() -> new IllegalStateException("Material " + generatedPart.materialId() + " not found for generated part " + generatedPart.id()));

		Component shapeComponent = Optional.ofNullable(normalizedState.shapes().get(generatedPart.shapeId())).map(this::map)
				.orElseThrow(() -> new IllegalStateException("Shape " + generatedPart.shapeId() + " not found for generated part " + generatedPart.id()));

		// Construct the structure for the part
		List<StructureSlot> structureSlots = List.of(
				new StructureSlot(identifierFactory.of("material"), identifierFactory.of("forgero:material_slot_type"), "Material slot", materialComponent),
				new StructureSlot(identifierFactory.of("shape"), identifierFactory.of("forgero:shape_slot_type"), "Shape slot", shapeComponent)
		);
		ComponentStructure structure = new ComponentStructure(structureSlots);

		Component component;
		if (upgrades != null && !upgrades.slots().isEmpty()) {
			component = new StructuredExtensiblePart(generatedPart.id(), generatedPart.tags(), properties, structure, upgrades);
		} else {
			component = new StructuredPart(generatedPart.id(), generatedPart.tags(), properties, structure);
		}
		componentCache.put(generatedPart.id(), component);
		return component;
	}

	public Component map(GeneratedState.GeneratedEquipment generatedEquipment) {
		if (componentCache.containsKey(generatedEquipment.id())) return componentCache.get(generatedEquipment.id());

		List<Property> properties = mapProperties(generatedEquipment.properties());
		ComponentUpgrades upgrades = mapUpgradeSlots(generatedEquipment.upgrades());

		List<StructureSlot> structureSlots = new ArrayList<>();
		for (Map.Entry<String, OpenIdentifier> slotEntry : generatedEquipment.structure().entrySet()) {
			OpenIdentifier partId = slotEntry.getValue();
			Component partComponent = componentCache.get(partId);
			if (partComponent == null) {
				throw new IllegalStateException("Part " + partId + " not found in component cache for equipment " + generatedEquipment.id() + ". Ensure all parts are mapped before equipment.");
			}
			OpenIdentifier slotTypeTag = partComponent.getTags().stream().findFirst().orElse(identifierFactory.of("forgero:equipment_slot_type"));
			structureSlots.add(new StructureSlot(identifierFactory.of(slotEntry.getKey()), slotTypeTag, "Slot for " + slotEntry.getKey(), partComponent));
		}
		ComponentStructure structure = new ComponentStructure(structureSlots);

		Component component;
		if (upgrades != null && !upgrades.slots().isEmpty()) {
			component = new StructuredExtensibleEquipment(generatedEquipment.id(), generatedEquipment.tags(), properties, structure, upgrades);
		} else {
			component = new StructuredEquipment(generatedEquipment.id(), generatedEquipment.tags(), properties, structure);
		}
		componentCache.put(generatedEquipment.id(), component);
		return component;
	}

	private @Nullable ComponentUpgrades mapUpgradeSlots(@Nullable List<UpgradeSlotData> upgradeSlotDtos) {
		if (upgradeSlotDtos == null || upgradeSlotDtos.isEmpty()) {
			return null;
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
