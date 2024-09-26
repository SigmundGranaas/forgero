package com.sigmundgranaas.forgero.content.compat.tags;

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
