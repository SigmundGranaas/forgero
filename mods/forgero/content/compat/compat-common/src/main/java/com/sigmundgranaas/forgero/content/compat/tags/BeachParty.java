package com.sigmundgranaas.forgero.content.compat.tags;

public class BeachParty extends CommonTagGenerator {
	protected BeachParty() {
		super("beachparty");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("palm_planks");
	}
}
