package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class FabricModPOJOLoader<T extends ForgeroDataResource> {
    private final Class<T> type;
    private final String location;

    public FabricModPOJOLoader(Class<T> type, String location) {
        this.type = type;
        this.location = location;
    }

    public List<T> loadPojosFromMods() {
        var resourceContainers = new ForgeroResourceModContainerService().getForgeroResourceContainers();
        List<Path> paths = resourceContainers.stream().map(container -> container.getResourcesInFolder(location)).flatMap(Collection::stream).toList();
        return loadPojoFromPaths(paths);
    }

    @NotNull
    public List<T> loadPojoFromPaths(List<Path> paths) {
        return paths
                .stream()
                .map(path -> JsonPOJOLoader
                        .loadPOJO(String.format("/%s%s", location, path.toString().split(location)[1]), type))
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(resource -> resource.getName().toLowerCase(Locale.ROOT), resource -> resource, (existing, replacement) -> existing.order > replacement.order ? existing : replacement))
                .values()
                .stream()
                .toList();
    }


}
