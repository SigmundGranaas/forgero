package com.sigmundgranaas.forgero.core.schematic;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SchematicLoader {
    public List<Schematic> loadSchematics() {
        var location = "/data/forgero/schematic/";
        URI uri = null;
        try {
            var etc = ForgeroInitializer.class.getResource(location);
            assert etc != null;
            uri = etc.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path dirPath = null;
        try {

            assert uri != null;
            dirPath = Paths.get(uri);
        } catch (FileSystemNotFoundException e) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            var env = new HashMap<String, String>();
            try {
                dirPath = FileSystems.newFileSystem(uri, env).getPath(location);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            assert dirPath != null;
            var pojos = Files.list(dirPath).map(path -> JsonPOJOLoader.loadPOJO(String.format("/data/forgero/schematic/%s", path.getFileName()), SchematicPOJO.class)).flatMap(Optional::stream).toList();
            return pojos.stream().map(SchematicPOJO::createSchematicFromPojo).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public List<Schematic> loadSchematicFromContainer(ModContainer container) {
        var location = "/data/forgero/schematic";
        URI uri = null;
        var etc1 = container.getRootPaths();
        var etc = container.findPath("data/forgero/schematic");
        if (etc.isPresent() && container.getMetadata().containsCustomValue("forgeroResource") && container.getMetadata().getCustomValue("forgeroResource").getAsBoolean()) {
            uri = etc.get().toUri();
            Path dirPath = null;
            try {
                assert uri != null;
                dirPath = Paths.get(uri);
            } catch (FileSystemNotFoundException e) {
                // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
                var env = new HashMap<String, String>();
                try {
                    dirPath = FileSystems.newFileSystem(uri, env).getPath(location);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            try {
                assert dirPath != null;
                var pojos = Files.list(dirPath).map(path -> JsonPOJOLoader.loadPOJO(String.format("/data/forgero/schematic/%s", path.getFileName()), SchematicPOJO.class)).flatMap(Optional::stream).toList();
                ForgeroInitializer.LOGGER.info("loaded {} schematics from {}", pojos.size(), container.getMetadata().getName());
                return pojos.stream().map(SchematicPOJO::createSchematicFromPojo).toList();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        } else {
            return Collections.emptyList();
        }

    }

}
