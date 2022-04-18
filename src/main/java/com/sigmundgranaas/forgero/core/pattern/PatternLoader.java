package com.sigmundgranaas.forgero.core.pattern;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PatternLoader {
    public List<Pattern> loadPatterns() {
        var location = "/data/forgero/pattern/";
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
            var pojos = Files.list(dirPath).map(path -> JsonPOJOLoader.loadPOJO(String.format("/data/forgero/pattern/%s", path.getFileName()), patternPOJO.class)).flatMap(Optional::stream).toList();
            return pojos.stream().map(patternPOJO::createPatternFromPojo).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
