package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;

public class BlockBreakingPropertiesPlugin implements DataPlugin, PostLoadPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				BlockBreakingProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(BlockBreakingProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public void onDataLoaded(DataLoadingContext context) {
		BlockBreakingManager.initialize(context.getConverter(), context.getResolver());
	}

	@Override
	public String getId() {
		return "forgero:block-breaking-properties";
	}
}
