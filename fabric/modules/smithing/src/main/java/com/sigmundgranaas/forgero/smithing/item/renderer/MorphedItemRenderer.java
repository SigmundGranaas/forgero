package com.sigmundgranaas.forgero.smithing.item.renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;

import com.sigmundgranaas.forgero.smithing.util.MorphingItemUtil;
import com.sigmundgranaas.forgero.smithing.util.PositionPreservingMorpher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

@Environment(EnvType.CLIENT)
public class MorphedItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroMorphedItemRenderer");
	private final PositionPreservingMorpher morpher = new PositionPreservingMorpher();
	private final Map<ItemStack, MorphCache> morphCache = new WeakHashMap<>();

	private static final double WEIGHT_TOLERANCE = 0.001;
	private static final Identifier PLACEHOLDER_TEX = new Identifier("forgero", "textures/item/morphed_item.png");

	private static class MorphCache {
		double lastWeight = -1.0;
		BufferedImage lastImage = null;
		BufferedImage lastStart = null;
		BufferedImage lastResult = null;
		NativeImageBackedTexture lastDynamicTexture = null;
		Identifier lastTextureId = null;
	}

	@Override
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		LOGGER.info("MorphedItemRenderer DIR invoked for stack={} mode={}", stack.getItem(), mode);
		if (stack.isEmpty()) return;

		NbtCompound nbt = stack.getOrCreateNbt();
		double weight = nbt.contains("morphProgress") ? nbt.getDouble("morphProgress") : 0.0;

		BufferedImage startImage = MorphingItemUtil.getStartImage(stack);
		BufferedImage resultImage = MorphingItemUtil.getResultImage(stack);

		// Graceful fallback: if only one is available, use it for both to avoid placeholder
		if (startImage == null && resultImage != null) {
			startImage = resultImage;
		}
		if (resultImage == null && startImage != null) {
			resultImage = startImage;
		}

		if (startImage == null || resultImage == null) {
			LOGGER.debug("MorphedItem: missing images (start={}, result={}) for stack={} nbt={}", startImage != null, resultImage != null, stack.getItem(), nbt);
			// Render placeholder via our own quad to avoid recursion
			renderTexturedQuad(matrices, vertexConsumers, mode, PLACEHOLDER_TEX, light, overlay);
			return;
		}

		MorphCache cache = morphCache.computeIfAbsent(stack, k -> new MorphCache());

		boolean needsUpdate = cache.lastImage == null
				|| Math.abs(cache.lastWeight - weight) > WEIGHT_TOLERANCE
				|| cache.lastStart != startImage
				|| cache.lastResult != resultImage;

		if (needsUpdate) {
			cache.lastStart = startImage;
			cache.lastResult = resultImage;
			cache.lastWeight = weight;

			BufferedImage[] padded = centerPadToSameSize(startImage, resultImage);
			cache.lastImage = morpher.morphStep(padded[0], padded[1], weight);

			NativeImage nativeImage = bufferedImageToNativeImage(cache.lastImage);
			if (cache.lastDynamicTexture != null) {
				try { cache.lastDynamicTexture.close(); } catch (Exception ignored) {}
			}
			cache.lastDynamicTexture = new NativeImageBackedTexture(nativeImage);
			cache.lastTextureId = MinecraftClient.getInstance().getTextureManager()
					.registerDynamicTexture("forgero_morph_" + stack.hashCode() + "_" + System.currentTimeMillis(),
							cache.lastDynamicTexture);
			LOGGER.debug("MorphedItem: built dynamic texture {} ({}x{})", cache.lastTextureId, nativeImage.getWidth(), nativeImage.getHeight());
		}

		if (cache.lastTextureId != null && cache.lastDynamicTexture != null) {
			LOGGER.trace("MorphedItem: rendering with dynamic texture {}", cache.lastTextureId);
			renderTexturedQuad(matrices, vertexConsumers, mode, cache.lastTextureId, light, overlay);
		} else {
			LOGGER.debug("MorphedItem: dynamic texture not ready, using placeholder");
			// Render placeholder if dynamic texture isn't ready yet
			renderTexturedQuad(matrices, vertexConsumers, mode, PLACEHOLDER_TEX, light, overlay);
		}
	}

	private void renderTexturedQuad(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ModelTransformationMode mode,
									 Identifier textureId, int light, int overlay) {
		matrices.push();
		// Apply a simple transform tuned for item display modes
		applyModeTransform(matrices, mode);

		var layer = net.minecraft.client.render.RenderLayer.getEntityCutout(textureId);
		var vertexConsumer = vertexConsumers.getBuffer(layer);
		renderDynamicTextureQuad(matrices, vertexConsumer, light, overlay);

		matrices.pop();
	}

	private void applyModeTransform(MatrixStack matrices, ModelTransformationMode mode) {
		switch (mode) {
			case GUI -> {
				// Center and scale quad for inventory slot (normalized 1x1)
				matrices.translate(0.5f, 0.5f, 0.0f);
				matrices.scale(0.8f, 0.8f, 0.8f); // Slightly smaller for padding
			}
			case GROUND -> {
				matrices.scale(0.5f, 0.5f, 0.5f);
			}
			case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
				matrices.scale(1.0f, 1.0f, 1.0f);
			}
			default -> {
				// No-op for other modes
			}
		}
	}

	private void renderDynamicTextureQuad(MatrixStack matrices, net.minecraft.client.render.VertexConsumer vertexConsumer, int light, int overlay) {
		var matrix = matrices.peek().getPositionMatrix();
		var normalMatrix = matrices.peek().getNormalMatrix();

		float size = 1.0f;

		vertexConsumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).texture(0.0f, 1.0f).overlay(overlay).light(light).normal(normalMatrix, 0.0f, 0.0f, 1.0f).next();
		vertexConsumer.vertex(matrix, size, 0, 0).color(255, 255, 255, 255).texture(1.0f, 1.0f).overlay(overlay).light(light).normal(normalMatrix, 0.0f, 0.0f, 1.0f).next();
		vertexConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255).texture(1.0f, 0.0f).overlay(overlay).light(light).normal(normalMatrix, 0.0f, 0.0f, 1.0f).next();
		vertexConsumer.vertex(matrix, 0, size, 0).color(255, 255, 255, 255).texture(0.0f, 0.0f).overlay(overlay).light(light).normal(normalMatrix, 0.0f, 0.0f, 1.0f).next();
	}

	// --- Util Methods ---

	private NativeImage bufferedImageToNativeImage(BufferedImage image) {
		int w = image.getWidth(), h = image.getHeight();
		NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, w, h, false);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int argb = image.getRGB(x, y);
				int a = (argb >>> 24) & 0xFF;
				int r = (argb >>> 16) & 0xFF;
				int g = (argb >>> 8) & 0xFF;
				int b = (argb) & 0xFF;
				int abgr = (a << 24) | (b << 16) | (g << 8) | r;
				nativeImage.setColor(x, y, abgr);
			}
		}
		return nativeImage;
	}

	private BufferedImage[] centerPadToSameSize(BufferedImage a, BufferedImage b) {
		int w = Math.max(a.getWidth(), b.getWidth());
		int h = Math.max(a.getHeight(), b.getHeight());
		if (a.getWidth() == w && a.getHeight() == h && b.getWidth() == w && b.getHeight() == h) {
			return new BufferedImage[]{a, b};
		}
		BufferedImage aa = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bb = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ga = aa.createGraphics();
		Graphics2D gb = bb.createGraphics();
		ga.drawImage(a, (w - a.getWidth()) / 2, (h - a.getHeight()) / 2, null);
		gb.drawImage(b, (w - b.getWidth()) / 2, (h - b.getHeight()) / 2, null);
		ga.dispose();
		gb.dispose();
		return new BufferedImage[]{aa, bb};
	}
}
