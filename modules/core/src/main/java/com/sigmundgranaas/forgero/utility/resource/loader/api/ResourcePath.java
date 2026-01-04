package com.sigmundgranaas.forgero.utility.resource.loader.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Objects;

/**
 * A structured path abstraction for resource locations.
 * Provides consistent path manipulation and normalization across all providers.
 * <p>
 * Unlike raw string paths, ResourcePath separates the path into logical components:
 * namespace, directory, filename, and extension. This enables:
 * <ul>
 *   <li>Type-safe path manipulation</li>
 *   <li>Consistent filtering by extension or directory</li>
 *   <li>Clean conversion to/from OpenIdentifier</li>
 *   <li>Cross-platform path handling (no OS-specific separators)</li>
 * </ul>
 *
 * @param namespace The resource namespace (e.g., "forgero", "minecraft")
 * @param directory The directory path within the namespace (e.g., "materials/metal")
 * @param fileName  The file name without extension (e.g., "iron")
 * @param extension The file extension without dot (e.g., "json", "png")
 */
public record ResourcePath(
		String namespace,
		String directory,
		String fileName,
		String extension
) {

	/**
	 * Canonical constructor with validation.
	 */
	public ResourcePath {
		Objects.requireNonNull(namespace, "namespace cannot be null");
		Objects.requireNonNull(directory, "directory cannot be null");
		Objects.requireNonNull(fileName, "fileName cannot be null");
		Objects.requireNonNull(extension, "extension cannot be null");

		if (namespace.isEmpty()) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		// Normalize directory separators and remove leading/trailing slashes
		directory = normalizeDirectory(directory);
	}

	/**
	 * Creates a ResourcePath from an OpenIdentifier.
	 * Parses the path component to extract directory, filename, and extension.
	 *
	 * @param id The identifier to convert
	 * @return A new ResourcePath
	 */
	public static ResourcePath fromIdentifier(OpenIdentifier id) {
		Objects.requireNonNull(id, "id cannot be null");

		String path = id.path();
		int lastSlash = path.lastIndexOf('/');
		String directory = lastSlash == -1 ? "" : path.substring(0, lastSlash);
		String fileWithExt = lastSlash == -1 ? path : path.substring(lastSlash + 1);

		int lastDot = fileWithExt.lastIndexOf('.');
		String fileName = lastDot == -1 ? fileWithExt : fileWithExt.substring(0, lastDot);
		String extension = lastDot == -1 ? "" : fileWithExt.substring(lastDot + 1);

		return new ResourcePath(id.namespace(), directory, fileName, extension);
	}

	/**
	 * Creates a ResourcePath for directory listing (no file/extension).
	 * Use this when you want to list resources within a directory.
	 *
	 * @param namespace The namespace
	 * @param directory The directory path
	 * @return A ResourcePath representing the directory
	 */
	public static ResourcePath directory(String namespace, String directory) {
		return new ResourcePath(namespace, directory, "", "");
	}

	/**
	 * Creates a ResourcePath for a specific file.
	 *
	 * @param namespace The namespace
	 * @param directory The directory path
	 * @param fileName  The file name without extension
	 * @param extension The file extension without dot
	 * @return A new ResourcePath
	 */
	public static ResourcePath file(String namespace, String directory, String fileName, String extension) {
		return new ResourcePath(namespace, directory, fileName, extension);
	}

	/**
	 * Parses a full path string in the format "namespace:directory/file.ext".
	 *
	 * @param fullPath The full path string
	 * @return A new ResourcePath
	 */
	public static ResourcePath parse(String fullPath) {
		return fromIdentifier(OpenIdentifier.parse(fullPath));
	}

	/**
	 * Converts this ResourcePath back to an OpenIdentifier.
	 *
	 * @return An OpenIdentifier representing this path
	 */
	public OpenIdentifier toIdentifier() {
		return new OpenIdentifier(namespace, fullPath());
	}

	/**
	 * Returns the full path without namespace.
	 * Combines directory, filename, and extension.
	 *
	 * @return The full path (e.g., "materials/metal/iron.json")
	 */
	public String fullPath() {
		StringBuilder sb = new StringBuilder();
		if (!directory.isEmpty()) {
			sb.append(directory);
			if (!fileName.isEmpty()) {
				sb.append("/");
			}
		}
		if (!fileName.isEmpty()) {
			sb.append(fileName);
			if (!extension.isEmpty()) {
				sb.append(".").append(extension);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the file name with extension.
	 *
	 * @return The file name with extension (e.g., "iron.json")
	 */
	public String fileNameWithExtension() {
		if (fileName.isEmpty()) {
			return "";
		}
		return extension.isEmpty() ? fileName : fileName + "." + extension;
	}

	/**
	 * Checks if this path has the specified extension (case-insensitive).
	 *
	 * @param ext The extension to check (without dot)
	 * @return true if the extension matches
	 */
	public boolean hasExtension(String ext) {
		return this.extension.equalsIgnoreCase(ext);
	}

	/**
	 * Checks if this path is within the specified directory.
	 *
	 * @param dir The directory to check
	 * @return true if this path is within the directory
	 */
	public boolean isInDirectory(String dir) {
		if (dir.isEmpty()) {
			return true;
		}
		String normalizedDir = normalizeDirectory(dir);
		return this.directory.equals(normalizedDir) ||
				this.directory.startsWith(normalizedDir + "/");
	}

	/**
	 * Checks if this is a directory path (no file name or extension).
	 *
	 * @return true if this represents a directory
	 */
	public boolean isDirectory() {
		return fileName.isEmpty() && extension.isEmpty();
	}

	/**
	 * Checks if this is a file path (has file name).
	 *
	 * @return true if this represents a file
	 */
	public boolean isFile() {
		return !fileName.isEmpty();
	}

	/**
	 * Creates a new ResourcePath with a different namespace.
	 *
	 * @param newNamespace The new namespace
	 * @return A new ResourcePath with the updated namespace
	 */
	public ResourcePath withNamespace(String newNamespace) {
		return new ResourcePath(newNamespace, directory, fileName, extension);
	}

	/**
	 * Creates a new ResourcePath with a different directory.
	 *
	 * @param newDirectory The new directory
	 * @return A new ResourcePath with the updated directory
	 */
	public ResourcePath withDirectory(String newDirectory) {
		return new ResourcePath(namespace, newDirectory, fileName, extension);
	}

	/**
	 * Creates a new ResourcePath with a different file name.
	 *
	 * @param newFileName The new file name
	 * @return A new ResourcePath with the updated file name
	 */
	public ResourcePath withFileName(String newFileName) {
		return new ResourcePath(namespace, directory, newFileName, extension);
	}

	/**
	 * Creates a new ResourcePath with a different extension.
	 *
	 * @param newExtension The new extension
	 * @return A new ResourcePath with the updated extension
	 */
	public ResourcePath withExtension(String newExtension) {
		return new ResourcePath(namespace, directory, fileName, newExtension);
	}

	/**
	 * Creates a child path under this directory.
	 * Only works if this is a directory path.
	 *
	 * @param subPath The sub-path to append
	 * @return A new ResourcePath with the child directory
	 * @throws IllegalStateException if this is not a directory path
	 */
	public ResourcePath child(String subPath) {
		if (!isDirectory()) {
			throw new IllegalStateException("Cannot create child of a file path: " + this);
		}
		String newDir = directory.isEmpty() ? subPath : directory + "/" + subPath;
		return directory(namespace, newDir);
	}

	/**
	 * Gets the parent directory of this path.
	 *
	 * @return A ResourcePath representing the parent directory, or empty directory if at root
	 */
	public ResourcePath parent() {
		if (isFile()) {
			return directory(namespace, directory);
		}
		int lastSlash = directory.lastIndexOf('/');
		if (lastSlash == -1) {
			return directory(namespace, "");
		}
		return directory(namespace, directory.substring(0, lastSlash));
	}

	/**
	 * Returns the depth of this path (number of directory levels).
	 *
	 * @return The depth (0 for root-level files)
	 */
	public int depth() {
		if (directory.isEmpty()) {
			return 0;
		}
		return (int) directory.chars().filter(ch -> ch == '/').count() + 1;
	}

	@Override
	public String toString() {
		return namespace + ":" + fullPath();
	}

	/**
	 * Normalizes a directory path by:
	 * - Converting backslashes to forward slashes
	 * - Removing leading/trailing slashes
	 * - Collapsing multiple consecutive slashes
	 */
	private static String normalizeDirectory(String dir) {
		if (dir == null || dir.isEmpty()) {
			return "";
		}
		return dir
				.replace('\\', '/')
				.replaceAll("^/+|/+$", "")
				.replaceAll("/+", "/");
	}
}
