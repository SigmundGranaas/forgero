package com.sigmundgranaas.forgero.core.configuration;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ForgeroConfiguration implements ForgeroConfigurationData {
	@NotNull
	public List<String> disabledResources = List.of("forgero:diamond-sacrificial_dagger_blade");

	@NotNull
	public List<String> disabledPacks = Collections.emptyList();

	@NotNull
	public Boolean disableVanillaRecipes = false;

	@NotNull
	public Boolean enableCustomRecipeDeletion = true;

	@NotNull
	public Boolean disableVanillaLoot = false;

	@NotNull
	public Boolean disableVanillaTools = false;

	@NotNull
	public Boolean convertVanillaRecipesToForgeroTools = false;

	@NotNull
	public Boolean convertVanillaToolLoot = false;

	@NotNull
	public Boolean enableUnbreakableTools = false;

	@NotNull
	public Boolean enableRepairKits = true;

	@NotNull
	public Boolean resourceLogging = true;

	@NotNull
	public Boolean logDisabledPackages = false;

	@NotNull
	public Integer baseSoulLevelRequirement = 1000;

	@NotNull
	public Boolean useEntityAttributes = true;

	@NotNull
	public Boolean hideRarity = true;

	@NotNull
	public Boolean exportGeneratedTextures = false;

	@NotNull
	public Boolean showAttributeDifference = true;

	@NotNull
	public Boolean enableRecipesForAllSchematics = false;
	@NotNull
	public Integer weightMiningSpeedReductionScaler = 0;
	@NotNull
	public Integer weightAttackSpeedReductionScaler = 100;
	@NotNull
	public Boolean weightReducesAttackSpeed = true;
	@NotNull
	public Boolean weightReducesMiningSpeed = true;
}
