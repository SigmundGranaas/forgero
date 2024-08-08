package com.sigmundgranaas.forgero.fabric.tags;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public class ModernIndustrialization {
	public static void generateTags() {
		RESOURCE_PACK.addTag(new Identifier("c", "items/bronze_ingot"), JTag.tag().add(new Identifier("modern_industrialization:bronze_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/steel_ingot"), JTag.tag().add(new Identifier("modern_industrialization:steel_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/aluminum_ingot"), JTag.tag().add(new Identifier("modern_industrialization:aluminum_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/lead_ingot"), JTag.tag().add(new Identifier("modern_industrialization:lead_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/invar_ingot"), JTag.tag().add(new Identifier("modern_industrialization:invar_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/nickel_ingot"), JTag.tag().add(new Identifier("modern_industrialization:nickel_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/silver_ingot"), JTag.tag().add(new Identifier("modern_industrialization:silver_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/titanium_ingot"), JTag.tag().add(new Identifier("modern_industrialization:titanium_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/electrum_ingot"), JTag.tag().add(new Identifier("modern_industrialization:electrum_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/chromium_ingot"), JTag.tag().add(new Identifier("modern_industrialization:chromium_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/platinum_ingot"), JTag.tag().add(new Identifier("modern_industrialization:platinum_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/iridium_ingot"), JTag.tag().add(new Identifier("modern_industrialization:iridium_ingot")));
		RESOURCE_PACK.addTag(new Identifier("c", "items/tungsten_ingot"), JTag.tag().add(new Identifier("modern_industrialization:tungsten_ingot")));
	}
}
