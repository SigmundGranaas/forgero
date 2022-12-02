package com.sigmundgranaas.forgero.resource.data;

import com.sigmundgranaas.forgero.Forgero;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class JsonPathLoader {
    public static List<Path> getResourcesInFolder(String rootFolder) {
        Path core_path = Path.of("invalid");
        try {
            core_path = Path.of(JsonPathLoader.class.getResource(rootFolder).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try (var filesStream = Files.walk(core_path, 20)) {
            return filesStream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json")).toList();
        } catch (IOException e) {
            Forgero.LOGGER.error("Unable to list files from {}", core_path.toString());
            Forgero.LOGGER.error(e);
        }
        return Collections.emptyList();
    }
}
