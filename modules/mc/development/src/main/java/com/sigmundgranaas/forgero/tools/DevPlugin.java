package com.sigmundgranaas.forgero.tools;


import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevPlugin implements PostLoadPlugin {
	public static final Logger LOGGER = LoggerFactory.getLogger(DevPlugin.class);

	@Override
	public String getId() {
		return "forgero:tools-plugin";
	}

	@Override
	public void onDataLoaded(DataLoadingContext context) {
		ComponentSlottingHandler slottingHandler = new ComponentSlottingHandler(context.getConverter());
		UseItemCallback.EVENT.register(slottingHandler::handle);
		LOGGER.info("Registered component slotting handler for item use events.");
	}
}
