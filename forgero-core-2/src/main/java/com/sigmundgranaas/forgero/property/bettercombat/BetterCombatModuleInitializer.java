package com.sigmundgranaas.forgero.property.bettercombat;


import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;

/**
 * Initializes the Better Combat Identifier property module by registering its components with the core registry.
 * This static method should be called once during the overall application startup.
 */
public class BetterCombatModuleInitializer {
	private BetterCombatModuleInitializer() {}

	public static void initialize() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		registry.registerPropertyBuilder(new BetterCombatIdentifierPropertyBuilder());
		registry.registerDataTypeEngine(new BetterCombatIdentifierEngine());
	}
}
