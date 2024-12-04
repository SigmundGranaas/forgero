package com.sigmundgranaas.forgero.core.util.loader;

import com.sigmundgranaas.forgero.core.Forgero;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

public class ClassLoader implements InputStreamLoader {
	private static final Logger logger = Forgero.LOGGER;

	public enum LoadStrategy {
		CLASSLOADER, CLASS
	}

	@Override
	public Optional<InputStream> load(String location) {
		try {
			// Try direct file access first for development environment
			if (isAbsolutePath(location)) {
				File file = new File(location);
				if (file.exists() && file.isFile()) {
					return Optional.of(new FileInputStream(file));
				}
				// If absolute path doesn't exist as file, try to extract relative path
				location = extractResourcePath(location);
			}

			LoadStrategy strategy = location.startsWith("/") ? LoadStrategy.CLASS : LoadStrategy.CLASSLOADER;
			return load(location, strategy);
		} catch (Exception e) {
			logger.error("Failed to load resource: {}", location, e);
			return Optional.empty();
		}
	}

	public Optional<InputStream> load(String location, LoadStrategy strategy) {
		if (location == null || location.trim().isEmpty()) {
			throw new IllegalArgumentException("Resource location cannot be null or empty");
		}

		try {
			String normalizedPath = normalizePath(location, strategy);
			InputStream resourceStream = loadWithStrategy(normalizedPath, strategy);

			if (resourceStream == null) {
				resourceStream = tryAlternateStrategy(normalizedPath, strategy);
			}

			return Optional.ofNullable(resourceStream);
		} catch (Exception e) {
			logger.error("Failed to load resource: {} using strategy: {}", location, strategy, e);
			return Optional.empty();
		}
	}

	private String normalizePath(String location, LoadStrategy strategy) {
		String normalized = location.replace('\\', '/').trim();

		if (isAbsolutePath(normalized)) {
			normalized = extractResourcePath(normalized);
		}

		return switch (strategy) {
			case CLASSLOADER -> normalized.replaceAll("^/+", "");
			case CLASS -> normalized.startsWith("/") ? normalized : "/" + normalized;
		};
	}

	private String extractResourcePath(String absolutePath) {
		String[] segments = absolutePath.split("/resources/");
		if (segments.length == 2) {
			return segments[1];
		}
		return absolutePath;
	}

	private boolean isAbsolutePath(String path) {
		return path.startsWith("/") && (path.contains("/resources/"));
	}

	private InputStream loadWithStrategy(String normalizedPath, LoadStrategy strategy) {
		return switch (strategy) {
			case CLASSLOADER -> this.getClass().getClassLoader().getResourceAsStream(normalizedPath);
			case CLASS -> this.getClass().getResourceAsStream(normalizedPath);
		};
	}

	private InputStream tryAlternateStrategy(String normalizedPath, LoadStrategy strategy) {
		LoadStrategy alternateStrategy = (strategy == LoadStrategy.CLASS) ?
				LoadStrategy.CLASSLOADER : LoadStrategy.CLASS;

		String alternatePath = normalizePath(normalizedPath, alternateStrategy);
		InputStream stream = loadWithStrategy(alternatePath, alternateStrategy);

		if (stream != null) {
			logger.debug("Resource found using alternate strategy: {} with path: {}",
					alternateStrategy, alternatePath);
		}

		return stream;
	}
}
