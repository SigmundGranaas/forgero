package com.sigmundgranaas.forgero.fabric.tags;

public class MythicMetalsCommons extends CommonTagGenerator {
	protected MythicMetalsCommons() {
		super("mythicmetals");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("silver_ingot");
		registerCommonItemTag("bronze_ingot");
		registerCommonItemTag("steel_ingot");
	}
}
