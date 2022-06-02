package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.resources.loader.ModContainerFileLoader;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class FabricModFileLoader {
    private final List<ModContainerFileLoader> containers;

    public FabricModFileLoader() {
        this.containers = new ModContainerService().getForgeroResourceContainers();
    }

    public boolean resourceExists(String resource) {
        return containers.stream().anyMatch(container -> container.containsResource(resource));
    }

    public List<Path> getResourcesFromFolder(String folder) {
        return containers.stream().map(container -> container.getResourcesInFolder(folder)).flatMap(Collection::stream).toList();
    }

    public Optional<InputStream> loadFileFromMods(String resource) {
        if (resourceExists(resource)) {
            return containers.stream().map(container -> container.loadResource(resource)).flatMap(Optional::stream).findFirst();
        }
//var result = containers.stream()
        return Optional.empty();
    }
}
