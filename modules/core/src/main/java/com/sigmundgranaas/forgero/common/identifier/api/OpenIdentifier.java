package com.sigmundgranaas.forgero.common.identifier.api;

import java.util.Objects;
import java.util.Optional;

/**
 * A namespace-qualified identifier in the format "namespace:path".
 * <p>
 * The namespace must be lowercase alphanumeric with '_' or '-'.
 * The path may also contain '/' and '.' for resource paths.
 */
public record OpenIdentifier(String namespace, String path) {

	public static final String DEFAULT_NAMESPACE = "forgero";

	public OpenIdentifier {
		Objects.requireNonNull(namespace, "namespace cannot be null");
		Objects.requireNonNull(path, "path cannot be null");

		if (namespace.isEmpty()) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		if (path.isEmpty()) {
			throw new IllegalArgumentException("path cannot be empty");
		}
		if (!isValidNamespace(namespace)) {
			throw new IllegalArgumentException(
					"Invalid namespace: '" + namespace + "'. Must be lowercase alphanumeric, '_', or '-'.");
		}
		if (!isValidPath(path)) {
			throw new IllegalArgumentException(
					"Invalid path: '" + path + "'. Must be lowercase alphanumeric, '_', '-', '/', or '.'.");
		}
	}

	/**
	 * Parses a string in "namespace:path" format.
	 *
	 * @param id The identifier string to parse
	 * @return A new OpenIdentifier
	 * @throws IllegalArgumentException if the format is invalid
	 * @throws NullPointerException     if id is null
	 */
	public static OpenIdentifier parse(String id) {
		Objects.requireNonNull(id, "id cannot be null");
		int colonIndex = id.indexOf(':');
		if (colonIndex == -1) {
			throw new IllegalArgumentException(
					"Invalid identifier format: '" + id + "'. Expected 'namespace:path'.");
		}
		if (colonIndex == 0) {
			throw new IllegalArgumentException(
					"Invalid identifier: '" + id + "'. Namespace cannot be empty.");
		}
		if (colonIndex == id.length() - 1) {
			throw new IllegalArgumentException(
					"Invalid identifier: '" + id + "'. Path cannot be empty.");
		}
		return new OpenIdentifier(
				id.substring(0, colonIndex),
				id.substring(colonIndex + 1)
		);
	}

	/**
	 * Attempts to parse an identifier string, returning empty on failure.
	 *
	 * @param id The identifier string to parse
	 * @return Optional containing the identifier if valid, empty otherwise
	 */
	public static Optional<OpenIdentifier> tryParse(String id) {
		if (id == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(parse(id));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	/**
	 * Creates an identifier in the default namespace ("forgero").
	 *
	 * @param path The path component
	 * @return A new OpenIdentifier with the default namespace
	 */
	public static OpenIdentifier of(String path) {
		return new OpenIdentifier(DEFAULT_NAMESPACE, path);
	}

	/**
	 * Creates an identifier with explicit namespace and path.
	 *
	 * @param namespace The namespace component
	 * @param path      The path component
	 * @return A new OpenIdentifier
	 */
	public static OpenIdentifier of(String namespace, String path) {
		return new OpenIdentifier(namespace, path);
	}

	/**
	 * Creates a Minecraft-namespaced identifier.
	 *
	 * @param path The path component
	 * @return A new OpenIdentifier with "minecraft" namespace
	 */
	public static OpenIdentifier minecraft(String path) {
		return new OpenIdentifier("minecraft", path);
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	/**
	 * Returns the final name segment of the path, without any leading directory structure or file extension.
	 *
	 * @return The name part of the identifier (e.g., "pickaxe_head" from "parts/heads/pickaxe_head.json").
	 */
	public String name() {
		return parsePath().name();
	}

	/**
	 * Returns a new OpenIdentifier that is the canonical form of this identifier.
	 * A canonical identifier has its path normalized to a single-group name
	 * (stripping any directory structure and file extension).
	 *
	 * @return A new OpenIdentifier instance representing the canonical ID.
	 */
	public OpenIdentifier toCanonical() {
		return new OpenIdentifier(this.namespace, name());
	}

	/**
	 * Normalizes a raw path segment by extracting only the final name and removing any file extension.
	 *
	 * @param rawPathSegment The raw path string, which may contain directories and a file extension.
	 * @return The normalized single-group name.
	 */
	public static String normalizePathSegment(String rawPathSegment) {
		return ParsedPath.parse(rawPathSegment).name();
	}

	/**
	 * Checks if this identifier matches a given string pattern based on the specified PatternType.
	 *
	 * @param type    The part of the identifier to match against.
	 * @param pattern The string pattern to test for equality.
	 * @return true if the specified part of the identifier equals the pattern, false otherwise.
	 */
	public boolean matches(PatternType type, String pattern) {
		return switch (type) {
			case NAMESPACE -> this.namespace.equals(pattern);
			case LOCATION -> this.path.equals(pattern);
			case PATH -> parsePath().directory().equals(pattern);
			case NAME -> parsePath().name().equals(pattern);
			case FILETYPE -> {
				final int firstSlash = this.path.indexOf('/');
				String category = firstSlash == -1 ? this.path : this.path.substring(0, firstSlash);
				yield category.equals(pattern);
			}
		};
	}

	/**
	 * Parses this identifier's path into its component parts.
	 */
	private ParsedPath parsePath() {
		return ParsedPath.parse(this.path);
	}

	/**
	 * Helper record for parsed path components. Eliminates duplicated parsing logic.
	 */
	private record ParsedPath(String directory, String fileName, String name) {
		static ParsedPath parse(String path) {
			int lastSlash = path.lastIndexOf('/');
			String directory = lastSlash == -1 ? "" : path.substring(0, lastSlash);
			String fileName = lastSlash == -1 ? path : path.substring(lastSlash + 1);
			int lastDot = fileName.lastIndexOf('.');
			String name = lastDot == -1 ? fileName : fileName.substring(0, lastDot);
			return new ParsedPath(directory, fileName, name);
		}
	}

	/**
	 * Returns a new identifier with a different namespace.
	 */
	public OpenIdentifier withNamespace(String newNamespace) {
		return new OpenIdentifier(newNamespace, this.path);
	}

	/**
	 * Returns a new identifier with a different path.
	 */
	public OpenIdentifier withPath(String newPath) {
		return new OpenIdentifier(this.namespace, newPath);
	}

	private static boolean isValidNamespace(String namespace) {
		if (namespace.isEmpty()) return false;
		for (int i = 0; i < namespace.length(); i++) {
			if (!isAllowedInNamespace(namespace.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidPath(String path) {
		if (path.isEmpty()) return false;
		for (int i = 0; i < path.length(); i++) {
			if (!isAllowedInPath(path.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAllowedInNamespace(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
	}

	public static boolean isAllowedInPath(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '/' || c == '.';
	}
}
