package com.sigmundgranaas.forgero.properties.minecraft.onequip;

import com.sigmundgranaas.forgero.common.api.DataPlugin;
import com.sigmundgranaas.forgero.common.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.core.property.compiled.CompilerPasses;

/**
 * Registers the {@code forgero:on_equip} and {@code forgero:on_unequip} properties (Phase 5: set bonuses).
 */
public class OnEquipPropertiesPlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		CompilerPasses.register(OnEquipProperty.KEY, OnEquipProperty.Engine::new);
		context.registerPropertyCodec(
				OnEquipProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnEquipProperty.codec(conditionCodecSupplier.get()))
		);

		CompilerPasses.register(OnUnequipProperty.KEY, OnUnequipProperty.Engine::new);
		context.registerPropertyCodec(
				OnUnequipProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnUnequipProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-equip-properties";
	}
}
