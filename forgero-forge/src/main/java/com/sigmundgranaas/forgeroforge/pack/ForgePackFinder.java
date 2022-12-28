package com.sigmundgranaas.forgeroforge.pack;

import com.google.common.base.Stopwatch;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.resource.data.v2.packages.FilePackageLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ForgePackFinder implements PackageSupplier {
    public final String PACK_LOCATION = "/data/forgero/packs/";

    public ForgePackFinder() {

    }

    @Override
    public List<DataPackage> supply() {
        Stopwatch timer = Stopwatch.createStarted();
        var resources = getResourcesInFolder(PACK_LOCATION, 1)
                .stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> !name.equals("packs"))
                .map(name -> PACK_LOCATION + name)
                .map(FilePackageLoader::new)
                .map(CompletableFuture::supplyAsync)
                .toList();

        var completeResources = resources.stream()
                .map(CompletableFuture::join)
                .toList();
        Forgero.LOGGER.info("Package load time: " + timer.stop());
        return completeResources;
    }

    public List<Path> getResourcesInFolder(String resourceLocation, int depth) {
        try {
            var path = Path.of(this.getClass().getClassLoader().getResource(PACK_LOCATION).toURI());
            try (var filesStream = Files.walk(path, depth)) {
                return filesStream.toList();
            } catch (IOException | NullPointerException e) {
                Forgero.LOGGER.error("Unable to list files from {}", resourceLocation);
                Forgero.LOGGER.error(e);
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
