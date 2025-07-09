package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassPathResourceProvider implements ResourceProvider {
	private final String topLevelDirectory;
	private final ClassLoader classLoader;

	public ClassPathResourceProvider(String topLevelDirectory) {
		this.topLevelDirectory = topLevelDirectory;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	private String buildFullPath(OpenIdentifier identifier) {
		return topLevelDirectory + "/" + identifier.namespace() + "/" + identifier.path();
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		String fullPath = buildFullPath(path);
		try {
			URL resourceUrl = classLoader.getResource(fullPath);
			if (resourceUrl == null) {
				return Stream.empty();
			}
			URI uri = resourceUrl.toURI();

			// Handle resources located in JAR files vs. standard file system
			if ("jar".equals(uri.getScheme())) {
				return listResourcesFromJar(uri, path, recursive);
			} else {
				return listResourcesFromFileSystem(Paths.get(uri), path, recursive);
			}
		} catch (Exception e) {
			// Log error or handle it as needed. Returning an empty stream is a safe default.
			return Stream.empty();
		}
	}

	private Stream<OpenIdentifier> listResourcesFromFileSystem(Path startPath, OpenIdentifier rootIdentifier, boolean recursive) throws Exception {
		int maxDepth = recursive ? Integer.MAX_VALUE : 1;
		try (Stream<Path> walk = Files.walk(startPath, maxDepth)) {
			return walk
					.filter(Files::isRegularFile)
					.map(filePath -> {
						String relativePath = startPath.relativize(filePath).toString().replace('\\', '/');
						return new OpenIdentifier(rootIdentifier.namespace(), rootIdentifier.path() + "/" + relativePath);
					})
					// This collect is necessary to avoid issues with the stream being closed by the try-with-resources block
					// before the caller has a chance to process it.
					.collect(Collectors.toList()).stream();
		}
	}

	private Stream<OpenIdentifier> listResourcesFromJar(URI jarUri, OpenIdentifier rootIdentifier, boolean recursive) throws Exception {
		// The path inside the JAR is after the '!' separator.
		String pathInJarStr = jarUri.toString().split("!")[1];

		// We need to establish a file system for the JAR to be able to walk it.
		try (FileSystem fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
			Path startPath = fs.getPath(pathInJarStr);
			int maxDepth = recursive ? Integer.MAX_VALUE : 1;

			try (Stream<Path> walk = Files.walk(startPath, maxDepth)) {
				return walk
						.filter(Files::isRegularFile)
						.map(filePath -> {
							String relativePath = startPath.relativize(filePath).toString(); // Slashes are already '/' in JARs
							return new OpenIdentifier(rootIdentifier.namespace(), rootIdentifier.path() + "/" + relativePath);
						})
						.collect(Collectors.toList()).stream();
			}
		}
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		String fullPath = buildFullPath(identifier);
		InputStream stream = classLoader.getResourceAsStream(fullPath);
		return Optional.ofNullable(stream);
	}
}
