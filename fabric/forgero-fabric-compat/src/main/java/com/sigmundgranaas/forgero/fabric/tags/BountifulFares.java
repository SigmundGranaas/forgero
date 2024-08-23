package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class BountifulFares extends CommonTagGenerator {
	protected BountifulFares() {
		super("bountifulfares");
	}

	@Override
	public void addTags() {
		registerCommonItemTag("walnut_planks");
	}
}
