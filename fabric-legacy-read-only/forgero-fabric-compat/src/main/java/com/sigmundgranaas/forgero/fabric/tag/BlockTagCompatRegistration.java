package com.sigmundgranaas.forgero.fabric.tag;

import static com.sigmundgranaas.forgero.core.Forgero.LOGGER;

import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.fabric.resources.ForgeroResourceGenerator;

import net.fabricmc.loader.api.SemanticVersion;

import net.minecraft.util.Identifier;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;

public class BlockTagCompatRegistration {
	// Namespaces
	public static final String MINECRAFT_NAMESPACE = "minecraft";
	public static final String COMMON_NAMESPACE = "c";
	public static final String BIOMES_YOULL_GO_NAMESPACE = "byg";
	public static final String REGIONS_UNEXPLORED_NAMESPACE = "regions-unexplored";
	// Blocks
	public static final String CHERRY_PLANKS = "cherry_planks";
	public static final String BAMBOO_PLANKS = "bamboo_planks";

	public static void register() {
		registerCherryPlanksTag();
		registerBambooPlanksTag();
	}

	private static int checkIfMinecraftVersionIsEqualOrNewer(String version) {
		var minecraftModContainer = FabricLoader.getInstance().getModContainer(MINECRAFT_NAMESPACE);
		if (minecraftModContainer.isEmpty()) {
			LOGGER.error("Couldn't parse Minecraft version (Minecraft mod container is empty).");
			return -1;
		}

		var minecraftVersion = minecraftModContainer.get().getMetadata().getVersion();
		try {
			return minecraftVersion.compareTo(SemanticVersion.parse(version));
		} catch (VersionParsingException e) {
			LOGGER.error("Couldn't parse Minecraft version: {}. See stacktrace below:\n{}", minecraftVersion, e);
			return -1;
		}
	}

	private static void registerCherryPlanksTag() {
		var tagBuilder = TagBuilder.items(COMMON_NAMESPACE + ":" + CHERRY_PLANKS);

		if (checkIfMinecraftVersionIsEqualOrNewer("1.20") >= 0) {
			tagBuilder.add(new Identifier(MINECRAFT_NAMESPACE, CHERRY_PLANKS).toString());
		}

		if (FabricLoader.getInstance().isModLoaded(BIOMES_YOULL_GO_NAMESPACE)) {
			tagBuilder.add(new Identifier(BIOMES_YOULL_GO_NAMESPACE, CHERRY_PLANKS).toString());
		}

		if (FabricLoader.getInstance().isModLoaded(REGIONS_UNEXPLORED_NAMESPACE)) {
			tagBuilder.add(new Identifier(REGIONS_UNEXPLORED_NAMESPACE, CHERRY_PLANKS).toString());
		}

		ForgeroResourceGenerator.getDynamicPack().addTag(tagBuilder);
	}

	private static void registerBambooPlanksTag() {
		var tagBuilder = TagBuilder.items(COMMON_NAMESPACE + ":" + BAMBOO_PLANKS);

		if (checkIfMinecraftVersionIsEqualOrNewer("1.20") >= 0) {
			tagBuilder.add(new Identifier(MINECRAFT_NAMESPACE, BAMBOO_PLANKS).toString());
		}

		ForgeroResourceGenerator.getDynamicPack().addTag(tagBuilder);
	}
}
