package com.sigmundgranaas.forgero.properties.plugin;

import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierSlot;
import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;

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

		// Register the StatusModifierSlot type for status modifiers (sharp, durable, broken, etc.)
		context.registerSlotCodec(
				StatusModifierSlot.TYPE,
				StatusModifierSlot.CODEC
		);
	}

	@Override
	public String getId() {
		return "forgero-core-slot-types";
	}
}
