package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class BiomesWeveGone {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/aspen_planks"), JTag.tag().add(new Identifier("biomeswevegone:aspen_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/baobab_planks"), JTag.tag().add(new Identifier("biomeswevegone:baobab_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/cypress_planks"), JTag.tag().add(new Identifier("biomeswevegone:cypress_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/fir_planks"), JTag.tag().add(new Identifier("biomeswevegone:fir_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/mahogany_planks"), JTag.tag().add(new Identifier("biomeswevegone:mahogany_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/maple_planks"), JTag.tag().add(new Identifier("biomeswevegone:maple_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/palm_planks"), JTag.tag().add(new Identifier("biomeswevegone:palm_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/pine_planks"), JTag.tag().add(new Identifier("biomeswevegone:pine_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/redwood_planks"), JTag.tag().add(new Identifier("biomeswevegone:redwood_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/willow_planks"), JTag.tag().add(new Identifier("biomeswevegone:willow_planks")));
	}
}
