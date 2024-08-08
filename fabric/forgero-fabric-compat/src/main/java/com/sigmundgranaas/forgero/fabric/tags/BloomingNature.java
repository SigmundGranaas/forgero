package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class BloomingNature {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/aspen_planks"), JTag.tag().add(new Identifier("bloomingnature:aspen_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/baobab_planks"), JTag.tag().add(new Identifier("bloomingnature:baobab_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/ebony_planks"), JTag.tag().add(new Identifier("bloomingnature:ebony_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/fir_planks"), JTag.tag().add(new Identifier("bloomingnature:fir_planks")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/larch_planks"), JTag.tag().add(new Identifier("bloomingnature:larch_planks")));
	}
}
