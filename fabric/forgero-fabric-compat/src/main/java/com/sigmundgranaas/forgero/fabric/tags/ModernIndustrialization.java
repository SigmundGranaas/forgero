package com.sigmundgranaas.forgero.fabric.tags;

public class ModernIndustrialization extends CommonTagGenerator {
	protected ModernIndustrialization() {
		super("modernindustrialization","modern_industrialization");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("bronze_ingot");
		registerCommonItemTag("steel_ingot");
		registerCommonItemTag("aluminum_ingot");
		registerCommonItemTag("lead_ingot");
		registerCommonItemTag("invar_ingot");
		registerCommonItemTag("nickel_ingot");
		registerCommonItemTag("silver_ingot");
		registerCommonItemTag("titanium_ingot");
		registerCommonItemTag("electrum_ingot");
		registerCommonItemTag("chromium_ingot");
		registerCommonItemTag("platinum_ingot");
		registerCommonItemTag("iridium_ingot");
		registerCommonItemTag("tungsten_ingot");
	}
}
