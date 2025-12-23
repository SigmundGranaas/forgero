package com.sigmundgranaas.forgero.properties.minecraft.onkill;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;

/**
 * Plugin for registering On-Kill properties with the Forgero system.
 * This enables victory reward effects that trigger when the wielder kills an entity.
 */
public class OnKillPropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				OnKillProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnKillProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-kill-properties";
	}
}
