package com.sigmundgranaas.forgero.fabric.tags;

public class NaturesSpirit extends CommonTagGenerator {
	protected NaturesSpirit() {
		super("natures_spirit");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("cypress_planks");
		registerCommonItemTag("joshua_planks");
		registerCommonItemTag("larch_planks");
		registerCommonItemTag("maple_planks");
		registerCommonItemTag("redwood_planks");
		registerCommonItemTag("willow_planks");
		registerCommonItemTag("coconut_planks");
	}
}
