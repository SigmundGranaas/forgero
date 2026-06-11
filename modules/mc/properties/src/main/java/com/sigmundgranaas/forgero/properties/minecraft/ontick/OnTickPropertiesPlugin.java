package com.sigmundgranaas.forgero.properties.minecraft.ontick;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

public class OnTickPropertiesPlugin implements DataPlugin {
	@Override
	public void register(PluginRegistrationContext context) {
		// Register the compile pass so terminals pre-compile this property at construction.
		CompilerPasses.register(OnTickProperty.KEY, OnTickProperty.Engine::new);
		context.registerPropertyCodec(
				OnTickProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnTickProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-tick-properties";
	}
}
