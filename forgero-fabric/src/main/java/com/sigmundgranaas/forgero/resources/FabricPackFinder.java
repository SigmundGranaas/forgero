package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.FilePackageLoader;
import com.sigmundgranaas.forgero.resource.data.v2.PackageSupplier;

import java.nio.file.Path;
import java.util.List;

public class FabricPackFinder implements PackageSupplier {
    public final String PACK_LOCATION = "/data/forgero/packs/";

    public static PackageSupplier supplier() {
        return new FabricPackFinder();
    }

    @Override
    public List<DataPackage> supply() {
        var modService = new ModContainerService();
        return modService.getForgeroResourceContainers()
                .stream()
                .filter(container -> container.containsResource(PACK_LOCATION))
                .map(container -> container.getResourcesInFolder(PACK_LOCATION, 1))
                .flatMap(List::stream)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> !name.equals("packs"))
                .map(name -> new FilePackageLoader(PACK_LOCATION + name).get())
                .toList();
    }

}