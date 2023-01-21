package com.sigmundgranaas.forgero.core.configuration;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ForgeroConfiguration implements ForgeroConfigurationData {
	@NotNull
	@SerializedName("disabled_resources")
	public List<String> disabledResources = List.of("forgero:diamond-sacrificial_dagger_blade");

	@NotNull
	@SerializedName("disabled_packs")
	public List<String> disabledPacks = new ArrayList<>();

	@NotNull
	@SerializedName("disable_vanilla_recipes")
	public Boolean disableVanillaRecipes = false;

	@NotNull
	@SerializedName("convert_vanilla_recipes_to_forgero_tools")
	public Boolean convertVanillaRecipesToForgeroTools = false;

	@NotNull
	@SerializedName("enable_repair_kits")
	public Boolean enableRepairKits = true;

	@NotNull
	@SerializedName("resource_logging")
	public Boolean resourceLogging = true;

	@NotNull
	@SerializedName("log_disabled_packages")
	public Boolean logDisabledPackages = false;
}
