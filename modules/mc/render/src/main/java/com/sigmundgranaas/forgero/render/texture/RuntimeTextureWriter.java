package com.sigmundgranaas.forgero.render.texture;

import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.model.texture.api.TextureWriter;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RuntimeTextureWriter implements TextureWriter {
	public static final Logger LOGGER = LoggerFactory.getLogger(RuntimeTextureWriter.class);
	private final DynamicResourcePack resourcePack;

	public RuntimeTextureWriter(DynamicResourcePack resourcePack) {
		this.resourcePack = resourcePack;
	}

	@Override
	public void write(BufferedImage image, String path) {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			ImageIO.write(image, "PNG", stream);
			byte[] bytes = stream.toByteArray();

			// The path from the generator is like "assets/forgero/item/diamond-boots.png"
			// We need to convert this to a Minecraft Identifier "forgero:item/diamond-boots"
			String identifierPath = path.replace("assets/", "");
			String[] parts = identifierPath.split("/", 2);
			if (parts.length == 2) {
				Identifier id = new Identifier(parts[0], "textures/" + parts[1]);
				resourcePack.addRawAsset(id, bytes);
				LOGGER.trace("Generated and added texture to runtime resource pack: {}", id);
			} else {
				LOGGER.warn("Could not form valid identifier from texture path: {}", path);
			}

		} catch (IOException e) {
			LOGGER.error("Failed to write texture to runtime resource pack for path: {}", path, e);
		}
	}
}
