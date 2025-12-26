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
			return new StructuredExtensibleEquipment(id, tags, properties, structure, upgrades);
		} else if (hasStructure) {
			return new StructuredEquipment(id, tags, properties, structure);
		} else if (hasUpgrades) {
			return new ExtensibleEquipment(id, tags, properties, upgrades);
		} else {
			return new StaticEquipment(id, tags, properties);
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
		return new StaticEquipment(id, tags, properties);
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
	 */
	public static OpenIdentifier getTypeIdentifier(Component component) {
		// Order matters: check most specific types first
		if (component instanceof StructuredExtensibleEquipment) {
			return OpenIdentifier.of("structured_extensible_equipment");
		} else if (component instanceof StructuredExtensiblePart) {
			return OpenIdentifier.of("structured_extensible_part");
		} else if (component instanceof StructuredEquipment) {
			return OpenIdentifier.of("structured_equipment");
		} else if (component instanceof StructuredPart) {
			return OpenIdentifier.of("structured_part");
		} else if (component instanceof ExtensibleEquipment) {
			return OpenIdentifier.of("extensible_equipment");
		} else if (component instanceof ExtensiblePart) {
			return OpenIdentifier.of("extensible_part");
		} else if (component instanceof StaticEquipment) {
			return OpenIdentifier.of("static_equipment");
		} else if (component instanceof StaticComponent) {
			return OpenIdentifier.of("static_component");
		}
		throw new IllegalArgumentException("Unknown component type: " + component.getClass().getName());
	}
}
