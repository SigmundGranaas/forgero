package com.sigmundgranaas.forgero.property.tooltip;

import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;

public class TooltipModuleInitializer {
	public static void initialize() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		ConditionMapper conditionMapper = new ConditionMapper(CodecConstants.IDENTIFIER_FACTORY);
		registry.registerPropertyCodec(new TooltipCodec(conditionMapper));
		registry.registerDataTypeEngine(new TooltipEngine());
	}
}
