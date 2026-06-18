package com.sigmundgranaas.forgero.properties.minecraft.oncrit;

import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

/**
 * Registers the {@code forgero:on_crit} property (Phase 2: skill combat).
 */
public class OnCritPropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		CompilerPasses.register(OnCritProperty.KEY, OnCritProperty.Engine::new);
		context.registerPropertyCodec(
				OnCritProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnCritProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-crit-properties";
	}
}
