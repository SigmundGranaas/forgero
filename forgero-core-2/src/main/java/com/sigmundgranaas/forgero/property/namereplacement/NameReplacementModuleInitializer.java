package com.sigmundgranaas.forgero.property.namereplacement;

import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;

/**
 * Initializes the Name Replacement property module by registering its components with the core registry.
 * This static method should be called once during the overall application startup.
 */
public class NameReplacementModuleInitializer {
	private NameReplacementModuleInitializer() {} // Prevent instantiation

	public static void initialize() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		registry.registerPropertyBuilder(new NameReplacementPropertyBuilder());
		registry.registerDataTypeEngine(new NameReplacementEngine());
	}
}
