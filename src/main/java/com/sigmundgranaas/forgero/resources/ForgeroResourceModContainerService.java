package com.sigmundgranaas.forgero.resources;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.List;


record ForgeroResourceModContainerService() {
    public static String FORGERO_RESOURCE_IDENTIFIER = "forgeroResource";

    public List<ModContainer> getContainers() {
        return FabricLoader.getInstance().getAllMods().stream().toList();
    }

    public List<ModContainerFileLoader> getForgeroResourceContainers() {
        return getContainers().stream()
                .filter(container -> container.getMetadata().getName().equals("Minecraft") || container.getMetadata().containsCustomValue(FORGERO_RESOURCE_IDENTIFIER))
                .filter(container -> container.getMetadata().getName().equals("Minecraft") || container.getMetadata().getCustomValue(FORGERO_RESOURCE_IDENTIFIER).getAsBoolean())
                .map(ModContainerFileLoader::new).toList();
    }
}