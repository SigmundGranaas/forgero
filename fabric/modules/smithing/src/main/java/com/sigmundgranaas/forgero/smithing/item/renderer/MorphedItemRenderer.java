package com.sigmundgranaas.forgero.smithing.item.renderer;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;

import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameLogic;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;
import com.sigmundgranaas.forgero.smithing.util.PositionPreservingMorpher;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;


// TODO find correct transformations for different render modes

@Environment(EnvType.CLIENT)
public class MorphedItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroMorphedItemRenderer");
	private static final PositionPreservingMorpher MORPHER = new PositionPreservingMorpher();

	private static final double MORPH_START_THRESHOLD = 0.4;
	private static final double MORPH_RESULT_THRESHOLD = 0.6;
	private static final double CACHE_PRECISION = 20.0;
	private static final double CACHE_TOLERANCE = 0.01;

	private static final Map<String, MorphTextureCache> morphTextureCache = new WeakHashMap<>();

	private static class MorphTextureCache {
		NativeImageBackedTexture dynamicTexture;
		Identifier textureId;
		BufferedImage morphImage;
		double lastProgress;

		MorphTextureCache(NativeImageBackedTexture texture, Identifier id, BufferedImage image, double progress) {
			this.dynamicTexture = texture;
			this.textureId = id;
			this.morphImage = image;
			this.lastProgress = progress;
		}

		void cleanup() {
			if (dynamicTexture != null) {
				dynamicTexture.close();
			}
		}
	}

	@Override
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();
		applyTransformation(mode, matrices);

		NbtCompound nbt = stack.getOrCreateNbt();
		double progress = nbt.getDouble(MorphedItem.PROGRESS_KEY);

		if (!renderMorphed3D(stack, progress, matrices, vertexConsumers, light, overlay)) {
			renderFallback(stack, progress, mode, matrices, vertexConsumers, light, overlay);
		}

		matrices.pop();
	}

	private void applyTransformation(ModelTransformationMode mode, MatrixStack matrices) {
		switch (mode) {
			case GUI -> {
				matrices.translate(0.5, 0.5, 0);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
			case FIXED -> {
				matrices.translate(0.5, 0.5, 0.5);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
			case FIRST_PERSON_RIGHT_HAND -> {
				matrices.translate(0.5F, 0.75F, 0.65F);
				matrices.scale(0.60f, 0.60f, 0.60f);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(65.0F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
			}
			case FIRST_PERSON_LEFT_HAND -> {
				matrices.translate(-0.3F, 1.0F, 0.65F);
				matrices.scale(0.60f, 0.60f, 0.60f);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-65.0F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0F));
			}
			case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> matrices.scale(0.5f, 0.5f, 0.5f);
			case GROUND -> {
				matrices.translate(0.5, 0.4, 0.5);
				matrices.scale(0.5f, 0.5f, 0.5f);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
			default -> {
				matrices.translate(0.5, 0.5, 0.5);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
		}
	}

	private void renderFallback(ItemStack stack, double progress, ModelTransformationMode mode,
								MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemStack fallbackStack = getFallbackItemStack(stack, progress);
		if (!fallbackStack.isEmpty()) {
			BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(fallbackStack, null, null, 0);
			MinecraftClient.getInstance().getItemRenderer().renderItem(
				fallbackStack, mode, false, matrices, vertexConsumers, light, overlay, model);
		}
	}

	private int getTemperatureColor(ItemStack stack) {
		if (!TemperatureRules.canTrackTemperature(stack)) {
			return 0xFFFFFF;
		}

		return TemperatureRules.color(stack);
	}

	private boolean renderMorphed3D(ItemStack morphedStack, double progress, MatrixStack matrices,
									VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemStack startStack = MorphedItem.getStartStack(morphedStack);
		ItemStack resultStack = MorphedItem.getResultStack(morphedStack);

		if (startStack.isEmpty() || resultStack.isEmpty()) {
			return false;
		}

		double progressClamped = MathHelper.clamp(progress, 0.0, 1.0);
		double stepProgress = Math.round(progressClamped * CACHE_PRECISION) / CACHE_PRECISION;
		String cacheKey = buildCacheKey(startStack, resultStack, stepProgress);

		MorphTextureCache cached = morphTextureCache.get(cacheKey);
		if (cached != null && Math.abs(cached.lastProgress - stepProgress) < CACHE_TOLERANCE) {
			renderMorphAs3DExtrudedPixels(matrices, vertexConsumers, cached.textureId, light, overlay, cached.morphImage, morphedStack);
			return true;
		}

		try {
			BufferedImage startImage = getItemImage(startStack);
			BufferedImage resultImage = getItemImage(resultStack);
			if (startImage == null || resultImage == null) {
				return false;
			}

			BufferedImage morphedImage = MORPHER.morphStep(startImage, resultImage, stepProgress);
			if (morphedImage != null) {
				MorphTextureCache newCache = createAndRegisterTexture(cacheKey, stepProgress, morphedImage);
				if (newCache != null) {
					if (cached != null) {
						cached.cleanup();
					}
					morphTextureCache.put(cacheKey, newCache);
					renderMorphAs3DExtrudedPixels(matrices, vertexConsumers, newCache.textureId, light, overlay, morphedImage, morphedStack);
					return true;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to create morphed texture: {}", e.getMessage());
		}
		return false;
	}

	private String buildCacheKey(ItemStack startStack, ItemStack resultStack, double stepProgress) {
		return startStack.getItem() + "_"
				+ startStack.getNbt() + "_"
				+ resultStack.getItem() + "_"
				+ resultStack.getNbt() + "_"
				+ stepProgress;
	}

	private MorphTextureCache createAndRegisterTexture(String cacheKey, double stepProgress, BufferedImage morphedImage) {
		try {
			NativeImage nativeImage = bufferedImageToNativeImage(morphedImage);
			NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(nativeImage);
			Identifier textureId = new Identifier("forgero", "morphed_item_" + Math.abs(cacheKey.hashCode()));

			MinecraftClient client = MinecraftClient.getInstance();
			client.getTextureManager().registerTexture(textureId, dynamicTexture);
			dynamicTexture.upload();

			return new MorphTextureCache(dynamicTexture, textureId, morphedImage, stepProgress);
		} catch (Exception e) {
			LOGGER.error("Failed to create morphed texture: {}", e.getMessage());
			return null;
		}
	}

	private void renderMorphAs3DExtrudedPixels(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
											  Identifier textureId, int light, int overlay, BufferedImage morphImage, ItemStack stack) {
		VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(textureId));
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f posMat = entry.getPositionMatrix();
		Matrix3f normalMat = entry.getNormalMatrix();

		int texW = Math.max(1, morphImage.getWidth());
		int texH = Math.max(1, morphImage.getHeight());

		float min = -0.5f;
		float stepX = 1.0f / texW;
		float stepZ = 1.0f / texH;
		float thickness = 0.05f;
		float epsilon = 0.001f;
		float yBottom = -thickness / 2.0f + epsilon;
		float yTop = yBottom + thickness;

		int color = getTemperatureColor(stack);
		int a = 255;
		int topR = (color >> 16) & 0xFF, topG = (color >> 8) & 0xFF, topB = color & 0xFF;
		int sideR = (int)(topR * 0.85f), sideG = (int)(topG * 0.85f), sideB = (int)(topB * 0.85f);
		int botR = (int)(topR * 0.70f), botG = (int)(topG * 0.70f), botB = (int)(topB * 0.70f);

		for (int y = 0; y < texH; y++) {
			for (int x = 0; x < texW; x++) {
				int argb = morphImage.getRGB(x, y);
				int alpha = (argb >>> 24) & 0xFF;
				if (alpha == 0) continue;

				float x0 = min + x * stepX;
				float x1 = x0 + stepX;
				float z0 = min + y * stepZ;
				float z1 = z0 + stepZ;

				float u0 = x / (float) texW;
				float u1 = (x + 1) / (float) texW;
				float v0 = y / (float) texH;
				float v1 = (y + 1) / (float) texH;

				float uC = (x + 0.5f) / texW;
				float vC = (y + 0.5f) / texH;

				vc.vertex(posMat, x0, yTop, z0).color(topR, topG, topB, a).texture(u0, v0).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x1, yTop, z0).color(topR, topG, topB, a).texture(u1, v0).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x1, yTop, z1).color(topR, topG, topB, a).texture(u1, v1).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x0, yTop, z1).color(topR, topG, topB, a).texture(u0, v1).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();

				vc.vertex(posMat, x0, yBottom, z1).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();
				vc.vertex(posMat, x1, yBottom, z1).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();
				vc.vertex(posMat, x1, yBottom, z0).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();
				vc.vertex(posMat, x0, yBottom, z0).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();

				renderPixelSides(vc, x, y, texW, texH, x0, x1, z0, z1, yTop, yBottom, uC, vC, sideR, sideG, sideB, a, overlay, light, normalMat, posMat, morphImage);
			}
		}
	}

	private void renderPixelSides(VertexConsumer vc, int x, int y, int texW, int texH,
								  float x0, float x1, float z0, float z1, float yTop, float yBottom,
								  float uC, float vC, int r, int g, int b, int a, int overlay, int light,
								  Matrix3f normalMat, Matrix4f posMat, BufferedImage morphImage) {
		boolean leftTransparent = (x - 1 < 0) || ((morphImage.getRGB(x - 1, y) >>> 24) & 0xFF) == 0;
		if (leftTransparent) {
			vc.vertex(posMat, x0, yTop, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
			vc.vertex(posMat, x0, yBottom, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
			vc.vertex(posMat, x0, yBottom, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
			vc.vertex(posMat, x0, yTop, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
		}

		boolean rightTransparent = (x + 1 >= texW) || ((morphImage.getRGB(x + 1, y) >>> 24) & 0xFF) == 0;
		if (rightTransparent) {
			vc.vertex(posMat, x1, yTop, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
			vc.vertex(posMat, x1, yBottom, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
			vc.vertex(posMat, x1, yBottom, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
			vc.vertex(posMat, x1, yTop, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
		}

		boolean nearTransparent = (y - 1 < 0) || ((morphImage.getRGB(x, y - 1) >>> 24) & 0xFF) == 0;
		if (nearTransparent) {
			vc.vertex(posMat, x0, yTop, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
			vc.vertex(posMat, x0, yBottom, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
			vc.vertex(posMat, x1, yBottom, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
			vc.vertex(posMat, x1, yTop, z0).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
		}

		boolean farTransparent = (y + 1 >= texH) || ((morphImage.getRGB(x, y + 1) >>> 24) & 0xFF) == 0;
		if (farTransparent) {
			vc.vertex(posMat, x1, yTop, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
			vc.vertex(posMat, x1, yBottom, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
			vc.vertex(posMat, x0, yBottom, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
			vc.vertex(posMat, x0, yTop, z1).color(r, g, b, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
		}
	}

	private BufferedImage getItemImage(ItemStack stack) {
		try {
			return RuntimeModelUtil.getFirstQuadTextureImage(stack, MinecraftClient.getInstance());
		} catch (Exception e) {
			LOGGER.debug("Failed to get item image for {}: {}", stack, e.getMessage());
		}

		return null;
	}

	private ItemStack getFallbackItemStack(ItemStack morphedStack, double progress) {
		Identifier startId = MorphedItem.getStartItemId(morphedStack);
		Identifier resultId = MorphedItem.getResultItemId(morphedStack);
		ItemStack startStack = MorphedItem.getStartStack(morphedStack);
		ItemStack resultStack = MorphedItem.getResultStack(morphedStack);
		double progressClamped = MathHelper.clamp(progress, 0.0, 1.0);

		if (progressClamped <= MORPH_START_THRESHOLD) {
			return createFallbackStack(startStack, startId, morphedStack);
		} else if (progressClamped >= MORPH_RESULT_THRESHOLD) {
			return createFallbackStack(resultStack, resultId, morphedStack);
		}

		int step = (int) Math.floor(progressClamped * MinigameLogic.getRequiredHits(morphedStack));
		boolean showResult = (step % 2) == 1;
		ItemStack chosenStack = showResult ? resultStack : startStack;
		Identifier chosenId = showResult ? resultId : startId;

		return createFallbackStack(chosenStack, chosenId, morphedStack);
	}

	private ItemStack createFallbackStack(ItemStack storedStack, Identifier itemId, ItemStack source) {
		ItemStack stack = storedStack.copy();

		if (stack.isEmpty() && itemId != null) {
			Item item = Registries.ITEM.get(itemId);
			stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
		}

		copyTemperatureData(source, stack);
		return stack;
	}

	private void copyTemperatureData(ItemStack source, ItemStack target) {
		if (TemperatureRules.canTrackTemperature(source)) {
			TemperatureRules.copyTemperatureData(source, target);
		}
	}

	private NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int argb = bufferedImage.getRGB(x, y);
				int a = (argb >>> 24) & 0xFF;
				int r = (argb >>> 16) & 0xFF;
				int g = (argb >>> 8) & 0xFF;
				int b = argb & 0xFF;
				int abgr = (a << 24) | (b << 16) | (g << 8) | r;
				nativeImage.setColor(x, y, abgr);
			}
		}

		return nativeImage;
	}

	public static void clearCache() {
		morphTextureCache.values().forEach(MorphTextureCache::cleanup);
		morphTextureCache.clear();
	}
}
