package com.sigmundgranaas.forgero.fabric.tags;

public class Create extends CommonTagGenerator {
	protected Create() {
		super("create");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("brass_ingot");
		registerCommonItemTag("zinc_ingot");
		registerCommonItemTag("limestone");
	}
}
