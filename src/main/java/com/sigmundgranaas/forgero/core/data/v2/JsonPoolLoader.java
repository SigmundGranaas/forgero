package com.sigmundgranaas.forgero.core.data.v2;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonResource;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JsonPoolLoader {
    private final List<Path> paths;

    public JsonPoolLoader(List<Path> paths) {
        this.paths = paths;
    }

    private Optional<String> getFilePath(Path path) {
        String[] elements = path.toString().split("data");
        if (elements.length == 2) {
            return Optional.of("/" + "data" + elements[1]);
        }
        ForgeroInitializer.LOGGER.error("Unable to resolve path {}, as it could not be split using default split operator {}", path.toString(), File.separator);
        return Optional.empty();
    }

    public List<JsonResource> loadPojos() {
        var resources = paths.stream()
                .map(this::getFilePath)
                .flatMap(Optional::stream)
                .map(JsonPOJOLoader::loadPOJOWithContext)
                .flatMap(Optional::stream)
                .toList();
        var defaults = resources.stream()
                .filter(resource -> Objects.nonNull(resource.context))
                .filter(resource -> resource.context.fileName.equals("default.json"))
                .toList();

        resources = resources.stream().map(resource -> {
            var resourceDefault = defaults.stream()
                    .filter(defaultTest -> resource.context.path.contains(defaultTest.context.path))
                    .sorted(Comparator.comparingInt(aDefault -> aDefault.context.path.split("\\" + File.separator).length))
                    .reduce((default1, default2) -> {
                        var newDefault = default1.copy();
                        newDefault.context.defaults = default2.copy();

                        return newDefault;
                    });
            var newResource = resource.copy();
            resourceDefault.ifPresent(resDef -> newResource.context.defaults = resDef);
            return newResource;
        }).map(JsonResource::applyDefaults).toList();

        return resources;
    }
}
