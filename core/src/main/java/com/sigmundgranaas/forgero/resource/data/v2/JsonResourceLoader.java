package com.sigmundgranaas.forgero.resource.data.v2;


import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.JsonPathLoader;
import com.sigmundgranaas.forgero.resource.data.ResourceLoader;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.DefaultResourcePair;
import com.sigmundgranaas.forgero.util.JsonPOJOLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JsonResourceLoader implements ResourceLoader {
    private final String folder;

    public JsonResourceLoader(String folderPath) {
        this.folder = folderPath;
    }

    @Override
    public List<DataResource> load() {
        List<Path> paths = JsonPathLoader.getResourcesInFolder(folder);
        return loadPojos(paths);
    }

    private Optional<String> getFilePath(Path path) {
        String[] elements = path.toString().split("data");
        if (elements.length == 2) {
            return Optional.of("/" + "data" + elements[1]);
        }
        Forgero.LOGGER.error("Unable to resolve path {}, as it could not be split using default split operator {}", path.toString(), File.separator);
        return Optional.empty();
    }

    private List<DataResource> loadPojos(List<Path> paths) {
        var resources = paths.stream()
                .map(this::getFilePath)
                .flatMap(Optional::stream)
                .map(JsonPOJOLoader::loadPOJOWithContext)
                .flatMap(Optional::stream)
                .toList();

        var defaults = resources.stream()
                .filter(resource -> resource.context().isPresent())
                .filter(resource -> resource.context().map(context -> context.fileName().equals("default.json")).orElse(false))
                .toList();

        return resources.stream()
                .filter(resource -> resource.context().map(context -> !context.fileName().equals("default.json")).orElse(false))
                .map(resource -> {
                    var defaultOptional = DefaultResourcePair.linkDefaults(defaults.stream()
                            .filter(defaultTest -> resource.context().map(context -> context.path().contains(defaultTest.context().get().path())).orElse(false)).toList());
                    return new DefaultResourcePair(resource, defaultOptional);
                })
                .map(pair -> pair.OptionalDefault().map(opt -> DefaultResourcePair.applyDefaults(pair.resource(), opt)).orElse(pair.resource()))
                .toList();
    }
}
