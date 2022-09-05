package com.sigmundgranaas.forgerocore.resourceloader;

import com.sigmundgranaas.forgerocore.Forgero;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgerocore.data.Constant.CORE_PATH;

public class JsonPathLoader {
    public static List<Path> getResourcesInFolder() throws URISyntaxException {
        Path core_path = Path.of(JsonPathLoader.class.getResource(CORE_PATH).toURI());
        try (var filesStream = Files.walk(core_path, 20)) {
            return filesStream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json")).toList();
        } catch (IOException e) {
            Forgero.LOGGER.error("Unable to list files from {}", core_path.toString());
            Forgero.LOGGER.error(e);
        }
        return Collections.emptyList();
    }

    @Test
    void testLoadJsonFiles() throws URISyntaxException {
        Assertions.assertTrue(getResourcesInFolder().size() > 0);
    }
}
