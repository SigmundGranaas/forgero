package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.mojang.serialization.DataResult;
import com.sigmundgranaas.forgero.cof.ComponentConstructor;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.cof.dto.CofSlot;
import com.sigmundgranaas.forgero.cof.dto.CofStructure;
import com.sigmundgranaas.forgero.cof.dto.CofUpgrades;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotFactory;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotFactoryRegistry;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds runtime {@link Component} objects from a map of final {@link CofComponent} DTOs.
 * This is the final stage of the pipeline, turning data into live game objects.
 */
public class ComponentBuilder {
	private final ComponentConstructor constructorRegistry;
	private final Map<OpenIdentifier, CofComponent> dtoList;
	private final Map<OpenIdentifier, Component> componentCache = new HashMap<>();

	public ComponentBuilder(ComponentConstructor registry, Map<OpenIdentifier, CofComponent> dtoList) {
		this.constructorRegistry = registry;
		this.dtoList = dtoList;
	}

	public List<Component> buildAll() {
		return dtoList.keySet().stream().map(this::buildFromId).collect(Collectors.toList());
	}

	private Component buildFromId(OpenIdentifier id) {
		if (componentCache.containsKey(id)) {
			return componentCache.get(id);
		}
		CofComponent dto = dtoList.get(id);
		if (dto == null) {
			throw new IllegalStateException("Attempted to build a component that was not processed: " + id);
		}

		// Pre-cache the component to handle recursive structures
		// We'll throw this away if building fails.
		// A proxy/future approach would be more robust but this is simpler for now.
		// To avoid issues, we will build children first.

		Component component = buildComponentFromDto(dto)
				.getOrThrow(false, err -> { throw new IllegalStateException(err); });

		componentCache.put(id, component);
		return component;
	}

	private DataResult<Component> buildComponentFromDto(CofComponent dto) {
		DataResult<ComponentStructure> structureResult = dto.structure().isPresent()
				? buildStructureFromDto(dto.structure().get())
				: DataResult.success(null);

		DataResult<ComponentUpgrades> upgradesResult = dto.upgrades().isPresent()
				? buildUpgradesFromDto(dto.upgrades().get())
				: DataResult.success(null);

		return structureResult.flatMap(structure ->
				upgradesResult.flatMap(upgrades ->
						constructorRegistry.construct(dto.id(), dto.componentType(), dto.tags().orElse(Set.of()), dto.properties().orElse(Map.of()), structure, upgrades)
				)
		);
	}

	private DataResult<ComponentStructure> buildStructureFromDto(CofStructure structureDto) {
		List<ComponentPart> parts = new ArrayList<>();
		for (Map.Entry<OpenIdentifier, CofSlot> entry : structureDto.slots().entrySet()) {
			CofSlot slotDto = entry.getValue();
			if (slotDto.content() == null) {
				// This case should be handled by template generator, but as a safeguard.
				return DataResult.error(() -> "Structure slot " + entry.getKey() + " is missing content.");
			}
			Component childComponent = buildFromId(slotDto.content().id());
			parts.add(new ComponentPart(entry.getKey(), slotDto.type(), slotDto.description(), SlotValidator.ACCEPT_ALL, childComponent));
		}
		return DataResult.success(ComponentStructure.of(parts));
	}

	private DataResult<ComponentUpgrades> buildUpgradesFromDto(CofUpgrades upgradesDto) {
		List<Slot> slots = new ArrayList<>();
		for (CofSlot slotDto : upgradesDto.slots()) {
			Component childComponent = null;
			if (slotDto.content() != null) {
				childComponent = buildFromId(slotDto.content().id());
			}

			// Dispatch on the slot kind so a plugin-defined slot type is built by its factory.
			// Absent kind => the standard component-upgrade slot, so existing content is unchanged.
			OpenIdentifier kind = slotDto.kind();
			SlotFactory factory = SlotFactoryRegistry.get(kind);
			if (factory == null) {
				return DataResult.error(() -> "Unknown slot kind '" + kind + "' for slot " + slotDto.id()
						+ " — register a SlotFactory for it.");
			}
			SlotFactory.SlotSpec spec = new SlotFactory.SlotSpec(
					slotDto.id(),
					slotDto.type(),
					slotDto.description() == null ? "" : slotDto.description(),
					java.util.Set.copyOf(slotDto.tagsOrEmpty()),
					slotDto.validTags());
			slots.add(factory.create(spec, Optional.ofNullable(childComponent)));
		}
		return DataResult.success(ComponentUpgrades.ofSlots(slots));
	}
}
