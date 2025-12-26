package com.sigmundgranaas.forgero.model.texture.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.texture.dto.AnimationMetadataDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Utility for detecting animation metadata by looking for .mcmeta files
 * alongside texture/palette resources.
 * Uses the codec-based approach consistent with the rest of the system.
 */
public class AnimationDetector {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnimationDetector.class);
	private static final String MCMETA_EXTENSION = ".mcmeta";

	private final ResourceProvider resourceProvider;

	/**
	 * Creates a new animation detector.
	 *
	 * @param resourceProvider The provider to use for loading .mcmeta files.
	 */
	public AnimationDetector(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
	}

	/**
	 * Detects animation metadata for a texture by looking for a .mcmeta file.
	 *
	 * @param textureIdentifier The texture identifier (e.g., "forgero:textures/item/foo.png").
	 * @return The animation metadata if found and valid, empty otherwise.
	 */
	public Optional<AnimationMetadataDTO> detectAnimation(String textureIdentifier) {
		String mcmetaPath = getMcmetaPath(textureIdentifier);
		return loadMetadata(mcmetaPath);
	}

	/**
	 * Detects animation metadata for a texture using an OpenIdentifier.
	 *
	 * @param textureId The texture identifier.
	 * @return The animation metadata if found and valid, empty otherwise.
	 */
	public Optional<AnimationMetadataDTO> detectAnimation(OpenIdentifier textureId) {
		// Append .mcmeta to the path
		String mcmetaPath = textureId.path() + MCMETA_EXTENSION;
		OpenIdentifier mcmetaId = new OpenIdentifier(textureId.namespace(), mcmetaPath);
		return loadMetadata(mcmetaId);
	}

	private String getMcmetaPath(String textureIdentifier) {
		// Handle both "namespace:path" and plain path formats
		String[] parts = textureIdentifier.split(":", 2);
		if (parts.length == 2) {
			String namespace = parts[0];
			String path = parts[1];

			// Ensure path has .png extension before adding .mcmeta
			if (!path.endsWith(".png")) {
				path = path + ".png";
			}
			return namespace + ":" + path + MCMETA_EXTENSION;
		}
		// Plain path
		if (!textureIdentifier.endsWith(".png")) {
			textureIdentifier = textureIdentifier + ".png";
		}
		return textureIdentifier + MCMETA_EXTENSION;
	}

	private Optional<AnimationMetadataDTO> loadMetadata(String identifier) {
		String[] parts = identifier.split(":", 2);
		if (parts.length != 2) {
			return Optional.empty();
		}

		String namespace = parts[0];
		String path = parts[1];

		// Normalize path for different resource locations
		if (!path.startsWith("textures/") && !path.startsWith("texture_templates/") && !path.startsWith("palettes/")) {
			path = "textures/" + path;
		}

		OpenIdentifier mcmetaId = new OpenIdentifier(namespace, path);
		return loadMetadata(mcmetaId);
	}

	private Optional<AnimationMetadataDTO> loadMetadata(OpenIdentifier mcmetaId) {
		try {
			Optional<InputStream> streamOpt = resourceProvider.read(mcmetaId);
			if (streamOpt.isEmpty()) {
				LOGGER.trace("No mcmeta file found at: {}", mcmetaId);
				return Optional.empty();
			}

			try (InputStream stream = streamOpt.get();
			     InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				JsonElement json = JsonParser.parseReader(reader);
				return AnimationMetadataDTO.CODEC.parse(JsonOps.INSTANCE, json)
						.resultOrPartial(error -> LOGGER.debug("Failed to parse mcmeta at {}: {}", mcmetaId, error));
			}
		} catch (JsonSyntaxException e) {
			LOGGER.debug("Invalid JSON syntax in mcmeta file {}: {}", mcmetaId, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			LOGGER.debug("Failed to load animation metadata from {}: {}", mcmetaId, e.getMessage());
			return Optional.empty();
		}
	}
}
