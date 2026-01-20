package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;

public class BlockBreakingPropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
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
