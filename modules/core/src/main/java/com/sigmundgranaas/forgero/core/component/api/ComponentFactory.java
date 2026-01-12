package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating components with the appropriate implementation type.
 * <p>
 * This is not an interface - there's one way to create components, and it's this.
 * The factory encapsulates the mapping from capabilities to implementation types.
 * <p>
 * Use this factory when you need to create components programmatically.
 * For step-by-step construction, see {@link ComponentBuilder}.
 */
public final class ComponentFactory {

	private ComponentFactory() {
	}

	/**
	 * Creates a component with the appropriate type based on provided structure and upgrades.
	 *
	 * @param id          Component identifier
	 * @param tags        Component tags
	 * @param properties  Component properties
	 * @param structure   Optional structure (null if none)
	 * @param upgrades    Optional upgrades (null if none)
	 * @param isEquipment Whether this is equipment (affects type selection)
	 * @return The appropriate component implementation
	 */
	public static Component create(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties,
			ComponentStructure structure,
			ComponentUpgrades upgrades,
			boolean isEquipment
	) {
		boolean hasStructure = structure != null && !structure.isEmpty();
		boolean hasUpgrades = upgrades != null && !upgrades.isEmpty();

		if (isEquipment) {
			return createEquipment(id, tags, properties, structure, upgrades, hasStructure, hasUpgrades);
		} else {
			return createPart(id, tags, properties, structure, upgrades, hasStructure, hasUpgrades);
		}
	}

	private static Component createEquipment(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties,
			ComponentStructure structure,
			ComponentUpgrades upgrades,
			boolean hasStructure,
			boolean hasUpgrades
	) {
		if (hasStructure && hasUpgrades) {
			return StructuredExtensibleEquipment.create(id, tags, properties, structure, upgrades);
		} else if (hasStructure) {
			return StructuredEquipment.create(id, tags, properties, structure);
		} else if (hasUpgrades) {
			return ExtensibleEquipment.create(id, tags, properties, upgrades);
		} else {
			return StaticEquipment.create(id, tags, properties);
		}
	}

	private static Component createPart(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties,
			ComponentStructure structure,
			ComponentUpgrades upgrades,
			boolean hasStructure,
			boolean hasUpgrades
	) {
		if (hasStructure && hasUpgrades) {
			return new StructuredExtensiblePart(id, tags, properties, structure, upgrades);
		} else if (hasStructure) {
			return new StructuredPart(id, tags, properties, structure);
		} else if (hasUpgrades) {
			return new ExtensiblePart(id, tags, properties, upgrades);
		} else {
			return new StaticComponent(id, tags, properties);
		}
	}

	/**
	 * Creates a simple static component with no structure or upgrades.
	 */
	public static Component createStatic(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties
	) {
		return new StaticComponent(id, tags, properties);
	}

	/**
	 * Creates static equipment with no structure or upgrades.
	 */
	public static Component createStaticEquipment(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			Map<String, List<?>> properties
	) {
		return StaticEquipment.create(id, tags, properties);
	}

	/**
	 * Creates a component from an existing component with additional properties merged in.
	 * Preserves the component's type and structure.
	 */
	public static Component derive(Component base, Map<String, List<?>> additionalProperties) {
		return base.withProperties(additionalProperties);
	}

	/**
	 * Returns the component type identifier for a component instance.
	 * Useful for serialization.
	 *
	 * @deprecated Use {@link Component#getTypeIdentifier()} instead.
	 * This static method will be removed in a future version.
	 * @since 0.14.0 deprecated
	 */
	@Deprecated(since = "0.14.0", forRemoval = true)
	public static OpenIdentifier getTypeIdentifier(Component component) {
		return component.getTypeIdentifier();
	}
}
