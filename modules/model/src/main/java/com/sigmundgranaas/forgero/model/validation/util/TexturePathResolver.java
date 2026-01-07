package com.sigmundgranaas.forgero.model.validation.util;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for resolving texture identifiers to actual file paths.
 * <p>
 * Handles the conversion from Forgero/Minecraft texture identifiers to filesystem paths,
 * supporting multiple content pack locations and standard texture directory structures.
 */
public class TexturePathResolver {
	
	private final List<Path> assetRoots;
	
	/**
	 * Creates a resolver with the given asset root paths.
	 * Each path should point to a directory containing namespace directories
	 * (e.g., path/assets/forgero/textures/).
	 *
	 * @param assetRoots List of asset root paths to search
	 */
	public TexturePathResolver(List<Path> assetRoots) {
		this.assetRoots = List.copyOf(assetRoots);
	}
	
	/**
	 * Resolves a texture identifier to an actual file path.
	 * <p>
	 * Examples:
	 * <ul>
	 *   <li>{@code forgero:item/diamond-gem} → {@code assets/forgero/textures/item/diamond-gem.png}</li>
	 *   <li>{@code forgero:texture_template/handle} → {@code assets/forgero/textures/texture_template/handle.png}</li>
	 *   <li>{@code forgero:palette/iron} → {@code assets/forgero/textures/palette/iron.png}</li>
	 * </ul>
	 *
	 * @param textureId The texture identifier (e.g., "forgero:item/diamond-gem")
	 * @return Optional containing the resolved path if found, empty otherwise
	 */
	public Optional<Path> resolveTexture(String textureId) {
		if (textureId == null || textureId.isEmpty()) {
			return Optional.empty();
		}
		
		OpenIdentifier id = OpenIdentifier.parse(textureId);
		return resolveTexture(id);
	}
	
	/**
	 * Resolves a texture identifier to an actual file path.
	 *
	 * @param textureId The parsed texture identifier
	 * @return Optional containing the resolved path if found, empty otherwise
	 */
	public Optional<Path> resolveTexture(OpenIdentifier textureId) {
		String namespace = textureId.namespace();
		String path = textureId.path();
		
		// Standard texture path: assets/{namespace}/textures/{path}.png
		String relativePath = String.format("assets/%s/textures/%s.png", namespace, path);
		
		for (Path assetRoot : assetRoots) {
			Path fullPath = assetRoot.resolve(relativePath);
			if (Files.exists(fullPath)) {
				return Optional.of(fullPath);
			}
			
			// Also check direct path under asset root (for src/main/resources structure)
			Path directPath = assetRoot.resolve(namespace).resolve("textures").resolve(path + ".png");
			if (Files.exists(directPath)) {
				return Optional.of(directPath);
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Resolves a palette identifier to an actual file path.
	 * <p>
	 * Palettes are typically stored in:
	 * <ul>
	 *   <li>{@code assets/forgero/textures/palette/{material}.png}</li>
	 *   <li>{@code assets/forgero/palettes/{material}.png}</li>
	 * </ul>
	 *
	 * @param paletteId The palette identifier (e.g., "forgero:palettes/iron" or just "iron")
	 * @return Optional containing the resolved path if found, empty otherwise
	 */
	public Optional<Path> resolvePalette(String paletteId) {
		if (paletteId == null || paletteId.isEmpty()) {
			return Optional.empty();
		}
		
		// Handle both full identifiers and simple names
		String namespace = "forgero";
		String path = paletteId;
		
		if (paletteId.contains(":")) {
			OpenIdentifier id = OpenIdentifier.parse(paletteId);
			namespace = id.namespace();
			path = id.path();
		}
		
		// Try multiple palette locations
		String[] possiblePaths = {
			String.format("assets/%s/textures/palette/%s.png", namespace, getLastSegment(path)),
			String.format("assets/%s/palettes/%s.png", namespace, getLastSegment(path)),
			String.format("assets/%s/textures/%s.png", namespace, path)
		};
		
		for (Path assetRoot : assetRoots) {
			for (String relativePath : possiblePaths) {
				Path fullPath = assetRoot.resolve(relativePath);
				if (Files.exists(fullPath)) {
					return Optional.of(fullPath);
				}
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Resolves a texture template identifier to an actual file path.
	 * <p>
	 * Templates are typically stored in:
	 * <ul>
	 *   <li>{@code assets/forgero/textures/texture_template/{part}.png}</li>
	 *   <li>{@code assets/forgero/templates/textures/{part}.png}</li>
	 * </ul>
	 *
	 * @param templateId The template identifier
	 * @return Optional containing the resolved path if found, empty otherwise
	 */
	public Optional<Path> resolveTemplate(String templateId) {
		if (templateId == null || templateId.isEmpty()) {
			return Optional.empty();
		}
		
		String namespace = "forgero";
		String path = templateId;
		
		if (templateId.contains(":")) {
			OpenIdentifier id = OpenIdentifier.parse(templateId);
			namespace = id.namespace();
			path = id.path();
		}
		
		// Try multiple template locations
		String[] possiblePaths = {
			String.format("assets/%s/textures/texture_template/%s.png", namespace, getLastSegment(path)),
			String.format("assets/%s/templates/textures/%s.png", namespace, getLastSegment(path)),
			String.format("assets/%s/textures/%s.png", namespace, path)
		};
		
		for (Path assetRoot : assetRoots) {
			for (String relativePath : possiblePaths) {
				Path fullPath = assetRoot.resolve(relativePath);
				if (Files.exists(fullPath)) {
					return Optional.of(fullPath);
				}
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Checks if a texture exists at the given identifier.
	 *
	 * @param textureId The texture identifier
	 * @return true if the texture file exists, false otherwise
	 */
	public boolean textureExists(String textureId) {
		return resolveTexture(textureId).isPresent();
	}
	
	/**
	 * Checks if a palette exists at the given identifier.
	 *
	 * @param paletteId The palette identifier
	 * @return true if the palette file exists, false otherwise
	 */
	public boolean paletteExists(String paletteId) {
		return resolvePalette(paletteId).isPresent();
	}
	
	/**
	 * Checks if a template exists at the given identifier.
	 *
	 * @param templateId The template identifier
	 * @return true if the template file exists, false otherwise
	 */
	public boolean templateExists(String templateId) {
		return resolveTemplate(templateId).isPresent();
	}
	
	/**
	 * Gets the expected path for a texture (without checking existence).
	 * Useful for error messages.
	 *
	 * @param textureId The texture identifier
	 * @return The expected relative path
	 */
	public String getExpectedTexturePath(String textureId) {
		if (textureId == null || textureId.isEmpty()) {
			return "<empty>";
		}
		
		OpenIdentifier id = OpenIdentifier.parse(textureId);
		return String.format("assets/%s/textures/%s.png", id.namespace(), id.path());
	}
	
	/**
	 * Gets the expected path for a palette (without checking existence).
	 *
	 * @param paletteId The palette identifier or material name
	 * @return The expected relative path
	 */
	public String getExpectedPalettePath(String paletteId) {
		if (paletteId == null || paletteId.isEmpty()) {
			return "<empty>";
		}
		
		String name = getLastSegment(paletteId);
		if (paletteId.contains(":")) {
			OpenIdentifier id = OpenIdentifier.parse(paletteId);
			return String.format("assets/%s/textures/palette/%s.png", id.namespace(), name);
		}
		return String.format("assets/forgero/textures/palette/%s.png", name);
	}
	
	private String getLastSegment(String path) {
		if (path.contains("/")) {
			return path.substring(path.lastIndexOf('/') + 1);
		}
		return path;
	}
}
