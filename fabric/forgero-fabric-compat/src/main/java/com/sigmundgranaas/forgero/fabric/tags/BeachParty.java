package com.sigmundgranaas.forgero.fabric.tags;

public class BeachParty extends CommonTagGenerator {
	protected BeachParty() {
		super("beachparty");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("palm_planks");
	}
}
