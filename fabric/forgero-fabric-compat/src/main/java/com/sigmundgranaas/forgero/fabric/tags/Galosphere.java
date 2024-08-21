package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class Galosphere {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/silver_ingot"), JTag.tag().add(new Identifier("galosphere:silver_ingot")));
	}
}
