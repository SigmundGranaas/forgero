package com.sigmundgranaas.forgero.fabric.resources;

import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.core.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FabricPackFinder implements PackageSupplier {
    public final String PACK_LOCATION = "/data/forgero/packs/";

    public static PackageSupplier supplier() {
        return new FabricPackFinder();
    }

    @Override
    public List<DataPackage> supply() {
        var modService = new ModContainerService();
        var resources = modService.getForgeroResourceContainers()
                .stream()
                .filter(container -> container.containsResource(PACK_LOCATION))
                .map(container -> container.getResourcesInFolder(PACK_LOCATION, 1))
                .flatMap(List::stream)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> !name.equals("packs"))
                .map(name -> PACK_LOCATION + name)
                .map(FilePackageLoader::new)
                .map(CompletableFuture::supplyAsync)
                .toList();

        return resources.stream().map(CompletableFuture::join).toList();
    }

}