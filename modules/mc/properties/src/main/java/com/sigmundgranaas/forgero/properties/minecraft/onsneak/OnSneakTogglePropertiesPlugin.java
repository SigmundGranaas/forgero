package com.sigmundgranaas.forgero.properties.minecraft.onsneak;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

/**
 * Plugin for registering On-Sneak-Toggle properties with the Forgero system.
 * This enables active ability effects that trigger when the wielder starts sneaking.
 */
public class OnSneakTogglePropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		// Register the compile pass so terminals pre-compile this property at construction.
		CompilerPasses.register(OnSneakToggleProperty.KEY, OnSneakToggleProperty.Engine::new);
		context.registerPropertyCodec(
				OnSneakToggleProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnSneakToggleProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-sneak-toggle-properties";
	}
}
