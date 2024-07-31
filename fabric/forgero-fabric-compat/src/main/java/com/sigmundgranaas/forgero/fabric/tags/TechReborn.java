package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class TechReborn {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/zinc_ingot"), JTag.tag().add(new Identifier("techreborn:zinc_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/steel_ingot"), JTag.tag().add(new Identifier("techreborn:steel_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/lead_ingot"), JTag.tag().add(new Identifier("techreborn:lead_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/nickel_ingot"), JTag.tag().add(new Identifier("techreborn:nickel_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/bronze_ingot"), JTag.tag().add(new Identifier("techreborn:bronze_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/brass_ingot"), JTag.tag().add(new Identifier("techreborn:brass_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/invar_ingot"), JTag.tag().add(new Identifier("techreborn:invar_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/silver_ingot"), JTag.tag().add(new Identifier("techreborn:silver_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/electrum_ingot"), JTag.tag().add(new Identifier("techreborn:electrum_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/aluminum_ingot"), JTag.tag().add(new Identifier("techreborn:aluminum_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/titanium_ingot"), JTag.tag().add(new Identifier("techreborn:titanium_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/chromium_ingot"), JTag.tag().add(new Identifier("techreborn:chromium_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/iridium_ingot"), JTag.tag().add(new Identifier("techreborn:iridium_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/tungsten_ingot"), JTag.tag().add(new Identifier("techreborn:tungsten_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/platinum_ingot"), JTag.tag().add(new Identifier("techreborn:platinum_ingot")));
	}
}
