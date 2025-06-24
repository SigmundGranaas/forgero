package com.sigmundgranaas.forgero.core.identifier.api;

import java.util.Objects;

public record OpenIdentifier(String namespace, String path) {

	public OpenIdentifier {
		Objects.requireNonNull(namespace);
		Objects.requireNonNull(path);

		if (!isValidNamespace(namespace)) {
			throw new IllegalArgumentException("Invalid namespace: '" + namespace + "'. Must be lowercase alphanumeric, '_', or '-'.");
		}
		if (!isValidPath(path)) {
			throw new IllegalArgumentException("Invalid path: '" + path + "'. Must be lowercase alphanumeric, '_', '-', '/', or '.'.");
		}
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	/**
	 * Checks if this identifier matches a given string pattern based on the specified PatternType.
	 *
	 * @param type    The part of the identifier to match against.
	 * @param pattern The string pattern to test for equality.
	 * @return true if the specified part of the identifier equals the pattern, false otherwise.
	 */
	public boolean matches(PatternType type, String pattern) {
		final int lastSlash = this.path.lastIndexOf('/');
		final String fileName = lastSlash == -1 ? this.path : this.path.substring(lastSlash + 1);
		final int lastDot = fileName.lastIndexOf('.');

		return switch (type) {
			case NAMESPACE -> this.namespace.equals(pattern);
			case LOCATION -> this.path.equals(pattern);
			case PATH -> {
				String pathOnly = lastSlash == -1 ? "" : this.path.substring(0, lastSlash);
				yield pathOnly.equals(pattern);
			}
			case NAME -> {
				String nameOnly = lastDot == -1 ? fileName : fileName.substring(0, lastDot);
				yield nameOnly.equals(pattern);
			}
			case FILETYPE -> {
				// Corrected logic: FILETYPE refers to the first segment of the path, not the extension.
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
