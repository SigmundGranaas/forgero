package com.sigmundgranaas.forgero.core.util.loader;

import com.sigmundgranaas.forgero.core.Forgero;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassLoader implements InputStreamLoader {
	private static final String RESOURCE_PREFIX = "/";
	private static final Logger logger = Forgero.LOGGER;

	@Override
	public Optional<InputStream> load(String location) {
		try {
			String normalizedPath = normalizePath(location);
			InputStream resourceStream = this.getClass().getResourceAsStream(normalizedPath);

			if (resourceStream == null) {
				// Try alternate path variations
				resourceStream = tryAlternatePathVariations(normalizedPath);
			}

			return Optional.ofNullable(resourceStream);
		} catch (Exception e) {
			logger.error("Failed to load resource: " + location, e);
			return Optional.empty();
		}
	}

	private String normalizePath(String location) {
		if (location == null || location.trim().isEmpty()) {
			throw new IllegalArgumentException("Resource location cannot be null or empty");
		}

		// Remove any leading dots and convert backslashes to forward slashes
		String normalized = location.replaceAll("^\\.*", "")
				.replace("\\", "/")
				.trim();

		// Ensure the path starts with a forward slash
		if (!normalized.startsWith(RESOURCE_PREFIX)) {
			normalized = RESOURCE_PREFIX + normalized;
		}

		// Remove any duplicate slashes
		normalized = normalized.replaceAll("/+", "/");

		return normalized;
	}

	private InputStream tryAlternatePathVariations(String normalizedPath) {
		List<String> pathVariations = new ArrayList<>();

		// Try without leading slash
		if (normalizedPath.startsWith(RESOURCE_PREFIX)) {
			pathVariations.add(normalizedPath.substring(1));
		}

		// Try with 'resources' directory prefix
		pathVariations.add("/resources" + normalizedPath);
		pathVariations.add("resources" + normalizedPath);

		// Try relative to the current package
		String packagePath = this.getClass().getPackage().getName().replace('.', '/');
		pathVariations.add("/" + packagePath + normalizedPath);

		for (String path : pathVariations) {
			InputStream stream = this.getClass().getResourceAsStream(path);
			if (stream != null) {
				logger.info("Resource found using alternate path: {}", path);
				return stream;
			}
		}

		logger.debug("Resource not found after trying variations for: {}", normalizedPath);
		return null;
	}

	/**
	 * Validates if a resource exists at the given location.
	 * @param location The resource location to validate
	 * @return true if the resource exists and is accessible, false otherwise
	 */
	public boolean validateResource(String location) {
		try (InputStream stream = load(location).orElse(null)) {
			return stream != null;
		} catch (IOException e) {
			logger.error("Error validating resource: {}", location, e);
			return false;
		}
	}
}
