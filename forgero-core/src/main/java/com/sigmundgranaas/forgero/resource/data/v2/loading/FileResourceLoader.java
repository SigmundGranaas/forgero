package com.sigmundgranaas.forgero.resource.data.v2.loading;


import com.google.common.base.Stopwatch;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.ResourceLoader;
import com.sigmundgranaas.forgero.resource.data.v2.ResourceCollectionMapper;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FileResourceLoader implements ResourceLoader {
    private final String folder;
    private final PathWalker walker;

    private final ResourceCollectionMapper mapper;

    public FileResourceLoader(String folderPath, PathWalker walker, ResourceCollectionMapper mapper) {
        this.folder = folderPath;
        this.walker = walker;
        this.mapper = mapper;
    }

    public static FileResourceLoader of(String folderPath, PathWalker walker, List<ResourceCollectionMapper> mappers) {
        var mapper = mappers.stream().reduce(ResourceCollectionMapper.DEFAULT, (mapper1, mapper2) -> mapper1.andThen(mapper2));
        return new FileResourceLoader(folderPath, walker, mapper);
    }

    @Override
    public List<DataResource> load() {
        List<Path> paths = walker.locate(folder);
        var rawResources = rawResources(paths);
        return mapper.apply(rawResources);
    }

    private List<DataResource> rawResources(List<Path> paths) {
        Stopwatch timer = Stopwatch.createStarted();
        var resources = paths.stream()
                .map(this::getFilePath)
                .flatMap(Optional::stream)
                .map(FileResourceProvider::new)
                .map(CompletableFuture::supplyAsync)
                .toList();

        var completedResources = resources.stream()
                .map(CompletableFuture::join)
                .flatMap(Optional::stream)
                .toList();
        Forgero.LOGGER.info("Method took: " + timer.stop());
        return completedResources;
    }


    private Optional<String> getFilePath(Path path) {
        String[] elements = path.toString().split("data");
        if (elements.length == 2) {
            return Optional.of("/" + "data" + elements[1]);
        }
        Forgero.LOGGER.error("Unable to resolve path {}, as it could not be split using default split operator {}", path.toString(), File.separator);
        return Optional.empty();
    }
}
