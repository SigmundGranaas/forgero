package com.sigmundgranaas.forgero.render.plugin;

import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;
import com.sigmundgranaas.forgero.render.ForgeroClient;

public class ForgeroRenderPlugin implements PostLoadPlugin {
	@Override
	public void onDataLoaded(DataLoadingContext context) {

	}

	@Override
	public String getId() {
		return "forgero-render-plugin";
	}
}
