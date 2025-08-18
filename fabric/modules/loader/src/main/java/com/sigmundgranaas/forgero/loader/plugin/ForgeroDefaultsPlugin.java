package com.sigmundgranaas.forgero.loader.plugin;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.loader.property.bettercombat.BetterCombatIdentifierProperty;
import com.sigmundgranaas.forgero.loader.property.namereplacement.NameReplacementProperty;
import com.sigmundgranaas.forgero.loader.property.tooltip.TooltipProperty;


/**
 * A Forgero data plugin that registers all the default custom properties.
 * This demonstrates how the modular property system is intended to be used.
 */
public class ForgeroDefaultsPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		// Register a builder for the tooltip property codec.
		// The builder function receives a supplier for the ConditionCodec and returns the final property codec.
		// This lazy-evaluation approach ensures that the ConditionCodec is fully configured with all
		// custom conditions from other plugins before our property codec is created.
		context.registerPropertyCodec(
				TooltipProperty.KEY_ID.toString(),
				conditionCodecSupplier -> ListCodecWrapper.of(TooltipProperty.codec(conditionCodecSupplier.get()))
		);

		context.registerPropertyCodec(
				NameReplacementProperty.KEY_ID.toString(),
				conditionCodecSupplier -> ListCodecWrapper.of(NameReplacementProperty.codec(conditionCodecSupplier.get()))
		);

		context.registerPropertyCodec(
				BetterCombatIdentifierProperty.KEY_ID.toString(),
				conditionCodecSupplier -> ListCodecWrapper.of(BetterCombatIdentifierProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero-defaults-properties";
	}
}
