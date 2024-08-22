package com.sigmundgranaas.forgero.fabric.tags;

public class BloomingNature extends CommonTagGenerator {
	protected BloomingNature() {
		super("bloomingnature");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("aspen_planks");
		registerCommonItemTag("baobab_planks");
		registerCommonItemTag("ebony_planks");
		registerCommonItemTag("fir_planks");
		registerCommonItemTag("larch_planks");
	}
}
