package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

public class BountifulFares extends CommonTagGenerator {
	protected BountifulFares() {
		super("bountifulfares");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("walnut_planks");
	}
}
