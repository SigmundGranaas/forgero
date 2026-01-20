package com.sigmundgranaas.forgero.fabric.tags;

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
