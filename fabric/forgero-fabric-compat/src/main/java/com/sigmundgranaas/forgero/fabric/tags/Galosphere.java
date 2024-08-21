package com.sigmundgranaas.forgero.fabric.tags;

public class Galosphere extends CommonTagGenerator {
	protected Galosphere() {
		super("galosphere");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("silver_ingots");
	}
}
