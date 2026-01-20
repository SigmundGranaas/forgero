package com.sigmundgranaas.forgero.fabric.tags;

public class BountifulFares extends CommonTagGenerator {
	protected BountifulFares() {
		super("bountifulfares");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("walnut_planks");
	}
}
