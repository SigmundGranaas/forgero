package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class NaturesSpirit {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/cypress_planks"), JTag.tag().add(new Identifier("mythicmetals:cypress_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/joshua_planks"), JTag.tag().add(new Identifier("mythicmetals:joshua_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/larch_planks"), JTag.tag().add(new Identifier("mythicmetals:larch_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/maple_planks"), JTag.tag().add(new Identifier("mythicmetals:maple_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/redwood_planks"), JTag.tag().add(new Identifier("mythicmetals:redwood_planks")));
	}
}
