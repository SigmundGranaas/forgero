package com.sigmundgranaas.forgero.core.resource.data.v2.packages;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.resource.data.ResourceLoader;
import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.DefaultMapper;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.FileResourceLoader;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.JsonContentFilter;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;

import java.util.List;
import java.util.function.Supplier;

public class FilePackageLoader implements Supplier<DataPackage> {
	private final ResourceLoader loader;

	private final String folderPath;

	public FilePackageLoader(String folderPath) {
		this.folderPath = folderPath;
		ResourceLocator walker = PathWalker.builder()
				.contentFilter(new JsonContentFilter())
				.pathFinder(PathFinder::ClassLoaderFinder)
				.build();
		this.loader = FileResourceLoader.of(folderPath, walker, List.of(new DefaultMapper()));
	}

	public FilePackageLoader(ResourceLoader loader, String folderPath) {
		this.loader = loader;
		this.folderPath = folderPath;
	}

	public static Supplier<DataPackage> of(String folderPath) {
		ResourceLocator walker = PathWalker.builder()
				.contentFilter(new JsonContentFilter())
				.pathFinder(PathFinder::ClassLoaderFinder)
				.build();
		var loader = FileResourceLoader.of(folderPath, walker, List.of(new DefaultMapper()));

		return new FilePackageLoader(loader, folderPath);
	}

	private List<DataResource> resources() {
		return loader.load();
	}

	@Override
	public DataPackage get() {
		var packageInfo = loader.loadResource(folderPath + "/package.json");
		var dependencies = packageInfo.map(DataResource::dependencies).orElse(ImmutableList.<String>builder().build());
		var priority = packageInfo.map(DataResource::priority).orElse(5);
		var name = packageInfo.map(DataResource::name).orElse(Identifiers.EMPTY_IDENTIFIER);
		var nameSpace = packageInfo.map(DataResource::nameSpace).orElse(Identifiers.EMPTY_IDENTIFIER);
		return new DefaultPackage(dependencies, priority, this::resources, nameSpace, name);
	}
}
