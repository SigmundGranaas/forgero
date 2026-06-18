package com.sigmundgranaas.forgero.properties.minecraft.onblock;

import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

/**
 * Registers the {@code forgero:on_block} (shield parry) property (Phase 2: skill combat).
 */
public class OnBlockPropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		CompilerPasses.register(OnBlockProperty.KEY, OnBlockProperty.Engine::new);
		context.registerPropertyCodec(
				OnBlockProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnBlockProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-block-properties";
	}
}
