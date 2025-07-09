package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.data.mapper.api.PropertyBuilder;

import java.util.List;

/**
 * A central registry for all property-related components (PropertyBuilders and DataTypeEngines).
 * This allows different modules to register their custom property types without
 * the core application needing direct knowledge of their concrete implementations.
 * It follows the singleton pattern to ensure a single point of truth for registered properties.
 */
public interface PropertyRegistry {
	/**
	 * Registers a PropertyBuilder for a specific custom property type.
	 * This method is intended to be called by modules during their initialization phase
	 * to make their property parsing capabilities known to the system.
	 *
	 * @param builder The PropertyBuilder to register.
	 */
	void registerPropertyBuilder(PropertyBuilder builder);

	/**
	 * Registers a DataTypeEngine for a specific custom property type.
	 * This method is intended to be called by modules during their initialization phase
	 * to make their property resolution capabilities known to the system.
	 *
	 * @param engine The DataTypeEngine to register.
	 */
	void registerDataTypeEngine(DataTypeEngine<?, ?> engine);

	/**
	 * Retrieves an immutable list of all registered PropertyBuilders.
	 * This list is typically used by the ComponentMapper to parse all known property types.
	 *
	 * @return An unmodifiable list of all registered PropertyBuilders.
	 */
	List<PropertyBuilder> getPropertyBuilders();

	/**
	 * Retrieves an immutable list of all registered DataTypeEngines.
	 * This list is typically used by the ResolverEngine to resolve all known property types.
	 *
	 * @return An unmodifiable list of all registered DataTypeEngines.
	 */
	List<DataTypeEngine<?, ?>> getDataTypeEngines();

	/**
	 * Gets the singleton instance of the PropertyRegistry.
	 * This is the primary way for modules to interact with the registry.
	 *
	 * @return The PropertyRegistry singleton instance.
	 */
	static PropertyRegistry getInstance() {
		return DefaultPropertyRegistry.INSTANCE;
	}

	/**
	 * Resets the registry. This method is primarily for testing purposes
	 * to ensure a clean state between tests. In a production environment,
	 * the registry is typically initialized once.
	 */
	void reset();
}
