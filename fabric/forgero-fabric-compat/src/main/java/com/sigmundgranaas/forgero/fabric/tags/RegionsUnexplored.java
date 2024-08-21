package com.sigmundgranaas.forgero.fabric.tags;

public class RegionsUnexplored extends CommonTagGenerator {
	protected RegionsUnexplored() {
		super("regions_unexplored");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("cypress_planks");
		registerCommonItemTag("joshua_planks");
		registerCommonItemTag("larch_planks");
		registerCommonItemTag("maple_planks");
		registerCommonItemTag("redwood_planks");
		registerCommonItemTag("willow_planks");
		registerCommonItemTag("pine_planks");
		registerCommonItemTag("baobab_planks");
	}
}
