package com.sigmundgranaas.forgero.core.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ForgeroConfiguration implements ForgeroConfigurationData {
    @NotNull
    public List<String> disabledResources = List.of("forgero:diamond-sacrificial_dagger_blade");

    @NotNull
    public List<String> disabledPacks = Collections.emptyList();

    @NotNull
    public Boolean disableVanillaRecipes = false;

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

}
