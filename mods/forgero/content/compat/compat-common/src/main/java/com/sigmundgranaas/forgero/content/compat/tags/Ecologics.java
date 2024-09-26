package com.sigmundgranaas.forgero.content.compat.tags;

public class Ecologics extends CommonTagGenerator {
	protected Ecologics() {
		super("ecologics");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("coconut_planks");
		registerCommonItemTag("walnut_planks");
	}
}
