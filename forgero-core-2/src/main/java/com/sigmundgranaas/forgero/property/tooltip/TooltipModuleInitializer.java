package com.sigmundgranaas.forgero.property.tooltip;

import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;

public class TooltipModuleInitializer {
	private TooltipModuleInitializer() {}

	public static void initialize() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		registry.registerPropertyCodec(new TooltipCodec());
		registry.registerDataTypeEngine(new TooltipEngine());
	}
}
