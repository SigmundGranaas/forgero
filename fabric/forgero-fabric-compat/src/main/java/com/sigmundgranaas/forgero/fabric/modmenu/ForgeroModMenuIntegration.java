package com.sigmundgranaas.forgero.fabric.modmenu;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.sigmundgranaas.forgero.fabric.yacl.ForgeroYACLConfigScreenBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ForgeroModMenuIntegration implements ModMenuApi {
	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of("forgero", ForgeroYACLConfigScreenBuilder::createScreen);
	}
}
