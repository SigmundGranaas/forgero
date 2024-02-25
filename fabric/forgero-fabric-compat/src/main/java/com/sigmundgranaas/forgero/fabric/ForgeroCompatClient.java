package com.sigmundgranaas.forgero.fabric;


import com.sigmundgranaas.forgero.fabric.tag.BlockTagCompatRegistration;

import net.fabricmc.api.ClientModInitializer;

public class ForgeroCompatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		BlockTagCompatRegistration.register();
	}
}
