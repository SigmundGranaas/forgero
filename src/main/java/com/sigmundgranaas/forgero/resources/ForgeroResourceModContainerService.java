package com.sigmundgranaas.forgero.resources;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public record ForgeroResourceModContainerService() {
    public static String FORGERO_RESOURCE_IDENTIFIER = "forgeroResource";

    public List<ModContainer> getContainers() {
        return FabricLoader.getInstance().getAllMods().stream().toList();
    }

    public List<String> getAllMods() {
        return FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).toList();
    }

    public Set<String> getAllModsAsSet() {
        return FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).collect(Collectors.toSet());
    }

    public List<ModContainerFileLoader> getForgeroResourceContainers() {
        return getContainers().stream()
                .filter(container -> container.getMetadata().getName().equals("Minecraft") || container.getMetadata().containsCustomValue(FORGERO_RESOURCE_IDENTIFIER))
                .filter(container -> container.getMetadata().getName().equals("Minecraft") || container.getMetadata().getCustomValue(FORGERO_RESOURCE_IDENTIFIER).getAsBoolean())
                .map(ModContainerFileLoader::new).toList();
    }
}