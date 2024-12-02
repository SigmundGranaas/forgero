package com.sigmundgranaas.forgero.core.util.loader;

import com.sigmundgranaas.forgero.core.Forgero;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Optional;

public class ClassLoader implements InputStreamLoader {
	private static final Logger logger = Forgero.LOGGER;

	/**
	 * Strategies for loading resources
	 */
	public enum LoadStrategy {
		CLASSLOADER,  // Uses ClassLoader.getResourceAsStream()
		CLASS         // Uses Class.getResourceAsStream()
	}

	@Override
	public Optional<InputStream> load(String location) {
		// Default to CLASS strategy if path starts with "/", otherwise CLASSLOADER
		LoadStrategy strategy = location.startsWith("/") ?
				LoadStrategy.CLASS : LoadStrategy.CLASSLOADER;
		return load(location, strategy);
	}

	/**
	 * Load a resource using a specific loading strategy.
	 *
	 * @param location The resource location
	 * @param strategy The loading strategy to use
	 * @return Optional containing the InputStream if found
	 */
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
		String normalized = location.replace("\\", "/").trim();

		switch (strategy) {
			case CLASSLOADER:
				// Remove leading slash for ClassLoader.getResourceAsStream()
				normalized = normalized.replaceAll("^/+", "");
				break;

			case CLASS:
				// Ensure leading slash for absolute paths in Class.getResourceAsStream()
				if (!normalized.startsWith("/")) {
					normalized = "/" + normalized;
				}
				break;
		}

		// Remove any duplicate slashes
		return normalized.replaceAll("/+", "/");
	}

	private InputStream loadWithStrategy(String normalizedPath, LoadStrategy strategy) {
		return switch (strategy) {
			case CLASSLOADER -> this.getClass().getClassLoader()
					.getResourceAsStream(normalizedPath);
			case CLASS -> this.getClass()
					.getResourceAsStream(normalizedPath);
		};
	}

	private InputStream tryAlternateStrategy(String normalizedPath, LoadStrategy strategy) {
		// If one strategy fails, try the other
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
