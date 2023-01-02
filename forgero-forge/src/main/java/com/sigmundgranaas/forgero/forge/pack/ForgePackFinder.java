package com.sigmundgranaas.forgero.forge.pack;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfiguration;
import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.core.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.DefaultMapper;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.FileResourceLoader;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ForgePackFinder implements PackageSupplier {
    public final String PACK_LOCATION = "/data/forgero/packs/";
    private ForgeroConfiguration configuration;

    public ForgePackFinder(ForgeroConfiguration configuration) {
        this.configuration = configuration;
    }

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

    private FilePackageLoader loader(String path) {
        var loader = new FileResourceLoader(path, configuration.locator(), new DefaultMapper(), configuration.streamLoader());
        return new FilePackageLoader(loader);
    }

    public List<Path> getResourcesInFolder(String resourceLocation) {
        return PathWalker.builder()
                .depth(1)
                .pathFinder(configuration.pathFinder())
                .build()
                .locate(resourceLocation);
    }
}
