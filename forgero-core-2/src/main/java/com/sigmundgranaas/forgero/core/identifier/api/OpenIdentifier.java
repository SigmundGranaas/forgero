// File: /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/main/java/com/sigmundgranaas/forgero/core/identifier/api/OpenIdentifier.java
package com.sigmundgranaas.forgero.core.identifier.api;

import java.util.Objects;

public record OpenIdentifier(String namespace, String path) {

	public OpenIdentifier {
		Objects.requireNonNull(namespace);
		Objects.requireNonNull(path);

		if (!isValidNamespace(namespace)) {
			throw new IllegalArgumentException("Invalid namespace: '" + namespace + "'. Must be lowercase alphanumeric, '_', or '-'.");
		}
		// The path field can now contain slashes and dots, as it represents a resource path.
		if (!isValidPath(path)) {
			throw new IllegalArgumentException("Invalid path: '" + path + "'. Must be lowercase alphanumeric, '_', '-', '/', or '.'.");
		}
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	/**
	 * Returns the final name segment of the path, without any leading directory structure or file extension.
	 * This is the original behavior for extracting the 'filename'.
	 *
	 * @return The name part of the identifier (e.g., "pickaxe_head" from "parts/heads/pickaxe_head.json").
	 */
	public String name() {
		final int lastSlash = this.path.lastIndexOf('/');
		final String fileName = lastSlash == -1 ? this.path : this.path.substring(lastSlash + 1);
		final int lastDot = fileName.lastIndexOf('.');
		return lastDot == -1 ? fileName : fileName.substring(0, lastDot);
	}

	/**
	 * Returns a new OpenIdentifier that is the canonical form of this identifier.
	 * A canonical identifier has its path normalized to a single-group name
	 * (stripping any directory structure and file extension).
	 *
	 * @return A new OpenIdentifier instance representing the canonical ID.
	 */
	public OpenIdentifier toCanonical() {
		// Use normalizePathSegment to ensure the path is a single-group name.
		return new OpenIdentifier(this.namespace, normalizePathSegment(this.path));
	}

	/**
	 * Normalizes a raw path segment by extracting only the final name and removing any file extension.
	 * This is used to create the canonical 'path' part of an OpenIdentifier.
	 * Examples:
	 * - "materials/iron.json" -> "iron"
	 * - "pickaxe_head.json" -> "pickaxe_head"
	 * - "some/path/my_item" -> "my_item"
	 * - "just_name" -> "just_name"
	 * - "long/path/with.dots.in.name.json" -> "with.dots.in.name" (the last segment, without its extension)
	 *
	 * @param rawPathSegment The raw path string, which may contain directories and a file extension.
	 * @return The normalized single-group name.
	 */
	public static String normalizePathSegment(String rawPathSegment) {
		// First, get the part after the last slash (filename or last segment)
		final int lastSlash = rawPathSegment.lastIndexOf('/');
		String fileNameOrLastSegment = lastSlash == -1 ? rawPathSegment : rawPathSegment.substring(lastSlash + 1);

		// Then, remove the last dot and everything after it (file extension)
		final int lastDot = fileNameOrLastSegment.lastIndexOf('.');
		return lastDot == -1 ? fileNameOrLastSegment : fileNameOrLastSegment.substring(0, lastDot);
	}

	/**
	 * Checks if this identifier matches a given string pattern based on the specified PatternType.
	 *
	 * @param type    The part of the identifier to match against.
	 * @param pattern The string pattern to test for equality.
	 * @return true if the specified part of the identifier equals the pattern, false otherwise.
	 */
	public boolean matches(PatternType type, String pattern) {
		// These local variables are based on the *raw* path, not the canonicalized one.
		final int lastSlash = this.path.lastIndexOf('/');
		final String fileName = lastSlash == -1 ? this.path : this.path.substring(lastSlash + 1);
		final int lastDot = fileName.lastIndexOf('.');

		return switch (type) {
			case NAMESPACE -> this.namespace.equals(pattern);
			case LOCATION -> this.path.equals(pattern); // Full path including directories and filetype
			case PATH -> { // Directory structure of the path, excluding the filename
				String pathOnly = lastSlash == -1 ? "" : this.path.substring(0, lastSlash);
				yield pathOnly.equals(pattern);
			}
			case NAME -> { // The final filename without extension (e.g., "pickaxe_head")
				String nameOnly = lastDot == -1 ? fileName : fileName.substring(0, lastDot);
				yield nameOnly.equals(pattern);
			}
			case FILETYPE -> { // The first segment of the path (e.g., "materials", "parts")
				final int firstSlash = this.path.indexOf('/');
				String category = firstSlash == -1 ? this.path : this.path.substring(0, firstSlash);
				yield category.equals(pattern);
			}
		};
	}

	private static boolean isValidNamespace(String namespace) {
		for (int i = 0; i < namespace.length(); ++i) {
			char c = namespace.charAt(i);
			if (!isAllowedInNamespace(c)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidPath(String path) {
		for (int i = 0; i < path.length(); ++i) {
			char c = path.charAt(i);
			if (!isAllowedInPath(c)) {
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
