package com.sigmundgranaas.forgero.content.compat.tags;

public class BountifulFares extends CommonTagGenerator {
	protected BountifulFares() {
		super("bountifulfares");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("walnut_planks");
	}
}
