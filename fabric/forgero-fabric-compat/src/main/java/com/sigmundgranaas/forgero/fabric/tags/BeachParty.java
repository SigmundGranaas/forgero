package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class BeachParty {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/palm_planks"), JTag.tag().add(new Identifier("beachparty:palm_planks")));
	}
}
