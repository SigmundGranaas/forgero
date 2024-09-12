package com.sigmundgranaas.forgero.trident;

import com.sigmundgranaas.forgero.core.registry.RegistryFactory;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.fabric.item.ItemGroupRegisters;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;
import com.sigmundgranaas.forgero.trident.item.DynamicTridentRegistrationHandler;

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.*;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.register;

public class ForgeroTridentInitializer implements ForgeroPreInitializationEntryPoint {
	@Override
	public void onPreInitialization() {

		var settingRegistry = ItemRegistries.SETTING_PROCESSOR;
		var groupRegistry = ItemRegistries.GROUP_CONVERTER;

		var factory = new RegistryFactory<>(groupRegistry);

		var baseConverter = BuildableStateConverter.builder()
				.group(factory::convert)
				.settings(settingProcessor(settingRegistry))
				.item(defaultItem)
				.priority(0)
				.build();

		register(ItemRegistries.STATE_CONVERTER, new DynamicTridentRegistrationHandler(baseConverter));
	}
}

