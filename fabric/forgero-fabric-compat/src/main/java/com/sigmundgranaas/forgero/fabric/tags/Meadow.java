package com.sigmundgranaas.forgero.fabric.tags;

public class Meadow extends CommonTagGenerator {
	protected Meadow() {
		super("meadow");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("limestone");
		registerCommonItemTag("pine_planks");
	}
}
