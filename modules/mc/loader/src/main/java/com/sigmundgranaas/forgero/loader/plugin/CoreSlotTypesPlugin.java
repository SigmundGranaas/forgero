package com.sigmundgranaas.forgero.loader.plugin;

import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;

/**
 * A Forgero data plugin that registers all core slot type codecs.
 * This plugin registers the built-in slot types that come with Forgero core.
 */
public class CoreSlotTypesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		registerSlotCodecs(context);
	}

	private void registerSlotCodecs(PluginRegistrationContext context) {
		// Register the core ComponentUpgradeSlot type
		context.registerSlotCodec(
				ComponentUpgradeSlot.TYPE,
				ComponentUpgradeSlot.CODEC
		);
	}

	@Override
	public String getId() {
		return "forgero-core-slot-types";
	}
}
