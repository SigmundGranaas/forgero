package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ModContainerFileLoader {
    private final ModContainer container;

    public ModContainerFileLoader(ModContainer container) {
        this.container = container;
    }

    public List<Path> getResources(String resourceLocation) {
        var optionalPath = container.findPath(resourceLocation);
        if (optionalPath.isPresent()) {
            return collectResourcesFromPath(optionalPath.get());
        } else {
            return Collections.emptyList();
        }
    }

    private List<Path> collectResourcesFromPath(Path path) {
        Optional<Path> dirPath = findPathFromEnv(path);
        try {
            if (dirPath.isPresent()) {
                return Files.list(dirPath.get()).toList();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ForgeroInitializer.LOGGER.info(e);
        } catch (NullPointerException e) {
            ForgeroInitializer.LOGGER.info("Caught a nullPointer trying to read path {}", path);
        }
        return Collections.emptyList();
    }

    private Optional<Path> findPathFromEnv(Path path) {
        try {
            return Optional.of(Paths.get(path.toUri()));
        } catch (FileSystemNotFoundException e) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            return getPathFromJar(path);
        }
    }

    private Optional<Path> getPathFromJar(Path path) {
        var env = new HashMap<String, String>();
        try {
            return Optional.of(FileSystems.newFileSystem(path.toUri(), env).getPath(path.toString()));
        } catch (IOException ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}
