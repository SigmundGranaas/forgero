package com.sigmundgranaas.forgero.render.texture;

import com.sigmundgranaas.forgero.model.rendering.api.TextureProvider;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class MinecraftResourceTextureProvider implements TextureProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftResourceTextureProvider.class);

	public MinecraftResourceTextureProvider() {
		// Constructor is now empty. We no longer cache the resource manager.
	}

	@Override
	public Optional<BufferedImage> getTexture(String identifier) {
		Identifier resourceIdentifier = toResourceId(identifier);
		if (resourceIdentifier == null) {
			return Optional.empty();
		}

		// Fetch the resource manager just-in-time to ensure it's always the most current one.
		ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

		return resourceManager.getResource(resourceIdentifier).flatMap(resource -> {
			try (InputStream inputStream = resource.getInputStream()) {
				BufferedImage image = ImageIO.read(inputStream);
				if (image == null) {
					LOGGER.warn("Failed to read image data for texture, ImageIO.read returned null: {}", resourceIdentifier);
					return Optional.empty();
				}
				return Optional.of(image);
			} catch (IOException e) {
				LOGGER.error("Failed to read texture input stream for: {}", resourceIdentifier, e);
				return Optional.empty();
			}
		});
	}

	/**
	 * Converts a Forgero texture string (e.g., "minecraft:item/diamond") into a
	 * full resource Identifier that points to the PNG file (e.g., "minecraft:textures/item/diamond.png").
	 */
	private Identifier toResourceId(String identifier) {
		String[] parts = identifier.split(":", 2);
		if (parts.length != 2) {
			LOGGER.warn("Invalid texture identifier format: {}. Expected 'namespace:path'.", identifier);
			return null;
		}
		return new Identifier(parts[0], "textures/" + parts[1] + ".png");
	}
}
