package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class BountifulFares {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/walnut_planks"), JTag.tag().add(new Identifier("bountifulfares:walnut_planks")));
	}
}
