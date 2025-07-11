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
	private String topLevelDirectory; // e.g., "assets"
	private final ClassLoader classLoader;

	public ClassPathResourceProvider(String topLevelDirectory) {
		// Ensure topLevelDirectory doesn't start with a slash or end with one for consistency
		this.topLevelDirectory = topLevelDirectory.startsWith("/") ? topLevelDirectory.substring(1) : topLevelDirectory;
		this.topLevelDirectory = this.topLevelDirectory.endsWith("/") ? this.topLevelDirectory.substring(0, this.topLevelDirectory.length() - 1) : this.topLevelDirectory;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	// This method builds the full classpath path for a resource to be read, e.g., "assets/forgero/item/oak-handle.png"
	private String buildFullPathForRead(OpenIdentifier identifier) {
		return topLevelDirectory + "/" + identifier.namespace() + "/" + identifier.path();
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		// The 'path' here is relative to the namespace, e.g., OpenIdentifier("forgero", "models")
		String baseClasspathPath = topLevelDirectory + "/" + path.namespace() + "/"; // e.g., "assets/forgero/"
		String targetDirectoryInNamespace = path.path(); // e.g., "models"

		URL rootUrl = classLoader.getResource(baseClasspathPath);
		if (rootUrl == null) {
			// This indicates the base namespace directory itself wasn't found.
			System.err.println("Base classpath directory not found: " + baseClasspathPath);
			return Stream.empty();
		}

		try {
			URI uri = rootUrl.toURI();

			// Handle resources located in JAR files vs. standard file system
			if ("jar".equals(uri.getScheme())) {
				return listResourcesFromJar(uri, baseClasspathPath, targetDirectoryInNamespace, path.namespace(), recursive);
			} else {
				return listResourcesFromFileSystem(Paths.get(uri), targetDirectoryInNamespace, path.namespace(), recursive);
			}
		} catch (Exception e) {
			System.err.println("Error listing resources from " + baseClasspathPath + ": " + e.getMessage());
			return Stream.empty();
		}
	}

	private Stream<OpenIdentifier> listResourcesFromFileSystem(Path rootFsPath, String targetDirectory, String namespace, boolean recursive) throws Exception {
		Path startPath = rootFsPath.resolve(targetDirectory); // This is the actual directory to walk, e.g., "/path/to/assets/forgero/models"
		if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
			return Stream.empty(); // Directory doesn't exist or isn't a directory
		}

		int maxDepth = recursive ? Integer.MAX_VALUE : 1;
		try (Stream<Path> walk = Files.walk(startPath, maxDepth)) {
			return walk
					.filter(Files::isRegularFile)
					.map(filePath -> {
						// Relativize the file path against the namespace root to get the resource path
						Path relativePath = rootFsPath.relativize(filePath);
						String relativePathString = relativePath.toString().replace('\\', '/');
						return new OpenIdentifier(namespace, relativePathString);
					})
					// Collect toList() to close the stream before returning, avoiding 'stream already operated upon' issues.
					.collect(Collectors.toList()).stream();
		}
	}

	private Stream<OpenIdentifier> listResourcesFromJar(URI jarUri, String baseClasspathPath, String targetDirectory, String namespace, boolean recursive) throws Exception {
		// The path inside the JAR is after the '!' separator.
		// baseClasspathPath is e.g., "assets/forgero/"
		String pathInJarRoot = jarUri.toString().split("!")[1]; // e.g., "/assets/forgero/" if jarUri is "jar:file:/path/to/jar.jar!/assets/forgero/"

		// Ensure we create FileSystem only once per JAR if possible, or close it properly.
		// Using try-with-resources for FileSystem ensures it's closed.
		try (FileSystem fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
			Path startPathInJar = fs.getPath(pathInJarRoot, targetDirectory); // e.g., "/assets/forgero/models" within the JAR FS
			if (!Files.exists(startPathInJar) || !Files.isDirectory(startPathInJar)) {
				return Stream.empty();
			}

			Path namespaceRootInJar = fs.getPath(pathInJarRoot);

			int maxDepth = recursive ? Integer.MAX_VALUE : 1;
			try (Stream<Path> walk = Files.walk(startPathInJar, maxDepth)) {
				return walk
						.filter(Files::isRegularFile)
						.map(filePath -> {
							// Relativize against the namespace root inside the JAR
							Path relativePath = namespaceRootInJar.relativize(filePath);
							String relativePathString = relativePath.toString().replace('\\', '/');
							return new OpenIdentifier(namespace, relativePathString);
						})
						.toList().stream();
			}
		}
	}


	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		String fullPath = buildFullPathForRead(identifier); // Use the correct method for reading
		InputStream stream = classLoader.getResourceAsStream(fullPath);
		return Optional.ofNullable(stream);
	}
}
