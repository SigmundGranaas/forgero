package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.io.InputStream;
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

    public List<Path> getResourcesInFolder(String resourceLocation) {
        var optionalPath = container.findPath(resourceLocation);
        if (optionalPath.isPresent()) {
            try (var filesStream = Files.walk(optionalPath.get(), 4)) {
                return filesStream.filter(path -> path.toFile().isFile()).toList();
            } catch (IOException e) {
                ForgeroInitializer.LOGGER.error("Unable to list files from {}", resourceLocation);
                ForgeroInitializer.LOGGER.error(e);
            }
        }
        return Collections.emptyList();
    }

    public boolean containsResource(String resourceLocation) {
        var optionalPath = container.findPath(resourceLocation);
        return optionalPath.isPresent();
    }

    public Optional<InputStream> loadResource(String resourceLocation) {
        ClassLoader classLoader = ModContainerFileLoader.class.getClassLoader();
        var optionalPath = container.findPath(resourceLocation).map(path -> Path.of(path.toString().split("/resources/assets/").length == 1 ? path.toString().split("/resources/assets/")[0] : path.toString().split("/resources/assets/")[1]));
        return optionalPath.map(path -> classLoader.getResourceAsStream(resourceLocation));
    }

    private List<Path> collectResourcesFromPath(Path path) {
        Optional<Path> dirPath = findPathFromEnv(path);
        try {
            if (dirPath.isPresent()) {
                return Files.list(path).toList();
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
