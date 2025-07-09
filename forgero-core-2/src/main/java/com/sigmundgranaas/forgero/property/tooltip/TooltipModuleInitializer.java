package com.sigmundgranaas.forgero.property.tooltip;

import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;

public class TooltipModuleInitializer {
	public static void initialize() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		registry.registerPropertyBuilder(new TooltipPropertyBuilder());
		registry.registerDataTypeEngine(new TooltipEngine());
	}
}
