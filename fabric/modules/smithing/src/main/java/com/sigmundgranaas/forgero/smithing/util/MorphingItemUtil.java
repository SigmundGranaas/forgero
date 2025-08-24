package com.sigmundgranaas.forgero.smithing.util;

import java.awt.image.BufferedImage;
import java.util.Optional;

import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class MorphingItemUtil {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroMorphingItemUtil");

	public static BufferedImage getStartImage(ItemStack stack) {
		if (!stack.hasNbt()) return null;
		String id = stack.getNbt().getString("morphStart");
		LOGGER.trace("MorphingItemUtil.getStartImage id={}", id);
		return getItemTextureAsImage(id);
	}

	public static BufferedImage getResultImage(ItemStack stack) {
		if (!stack.hasNbt()) return null;
		String id = stack.getNbt().getString("morphResult");
		LOGGER.trace("MorphingItemUtil.getResultImage id={}", id);
		return getItemTextureAsImage(id);
	}

	private static BufferedImage getItemTextureAsImage(String id) {
		if (id == null || id.isEmpty()) return null;
		try {
			Identifier itemId = new Identifier(id);
			Identifier texId = new Identifier(itemId.getNamespace(), "textures/item/" + itemId.getPath() + ".png");
			Optional<Resource> res = MinecraftClient.getInstance().getResourceManager().getResource(texId);
			if (res.isPresent()) {
				try (var in = res.get().getInputStream()) {
					LOGGER.trace("Loaded direct item texture {}", texId);
					return javax.imageio.ImageIO.read(in);
				}
			}
			// Try registered item -> baked model path
			Optional<Item> itemOpt = Registries.ITEM.getOrEmpty(itemId);
			if (itemOpt.isPresent()) {
				ItemStack tmp = new ItemStack(itemOpt.get());
				BufferedImage img = RuntimeModelUtil.getFirstQuadTextureImage(tmp, MinecraftClient.getInstance());
				if (img != null) {
					LOGGER.trace("Extracted texture from baked model for item {}", itemId);
					return img;
				}
			}
			// Try Forgero State -> item stack -> baked model path
			var maybeState = StateService.INSTANCE.find(id);
			if (maybeState.isPresent()) {
				var stackOpt = StateService.INSTANCE.convert(maybeState.get());
				if (stackOpt.isPresent() && !stackOpt.get().isEmpty()) {
					BufferedImage img = RuntimeModelUtil.getFirstQuadTextureImage(stackOpt.get(), MinecraftClient.getInstance());
					if (img != null) {
						LOGGER.trace("Extracted texture from baked model via StateService for id {}", id);
						return img;
					}
				}
			}
			LOGGER.debug("Failed to resolve texture for id {}", id);
			return null;
		} catch (Exception e) {
			// Final fallback: StateService route if identifier parsing failed
			try {
				var maybeState = StateService.INSTANCE.find(id);
				if (maybeState.isPresent()) {
					var stackOpt = StateService.INSTANCE.convert(maybeState.get());
					if (stackOpt.isPresent() && !stackOpt.get().isEmpty()) {
						LOGGER.trace("Extracted texture from baked model via StateService (exception path) for id {}", id);
						return RuntimeModelUtil.getFirstQuadTextureImage(stackOpt.get(), MinecraftClient.getInstance());
					}
				}
			} catch (Exception ignored) {}
			LOGGER.warn("Exception resolving texture for id {}: {}", id, e.toString());
			return null;
		}
	}
}
