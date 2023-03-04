package com.sigmundgranaas.forgero.forge.pack;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfiguration;
import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.core.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.DefaultMapper;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.FileResourceLoader;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;
import com.sigmundgranaas.forgero.core.util.loader.ClassLoader;
import com.sigmundgranaas.forgero.core.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ForgePackFinder implements PackageSupplier {
	public final String PACK_LOCATION = "/data/forgero/packs/";

	@Override
	public List<DataPackage> supply() {
		var resources = getResourcesInFolder(PACK_LOCATION)
				.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.filter(name -> !name.equals("packs"))
				.map(name -> PACK_LOCATION + name)
				.map(this::loader)
				.map(CompletableFuture::supplyAsync)
				.toList();

		return resources.stream()
				.map(CompletableFuture::join)
				.toList();
	}

	public List<Path> getResourcesInFolder(String resourceLocation) {
		return PathWalker.builder()
				.depth(1)
				.pathFinder(pathFinder())
				.build()
				.locate(resourceLocation);
	}

	private FilePackageLoader loader(String path) {
		var loader = new FileResourceLoader(path, locator(), new DefaultMapper(), streamLoader());
		return new FilePackageLoader(loader, path);
	}

	private InputStreamLoader streamLoader() {
		return new ClassLoader();
	}

	private ResourceLocator locator() {
		return PathWalker.builder()
				.pathFinder(pathFinder())
				.pathFinder(pathFinder())
				.depth(10)
				.build();
	}

	private PathFinder pathFinder() {
		return PathFinder::ClassFinder;
	}
}
