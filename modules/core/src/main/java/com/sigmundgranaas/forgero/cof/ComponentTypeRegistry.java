package com.sigmundgranaas.forgero.cof;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * Central registry of all component type identifiers used throughout Forgero.
 * <p>
 * Provides type-safe constants for component types, ensuring consistency across
 * ComponentConstructor, TemplateGenerator, CofComponentConverter, and ComponentCofCodec.
 * <p>
 * Component types represent the implementation class of a component, not its identity.
 * For example, "iron_pickaxe" is a component ID, while "structured_equipment" is its type.
 *
 * @since 0.14.0
 */
public final class ComponentTypeRegistry {
	private ComponentTypeRegistry() {
		// Prevent instantiation
	}

	private static final IdentifierFactory FACTORY = CodecConstants.IDENTIFIER_FACTORY;

	// Static component types (no structure, no upgrades)

	/**
	 * Type identifier for simple components with no structure or upgrades.
	 * Examples: materials (iron, oak), gems, simple items.
	 */
	public static final OpenIdentifier STATIC_COMPONENT = FACTORY.of("static_component");

	/**
	 * Type identifier for simple equipment with no structure or upgrades.
	 * Examples: static tools or armor pieces.
	 */
	public static final OpenIdentifier STATIC_EQUIPMENT = FACTORY.of("static_equipment");

	// Extensible types (upgrades, no structure)

	/**
	 * Type identifier for parts that have upgrade slots but no structure.
	 * Can be customized with upgrades but are not composed of other components.
	 */
	public static final OpenIdentifier EXTENSIBLE_PART = FACTORY.of("extensible_part");

	/**
	 * Type identifier for equipment that has upgrade slots but no structure.
	 * Can be customized with upgrades but is not composed of other components.
	 */
	public static final OpenIdentifier EXTENSIBLE_EQUIPMENT = FACTORY.of("extensible_equipment");

	// Structured types (structure, no upgrades)

	/**
	 * Type identifier for parts composed of other components (structure slots).
	 * Examples: pickaxe heads made from materials and shapes.
	 */
	public static final OpenIdentifier STRUCTURED_PART = FACTORY.of("structured_part");

	/**
	 * Type identifier for equipment composed of other components (structure slots).
	 * Examples: pickaxes made from heads and handles.
	 */
	public static final OpenIdentifier STRUCTURED_EQUIPMENT = FACTORY.of("structured_equipment");

	// Structured extensible types (both structure and upgrades)

	/**
	 * Type identifier for parts with both structure slots and upgrade slots.
	 * Composed of other components AND can be customized with upgrades.
	 */
	public static final OpenIdentifier STRUCTURED_EXTENSIBLE_PART = FACTORY.of("structured_extensible_part");

	/**
	 * Type identifier for equipment with both structure slots and upgrade slots.
	 * Composed of other components AND can be customized with upgrades.
	 */
	public static final OpenIdentifier STRUCTURED_EXTENSIBLE_EQUIPMENT = FACTORY.of("structured_extensible_equipment");

	/**
	 * Determines if a component type requires upgrades capability.
	 * <p>
	 * Used by ComponentCofCodec for validation during deserialization.
	 *
	 * @param type The component type identifier to check
	 * @return true if the type requires upgrades, false otherwise
	 */
	public static boolean requiresUpgrades(OpenIdentifier type) {
		return type.equals(EXTENSIBLE_PART) ||
				type.equals(EXTENSIBLE_EQUIPMENT) ||
				type.equals(STRUCTURED_EXTENSIBLE_PART) ||
				type.equals(STRUCTURED_EXTENSIBLE_EQUIPMENT);
	}

	/**
	 * Determines if a component type requires structure capability.
	 * <p>
	 * Used for validation and constructor logic.
	 *
	 * @param type The component type identifier to check
	 * @return true if the type requires structure, false otherwise
	 */
	public static boolean requiresStructure(OpenIdentifier type) {
		return type.equals(STRUCTURED_PART) ||
				type.equals(STRUCTURED_EQUIPMENT) ||
				type.equals(STRUCTURED_EXTENSIBLE_PART) ||
				type.equals(STRUCTURED_EXTENSIBLE_EQUIPMENT);
	}
}
