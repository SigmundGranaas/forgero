package com.sigmundgranaas.forgero.fabric.tag;

import static com.sigmundgranaas.forgero.fabric.resources.ARRPGenerator.RESOURCE_PACK;

import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

import net.fabricmc.loader.api.FabricLoader;

public class BlockTagCompatRegistration {
	public static final String MINECRAFT_NAMESPACE = "minecraft";
	public static final String COMMON_NAMESPACE = "c";
	public static final String BIOMES_YOULL_GO_NAMESPACE = "byg";
	public static final String REGIONS_UNEXPLORED_NAMESPACE = "regions-unexplored";

	public static void register() {
		registerCherryPlanksTag();
	}

	private static void registerCherryPlanksTag() {
		var cherryPlanksTag = new JTag();

		var minecraftModContainer = FabricLoader.getInstance().getModContainer(MINECRAFT_NAMESPACE);
		if (minecraftModContainer.isPresent() && minecraftModContainer.get().getMetadata().getVersion().getFriendlyString().contains(
				"1.20")) {
			cherryPlanksTag.add(new Identifier(MINECRAFT_NAMESPACE, "cherry_planks"));
		}

		if (FabricLoader.getInstance().isModLoaded(BIOMES_YOULL_GO_NAMESPACE)) {
			cherryPlanksTag.add(new Identifier(BIOMES_YOULL_GO_NAMESPACE, "cherry_planks"));
		}

		if (FabricLoader.getInstance().isModLoaded(REGIONS_UNEXPLORED_NAMESPACE)) {
			cherryPlanksTag.add(new Identifier(REGIONS_UNEXPLORED_NAMESPACE, "cherry_planks"));
		}

		RESOURCE_PACK.addTag(new Identifier(COMMON_NAMESPACE, "cherry_planks"), cherryPlanksTag);
	}
}
