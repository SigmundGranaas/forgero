package com.sigmundgranaas.forgero.core.configuration;

import static com.sigmundgranaas.forgero.core.model.Strategy.PRE_BAKED;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.model.Strategy;
import org.jetbrains.annotations.NotNull;

public class ForgeroConfiguration implements ForgeroConfigurationData {
	// Main
	@NotNull
	public List<String> disabledResources = Collections.emptyList();
	@NotNull
	public List<String> disabledPacks = List.of(
			"forgero:extended-handles-deprecated",
			"forgero:extended-swords-deprecated",
			"forgero:extended-tools-deprecated",
			"forgero:material-deprecated",
			"forgero:trinkets-deprecated"
	);

	// Recipes, upgrades, and loot
	@NotNull
	public Boolean disableVanillaRecipes = false;
	@NotNull
	public Boolean enableCustomRecipeDeletion = true;
	@NotNull
	public Boolean enableRecipesForAllSchematics = false;
	@NotNull
	public Boolean enableUpgradeInCraftingTable = false;
	@NotNull
	public Boolean disableVanillaLoot = false;
	@NotNull
	public Boolean disableVanillaTools = false;
	@NotNull
	public Boolean convertVanillaRecipesToForgeroTools = false;
	@NotNull
	public Boolean convertVanillaToolLoot = false;

	// Repairing
	@NotNull
	public Boolean enableUnbreakableTools = false;
	@NotNull
	public Boolean enableRepairKits = true;

	// Attributes
	@NotNull
	public Integer baseSoulLevelRequirement = 1000;
	@NotNull
	public Boolean useEntityAttributes = true;
	@NotNull
	public Boolean showAttributeDifference = true;
	@NotNull
	public Boolean hideRarity = true;
	@NotNull
	public Boolean weightReducesAttackSpeed = true;
	@NotNull
	public Float minimumAttackSpeed = 0.5f;
	@NotNull
	public Boolean weightIncreasesHunger = false;
	@NotNull
	public Integer weightIncreasesHungerScalar = 10;
	@NotNull
	public Float weightIncreasesHungerBaseChance = 0.01f;
	@NotNull
	public Integer weightIncreasesHungerCenterPoint = 50;

	// Debug
	@NotNull
	public Boolean resourceLogging = true;
	@NotNull
	public Boolean debugLogging = false;
	@NotNull
	public Boolean logDisabledPackages = false;
	@NotNull
	public Boolean exportGeneratedTextures = false;

	// Performance
	@NotNull
	public Strategy modelStrategy = PRE_BAKED;
}
