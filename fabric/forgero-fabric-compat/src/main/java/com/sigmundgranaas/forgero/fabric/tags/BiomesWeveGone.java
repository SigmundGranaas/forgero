package com.sigmundgranaas.forgero.fabric.tags;

public class BiomesWeveGone extends CommonTagGenerator {

	protected BiomesWeveGone() {
		super("biomeswevegone");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("aspen_planks");
		registerCommonItemTag("baobab_planks");
		registerCommonItemTag("cypress_planks");
		registerCommonItemTag("fir_planks");
		registerCommonItemTag("mahogany_planks");
		registerCommonItemTag("maple_planks");
		registerCommonItemTag("palm_planks");
		registerCommonItemTag("pine_planks");
		registerCommonItemTag("redwood_planks");
		registerCommonItemTag("willow_planks");
	}
}
