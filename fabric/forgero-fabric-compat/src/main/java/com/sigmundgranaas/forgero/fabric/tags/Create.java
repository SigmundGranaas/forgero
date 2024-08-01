package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class Create {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/brass_ingot"), JTag.tag().add(new Identifier("create:brass_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/zinc_ingot"), JTag.tag().add(new Identifier("create:zinc_ingot")));
	}
}
