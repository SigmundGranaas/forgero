package com.sigmundgranaas.forgero.content.compat.tags;

public class Create extends CommonTagGenerator {
	protected Create() {
		super("create");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("brass_ingot");
		registerCommonItemTag("zinc_ingot");
	}
}
