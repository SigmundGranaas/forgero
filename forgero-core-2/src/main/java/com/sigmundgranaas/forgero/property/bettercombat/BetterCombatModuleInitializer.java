package com.sigmundgranaas.forgero.property.bettercombat;


import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;

/**
 * Initializes the Better Combat Identifier property module by registering its components with the core registry.
 * This static method should be called once during the overall application startup.
 */
public class BetterCombatModuleInitializer {
	private BetterCombatModuleInitializer() {}

	public static void initialize() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		// Dependencies can be fetched from a central DI container or created here
		ConditionMapper conditionMapper = new ConditionMapper(CodecConstants.IDENTIFIER_FACTORY);
		registry.registerPropertyCodec(new BetterCombatIdentifierCodec(conditionMapper));
		registry.registerDataTypeEngine(new BetterCombatIdentifierEngine());
	}
}
