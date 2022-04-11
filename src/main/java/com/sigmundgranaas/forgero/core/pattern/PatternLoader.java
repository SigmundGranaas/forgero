package com.sigmundgranaas.forgero.core.pattern;

import com.sigmundgranaas.forgero.ForgeroInitializer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PatternLoader {
    public List<Pattern> loadPatterns() {
        var location = "/data/forgero/pattern/";
        URI uri = null;
        try {
            var etc = ForgeroInitializer.class.getResource(location);
            System.out.println(etc);
            assert etc != null;
            uri = etc.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path dirPath = null;
        System.out.println("dirPath");
        try {
            assert uri != null;
            dirPath = Paths.get(uri);
            System.out.println(dirPath);
        } catch (FileSystemNotFoundException e) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            var env = new HashMap<String, String>();
            try {
                System.out.println(uri);
                dirPath = FileSystems.newFileSystem(uri, env).getPath(location);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            assert dirPath != null;
            Files.list(dirPath).forEach(file -> {
                        System.out.println(file.getFileName());
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
