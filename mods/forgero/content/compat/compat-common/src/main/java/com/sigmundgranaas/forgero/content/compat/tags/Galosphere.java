package com.sigmundgranaas.forgero.content.compat.tags;

public class Galosphere extends CommonTagGenerator {
	protected Galosphere() {
		super("galosphere");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("silver_ingot");
	}
}
