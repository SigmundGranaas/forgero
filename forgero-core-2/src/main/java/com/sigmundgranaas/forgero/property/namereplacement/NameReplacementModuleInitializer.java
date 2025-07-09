package com.sigmundgranaas.forgero.property.namereplacement;

import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;

/**
 * Initializes the Name Replacement property module by registering its components with the core registry.
 * This static method should be called once during the overall application startup.
 */
public class NameReplacementModuleInitializer {
	private NameReplacementModuleInitializer() {} // Prevent instantiation

	public static void initialize() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		ConditionMapper conditionMapper = new ConditionMapper(CodecConstants.IDENTIFIER_FACTORY);
		registry.registerPropertyCodec(new NameReplacementCodec(conditionMapper));
		registry.registerDataTypeEngine(new NameReplacementEngine());
	}
}
