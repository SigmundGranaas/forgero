package com.sigmundgranaas.forgero.smithing.item.renderer;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;

import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
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

	// Cache for morphed textures - key is cache key (start+result+progress), value is dynamic texture info
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

		// Apply transformations based on render mode
		switch (mode) {
			case GUI -> {
				matrices.translate(0.5, 0.5, 0);
				matrices.scale(1f, 1f, 1f);
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
			case FIXED -> {
				matrices.translate(0.5, 0.5, 0.5);
				matrices.scale(1f, 1f, 1f);
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
			case FIRST_PERSON_RIGHT_HAND -> {
				matrices.translate(0.5F, 0.75F, 0.65F);
				matrices.scale(0.60f, 0.60f, 0.60f);
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(65.0F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
			}
			case  FIRST_PERSON_LEFT_HAND -> {
				matrices.translate(-0.3F, 1.0F, 0.65F);
				matrices.scale(0.60f, 0.60f, 0.60f);
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(-65.0F));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0F));
			}
			case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {

				matrices.scale(0.5f, 0.5f, 0.5f);
				// No rotation, keep upright
			}
			case GROUND -> {
				matrices.translate(0.5, 0.4, 0.5);
				matrices.scale(0.5f, 0.5f, 0.5f);
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
			case NONE -> {
				matrices.translate(0.5, 0.5, 0.5);
				matrices.scale(1f, 1f, 1f);
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
			}
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		double progress = nbt.contains(MorphedItem.PROGRESS_KEY) ? nbt.getDouble(MorphedItem.PROGRESS_KEY) : 0.0;

		// Try to render the morphed texture using 3D extruded pixels
		if (!renderMorphed3D(stack, progress, matrices, vertexConsumers, light, overlay)) {
			// Fallback to discrete item switching if morphed rendering fails
			ItemStack fallbackStack = getFallbackItemStack(stack, progress);
			if (!fallbackStack.isEmpty()) {
				BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(fallbackStack, null, null, 0);
				MinecraftClient.getInstance().getItemRenderer().renderItem(
					fallbackStack, mode, false, matrices, vertexConsumers, light, overlay, model);
			}
		}

		matrices.pop();
	}

	private int getTemperatureColor(ItemStack stack) {
		if (!com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.hasMaxTemperature(stack)) {
			return 0xFFFFFF;
		}
		int temp = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getTemperature(stack);
		int max = com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.getMaxTemp(stack);
		return com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.getHeatColor(temp, max);
	}

	private boolean renderMorphed3D(ItemStack morphedStack, double progress, MatrixStack matrices,
									VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Identifier startId = MorphedItem.getStartItemId(morphedStack);
		Identifier resultId = MorphedItem.getResultItemId(morphedStack);
		if (startId == null && resultId == null) {
			return false;
		}
		double progressClamped = MathHelper.clamp(progress, 0.0, 1.0);
		double stepProgress = Math.round(progressClamped * 10.0) / 10.0;
		String cacheKey = String.format("%s_%s_%.1f",
			startId != null ? startId.toString() : "null",
			resultId != null ? resultId.toString() : "null",
			stepProgress);
		MorphTextureCache cached = morphTextureCache.get(cacheKey);
		if (cached != null && Math.abs(cached.lastProgress - stepProgress) < 0.001) {
			renderMorphAs3DExtrudedPixels(matrices, vertexConsumers, cached.textureId, light, overlay, cached.morphImage, morphedStack);
			return true;
		}
		try {
			BufferedImage startImage = getItemImage(startId);
			BufferedImage resultImage = getItemImage(resultId);
			if (startImage == null || resultImage == null) {
				return false;
			}
			BufferedImage morphedImage = MORPHER.morphStep(startImage, resultImage, stepProgress);
			if (morphedImage != null) {
				NativeImage nativeImage = bufferedImageToNativeImage(morphedImage);
				NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(nativeImage);
				Identifier textureId = new Identifier("forgero", "morphed_item_" + Math.abs(cacheKey.hashCode()));
				MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, dynamicTexture);
				if (cached != null) {
					cached.cleanup();
				}
				morphTextureCache.put(cacheKey, new MorphTextureCache(dynamicTexture, textureId, morphedImage, stepProgress));
				renderMorphAs3DExtrudedPixels(matrices, vertexConsumers, textureId, light, overlay, morphedImage, morphedStack);
				return true;
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to create morphed texture: {}", e.getMessage());
		}
		return false;
	}

	// Render only opaque pixels of the morph texture as a thin extruded 3D mesh (top quads + edge sides)
	private void renderMorphAs3DExtrudedPixels(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, int light, int overlay, BufferedImage morphImage, ItemStack stack) {
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

		// Iterate image and extrude only opaque pixels
		for (int y = 0; y < texH; y++) {
			for (int x = 0; x < texW; x++) {
				int argb = morphImage.getRGB(x, y);
				int alpha = (argb >>> 24) & 0xFF;
				if (alpha == 0) continue; // skip transparent pixels

				// Pixel quad bounds in local coords - mapping texture to horizontal plane
				float x0 = min + x * stepX;
				float x1 = x0 + stepX;
				float z0 = min + y * stepZ;
				float z1 = z0 + stepZ;

				// UVs for the pixel (full coverage of that texel)
				float u0 = x / (float) texW;
				float u1 = (x + 1) / (float) texW;
				float v0 = y / (float) texH;
				float v1 = (y + 1) / (float) texH;

				// Center UV for sides to inherit the top color (prevents see-through/stretch)
				float uC = (x + 0.5f) / texW;
				float vC = (y + 0.5f) / texH;

				// Top face (quad facing +Y) - this is the visible surface of the flat item
				vc.vertex(posMat, x0, yTop, z0).color(topR, topG, topB, a).texture(u0, v0).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x1, yTop, z0).color(topR, topG, topB, a).texture(u1, v0).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x1, yTop, z1).color(topR, topG, topB, a).texture(u1, v1).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x0, yTop, z1).color(topR, topG, topB, a).texture(u0, v1).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();

				// Bottom cap (quad facing -Y) - underside of the flat item
				vc.vertex(posMat, x0, yBottom, z1).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();
				vc.vertex(posMat, x1, yBottom, z1).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();
				vc.vertex(posMat, x1, yBottom, z0).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();
				vc.vertex(posMat, x0, yBottom, z0).color(botR, botG, botB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, -1, 0).next();

				// For sides, only draw where the neighboring pixel is transparent or out of bounds
				boolean leftTransparent = (x - 1 < 0) || ((morphImage.getRGB(x - 1, y) >>> 24) & 0xFF) == 0;
				if (leftTransparent) {
					vc.vertex(posMat, x0, yTop, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
					vc.vertex(posMat, x0, yBottom, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
					vc.vertex(posMat, x0, yBottom, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
					vc.vertex(posMat, x0, yTop, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, -1, 0, 0).next();
				}

				boolean rightTransparent = (x + 1 >= texW) || ((morphImage.getRGB(x + 1, y) >>> 24) & 0xFF) == 0;
				if (rightTransparent) {
					vc.vertex(posMat, x1, yTop, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
					vc.vertex(posMat, x1, yBottom, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
					vc.vertex(posMat, x1, yBottom, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
					vc.vertex(posMat, x1, yTop, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 1, 0, 0).next();
				}

				boolean nearTransparent = (y - 1 < 0) || ((morphImage.getRGB(x, y - 1) >>> 24) & 0xFF) == 0;
				if (nearTransparent) {
					vc.vertex(posMat, x0, yTop, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
					vc.vertex(posMat, x0, yBottom, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
					vc.vertex(posMat, x1, yBottom, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
					vc.vertex(posMat, x1, yTop, z0).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, -1).next();
				}

				boolean farTransparent = (y + 1 >= texH) || ((morphImage.getRGB(x, y + 1) >>> 24) & 0xFF) == 0;
				if (farTransparent) {
					vc.vertex(posMat, x1, yTop, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
					vc.vertex(posMat, x1, yBottom, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
					vc.vertex(posMat, x0, yBottom, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
					vc.vertex(posMat, x0, yTop, z1).color(sideR, sideG, sideB, a).texture(uC, vC).overlay(overlay).light(light).normal(normalMat, 0, 0, 1).next();
				}
			}
		}
	}

	private BufferedImage getItemImage(Identifier itemId) {
		if (itemId == null) return null;

		try {
			// Try to get the item and extract its texture
			Item item = Registries.ITEM.get(itemId);
			if (item != null) {
				ItemStack tempStack = new ItemStack(item);
				return RuntimeModelUtil.getFirstQuadTextureImage(tempStack, MinecraftClient.getInstance());
			}
		} catch (Exception e) {
			LOGGER.debug("Failed to get item image for {}: {}", itemId, e.getMessage());
		}

		return null;
	}

	private ItemStack getFallbackItemStack(ItemStack morphedStack, double progress) {
		Identifier startId = MorphedItem.getStartItemId(morphedStack);
		Identifier resultId = MorphedItem.getResultItemId(morphedStack);

		// Clamp progress between 0.0 and 1.0
		double progressClamped = MathHelper.clamp(progress, 0.0, 1.0);

		// Progressive transition: 0-30% start item, 30-70% transition, 70-100% result item
		if (progressClamped <= 0.3) {
			// 0-30%: Show start item
			if (startId != null) {
				Item startItem = Registries.ITEM.get(startId);
				return startItem != null ? new ItemStack(startItem) : ItemStack.EMPTY;
			}
		} else if (progressClamped >= 0.7) {
			// 70-100%: Show result item
			if (resultId != null) {
				Item resultItem = Registries.ITEM.get(resultId);
				return resultItem != null ? new ItemStack(resultItem) : ItemStack.EMPTY;
			}
		} else {
			// 30-70%: Alternate based on discrete progress steps to show morphing
			int step = (int) Math.floor(progressClamped * 10); // 0-9 steps
			boolean showResult = (step % 2) == 1;

			Identifier chosenId = showResult && resultId != null ? resultId : startId;
			if (chosenId != null) {
				Item item = Registries.ITEM.get(chosenId);
				return item != null ? new ItemStack(item) : ItemStack.EMPTY;
			}
		}

		// Final fallback to start item
		if (startId != null) {
			Item startItem = Registries.ITEM.get(startId);
			return startItem != null ? new ItemStack(startItem) : ItemStack.EMPTY;
		}

		return ItemStack.EMPTY;
	}

	private NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int argb = bufferedImage.getRGB(x, y);
				// Convert ARGB to ABGR for NativeImage
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

	// Cleanup method to release cached textures (call this from mod cleanup if needed)
	public static void clearCache() {
		morphTextureCache.values().forEach(MorphTextureCache::cleanup);
		morphTextureCache.clear();
	}
}
