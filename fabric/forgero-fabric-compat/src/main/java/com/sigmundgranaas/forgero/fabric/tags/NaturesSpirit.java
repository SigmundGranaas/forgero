package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class NaturesSpirit {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/cypress_planks"), JTag.tag().add(new Identifier("natures_spirit:cypress_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/joshua_planks"), JTag.tag().add(new Identifier("natures_spirit:joshua_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/larch_planks"), JTag.tag().add(new Identifier("natures_spirit:larch_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/maple_planks"), JTag.tag().add(new Identifier("natures_spirit:maple_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/redwood_planks"), JTag.tag().add(new Identifier("natures_spirit:redwood_planks")));
	}
}
