package com.sigmundgranaas.forgero.properties.minecraft.ondamage;

import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;

/**
 * Plugin for registering On-Damage-Received properties with the Forgero system.
 * This enables defensive effects that trigger when the wielder takes damage.
 */
public class OnDamageReceivedPropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				OnDamageReceivedProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnDamageReceivedProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-damage-received-properties";
	}
}
