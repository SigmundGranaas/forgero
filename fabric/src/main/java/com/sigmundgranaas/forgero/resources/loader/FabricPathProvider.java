package com.sigmundgranaas.forgero.resources.loader;

import com.sigmundgranaas.forgerocore.resource.ForgeroResourceType;
import com.sigmundgranaas.forgerocore.resource.ResourcePathProvider;
import com.sigmundgranaas.forgero.resources.ModContainerService;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class FabricPathProvider implements ResourcePathProvider {
    public static FabricPathProvider PROVIDER = new FabricPathProvider();

    @Override
    public List<Path> getPaths(ForgeroResourceType type) {
        var resourceContainers = new ModContainerService().getForgeroResourceContainers();
        return resourceContainers.stream()
                .map(container -> container.getResourcesInFolder(ResourceLocations.getPathFromType(type)))
                .flatMap(Collection::stream)
                .toList();
    }
}
