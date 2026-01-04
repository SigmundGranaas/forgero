package com.sigmundgranaas.forgero.utility.resource.loader.api;

import static com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConstants.*;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A configurable filter for resource discovery.
 * Replaces hardcoded extension checks with flexible filtering.
 * <p>
 * ResourceFilter extends {@link Predicate} allowing it to be used directly
 * in stream operations and composed with other filters.
 * <p>
 * Common filters are provided as static constants:
 * <ul>
 *   <li>{@link #JSON} - accepts only .json files</li>
 *   <li>{@link #PNG} - accepts only .png files</li>
 *   <li>{@link #ALL} - accepts all files</li>
 * </ul>
 */
@FunctionalInterface
public interface ResourceFilter extends Predicate<ResourcePath> {

	/**
	 * Filter that accepts only JSON files.
	 */
	ResourceFilter JSON = path -> path.hasExtension(EXTENSION_JSON);

	/**
	 * Filter that accepts only PNG image files.
	 */
	ResourceFilter PNG = path -> path.hasExtension(EXTENSION_PNG);

	/**
	 * Filter that accepts only OGG audio files.
	 */
	ResourceFilter OGG = path -> path.hasExtension("ogg");

	/**
	 * Filter that accepts all files.
	 */
	ResourceFilter ALL = path -> true;

	/**
	 * Filter that rejects all files.
	 */
	ResourceFilter NONE = path -> false;

	/**
	 * Creates a filter that accepts files with any of the specified extensions.
	 * Extension matching is case-insensitive.
	 *
	 * @param extensions The extensions to accept (without dots)
	 * @return A filter accepting files with any of the specified extensions
	 */
	static ResourceFilter extensions(String... extensions) {
		if (extensions == null || extensions.length == 0) {
			return NONE;
		}
		Set<String> extSet = Set.of(extensions);
		return path -> {
			String ext = path.extension().toLowerCase();
			return extSet.stream().anyMatch(e -> e.equalsIgnoreCase(ext));
		};
	}

	/**
	 * Creates a filter for a single extension.
	 * Extension matching is case-insensitive.
	 *
	 * @param extension The extension to accept (without dot)
	 * @return A filter accepting files with the specified extension
	 */
	static ResourceFilter extension(String extension) {
		return path -> path.hasExtension(extension);
	}

	/**
	 * Creates a filter that matches paths against a glob pattern.
	 * The pattern is matched against the full path (directory + filename + extension).
	 * <p>
	 * Glob patterns support:
	 * <ul>
	 *   <li>{@code *} - matches any number of characters except /</li>
	 *   <li>{@code **} - matches any number of characters including /</li>
	 *   <li>{@code ?} - matches exactly one character</li>
	 *   <li>{@code [abc]} - matches any character in the brackets</li>
	 *   <li>{@code {a,b,c}} - matches any of the comma-separated patterns</li>
	 * </ul>
	 * <p>
	 * Examples:
	 * <ul>
	 *   <li>{@code "*.json"} - matches all JSON files in any directory</li>
	 *   <li>{@code "materials/*.json"} - matches JSON files in the materials directory</li>
	 *   <li>{@code "**&#47;iron*.json"} - matches JSON files starting with "iron" in any directory</li>
	 * </ul>
	 *
	 * @param pattern The glob pattern
	 * @return A filter matching the pattern
	 */
	static ResourceFilter glob(String pattern) {
		// Use NIO's glob matcher
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		return path -> matcher.matches(Paths.get(path.fullPath()));
	}

	/**
	 * Creates a filter that matches paths against a regular expression.
	 * The regex is matched against the full path (directory + filename + extension).
	 *
	 * @param regex The regular expression pattern
	 * @return A filter matching the regex
	 */
	static ResourceFilter regex(String regex) {
		Pattern pattern = Pattern.compile(regex);
		return path -> pattern.matcher(path.fullPath()).matches();
	}

	/**
	 * Creates a filter that accepts files in a specific directory (non-recursive).
	 *
	 * @param directory The directory to match
	 * @return A filter accepting files directly in the specified directory
	 */
	static ResourceFilter inDirectory(String directory) {
		String normalizedDir = directory.replaceAll("^/+|/+$", "");
		return path -> path.directory().equals(normalizedDir);
	}

	/**
	 * Creates a filter that accepts files in a directory or any subdirectory (recursive).
	 *
	 * @param directory The directory to match
	 * @return A filter accepting files in the directory or any subdirectory
	 */
	static ResourceFilter inDirectoryRecursive(String directory) {
		return path -> path.isInDirectory(directory);
	}

	/**
	 * Creates a filter that accepts files with names matching a pattern.
	 * The pattern is matched against the filename only (no extension).
	 *
	 * @param pattern A glob pattern for the filename
	 * @return A filter matching filenames
	 */
	static ResourceFilter fileNameMatches(String pattern) {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		return path -> matcher.matches(Paths.get(path.fileName()));
	}

	/**
	 * Creates a filter that accepts files with names starting with the specified prefix.
	 *
	 * @param prefix The filename prefix
	 * @return A filter matching filenames starting with the prefix
	 */
	static ResourceFilter fileNameStartsWith(String prefix) {
		return path -> path.fileName().startsWith(prefix);
	}

	/**
	 * Creates a filter that accepts files with names ending with the specified suffix.
	 * Note: This matches the filename without extension.
	 *
	 * @param suffix The filename suffix
	 * @return A filter matching filenames ending with the suffix
	 */
	static ResourceFilter fileNameEndsWith(String suffix) {
		return path -> path.fileName().endsWith(suffix);
	}

	/**
	 * Combines this filter with another using AND logic.
	 * The resulting filter accepts a path only if both filters accept it.
	 *
	 * @param other The other filter
	 * @return A combined filter
	 */
	default ResourceFilter and(ResourceFilter other) {
		return path -> this.test(path) && other.test(path);
	}

	/**
	 * Combines this filter with another using OR logic.
	 * The resulting filter accepts a path if either filter accepts it.
	 *
	 * @param other The other filter
	 * @return A combined filter
	 */
	default ResourceFilter or(ResourceFilter other) {
		return path -> this.test(path) || other.test(path);
	}

	/**
	 * Negates this filter.
	 * The resulting filter accepts a path if this filter rejects it.
	 *
	 * @return A negated filter
	 */
	default ResourceFilter negate() {
		return path -> !this.test(path);
	}
}
