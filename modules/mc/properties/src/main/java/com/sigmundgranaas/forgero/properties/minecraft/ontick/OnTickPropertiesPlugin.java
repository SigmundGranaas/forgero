package com.sigmundgranaas.forgero.properties.minecraft.ontick;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;

public class OnTickPropertiesPlugin implements DataPlugin {
	@Override
	public void register(PluginRegistrationContext context) {
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
