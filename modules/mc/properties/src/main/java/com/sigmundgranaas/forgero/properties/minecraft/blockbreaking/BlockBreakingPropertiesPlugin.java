package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

public class BlockBreakingPropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		// Register the compile pass so terminals pre-compile this property at construction.
		CompilerPasses.register(BlockBreakingProperty.KEY, BlockBreakingProperty.Engine::new);
		context.registerPropertyCodec(
				BlockBreakingProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(BlockBreakingProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:block-breaking-properties";
	}
}
