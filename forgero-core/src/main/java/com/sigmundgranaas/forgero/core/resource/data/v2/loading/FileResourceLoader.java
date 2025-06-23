package com.sigmundgranaas.forgero.core.resource.data.v2.loading;


import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.ResourceLoader;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceCollectionMapper;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.util.loader.ClassLoader;
import com.sigmundgranaas.forgero.core.util.loader.InputStreamLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FileResourceLoader implements ResourceLoader {
	private final String folder;
	private final ResourceLocator walker;
	private final ResourceCollectionMapper mapper;

	private final InputStreamLoader streamLoader;

	public FileResourceLoader(String folderPath, ResourceLocator walker, ResourceCollectionMapper mapper, InputStreamLoader loader) {
		this.folder = folderPath;
		this.walker = walker;
		this.mapper = mapper;
		this.streamLoader = loader;
	}

	public FileResourceLoader(String folderPath, ResourceLocator walker, ResourceCollectionMapper mapper) {
		this.folder = folderPath;
		this.walker = walker;
		this.mapper = mapper;
		this.streamLoader = new ClassLoader();
	}

	public static FileResourceLoader of(String folderPath, ResourceLocator walker, List<ResourceCollectionMapper> mappers) {
		var mapper = mappers.stream().reduce(ResourceCollectionMapper.DEFAULT, (mapper1, mapper2) -> mapper1.andThen(mapper2));

		return new FileResourceLoader(folderPath, walker, mapper, new ClassLoader());
	}

	@Override
	public List<DataResource> load() {
		List<Path> paths = walker.locate(folder);
		var rawResources = rawResources(paths);
		return mapper.apply(rawResources);
	}

	@Override
	public Optional<DataResource> loadResource(String path) {
		return fileProvider(path).get();
	}

	private List<DataResource> rawResources(List<Path> paths) {
		var resources = paths.stream()
				.map(Path::toString)
				.map(this::fileProvider)
				.map(CompletableFuture::supplyAsync)
				.toList();

		return resources.stream()
				.map(CompletableFuture::join)
				.flatMap(Optional::stream)
				.toList();
	}

	private FileResourceProvider fileProvider(String path) {
		return new FileResourceProvider(path, streamLoader);
	}
}
