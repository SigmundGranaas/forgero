package com.sigmundgranaas.forgero.cof;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.core.property.api.Property;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ComponentConstructorRegistry {
	private static final ComponentConstructorRegistry INSTANCE = new ComponentConstructorRegistry();
	private final Map<String, ComponentConstructor> constructorMap = new ConcurrentHashMap<>();
	private final List<Pair<Class<? extends Component>, String>> typeMap = new CopyOnWriteArrayList<>();

	private ComponentConstructorRegistry() {
		// Private constructor for singleton
	}

	public static ComponentConstructorRegistry getInstance() {
		return INSTANCE;
	}

	public void registerCoreTypes() {
		// Register from most specific to least specific to ensure correct type resolution
		register("forgero:structured_extensible_equipment", StructuredExtensibleEquipment.class, (dto, props, struct, upgs) -> {
			if (struct == null || upgs == null) {
				return DataResult.error(() -> "Missing structure or upgrades for structured_extensible_equipment with id: " + dto.id());
			}
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new StructuredExtensibleEquipment(dto.id(), tags, props, struct, upgs));
		});

		register("forgero:structured_extensible_part", StructuredExtensiblePart.class, (dto, props, struct, upgs) -> {
			if (struct == null || upgs == null) {
				return DataResult.error(() -> "Missing structure or upgrades for structured_extensible_part with id: " + dto.id());
			}
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new StructuredExtensiblePart(dto.id(), tags, props, struct, upgs));
		});

		register("forgero:structured_equipment", StructuredEquipment.class, (dto, props, struct, upgs) -> {
			if (struct == null) {
				return DataResult.error(() -> "Missing structure for structured_equipment with id: " + dto.id());
			}
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new StructuredEquipment(dto.id(), tags, props, struct));
		});

		register("forgero:structured_part", StructuredPart.class, (dto, props, struct, upgs) -> {
			if (struct == null) {
				return DataResult.error(() -> "Missing structure for structured_part with id: " + dto.id());
			}
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new StructuredPart(dto.id(), tags, props, struct));
		});

		register("forgero:extensible_equipment", ExtensibleEquipment.class, (dto, props, struct, upgs) -> {
			if (upgs == null) {
				return DataResult.error(() -> "Missing upgrades for extensible_equipment with id: " + dto.id());
			}
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new ExtensibleEquipment(dto.id(), tags, props, upgs));
		});

		register("forgero:extensible_part", ExtensiblePart.class, (dto, props, struct, upgs) -> {
			if (upgs == null) {
				return DataResult.error(() -> "Missing upgrades for extensible_part with id: " + dto.id());
			}
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new ExtensiblePart(dto.id(), tags, props, upgs));
		});

		register("forgero:static_equipment", StaticEquipment.class, (dto, props, struct, upgs) -> {
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new StaticEquipment(dto.id(), tags, props));
		});

		register("forgero:static_component", StaticComponent.class, (dto, props, struct, upgs) -> {
			var tags = dto.tags() != null ? dto.tags() : Collections.<OpenIdentifier>emptySet();
			return DataResult.success(new StaticComponent(dto.id(), tags, props));
		});
	}

	public void register(String type, Class<? extends Component> clazz, ComponentConstructor constructor) {
		constructorMap.put(type, constructor);
		typeMap.add(Pair.of(clazz, type));
	}

	public String getTypeString(Component component) {
		return typeMap.stream()
				.filter(pair -> pair.getFirst().isInstance(component))
				.findFirst()
				.map(Pair::getSecond)
				.orElseThrow(() -> new IllegalArgumentException("Unsupported component type for serialization: " + component.getClass().getName()));
	}

	public DataResult<Component> construct(CofComponent dto, List<Property> properties, ComponentStructure structure, ComponentUpgrades upgrades) {
		ComponentConstructor constructor = constructorMap.get(dto.componentType());
		if (constructor == null) {
			return DataResult.error(() -> "Unknown component type '" + dto.componentType() + "' for id: " + dto.id());
		}
		return constructor.construct(dto, properties, structure, upgrades);
	}

	public void clear() {
		constructorMap.clear();
		typeMap.clear();
	}

	@FunctionalInterface
	public interface ComponentConstructor {
		DataResult<Component> construct(CofComponent dto, List<Property> properties, ComponentStructure structure, ComponentUpgrades upgrades);
	}
}
