package com.sigmundgranaas.forgero.fabric.modmenu;

import com.sigmundgranaas.forgero.fabric.yacl.ForgeroYACLConfigScreenBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ForgeroModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ForgeroYACLConfigScreenBuilder::createScreen;
	}
}
