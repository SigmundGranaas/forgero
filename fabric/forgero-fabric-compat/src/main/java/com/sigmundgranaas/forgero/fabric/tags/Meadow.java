package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class Meadow {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/limestone"), JTag.tag().add(new Identifier("meadow:limestone")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/pine_planks"), JTag.tag().add(new Identifier("meadow:pine_planks")));
	}
}
