package com.sigmundgranaas.forgero.smithing.block.renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.util.BoundingBoxUtil;
import com.sigmundgranaas.forgero.smithing.util.MinigamePositioningUtil;
import com.sigmundgranaas.forgero.smithing.util.PositionPreservingMorpher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.block.AnvilBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

//TODO for some reason the side pixels are inverted, not biggest problem but I hate it.
//TODO dont think I can complain but its happening again.

@Environment(EnvType.CLIENT)
public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<SmithingAnvilBlockEntity> {
	private static final Logger LOGGER = LogManager.getLogger("ForgeroSmithingAnvilRenderer");
	private final BoundingBoxUtil boundingBoxUtil = new BoundingBoxUtil();
	// Use WeakHashMap for caches to prevent memory leaks if itemstacks are frequently recreated/discarded
	private final Map<ItemStack, int[]> itemTextureOffsetCache = new WeakHashMap<>();
	private final Map<ItemStack, List<java.awt.Point>> validPixelsCache = new WeakHashMap<>();

	// Global scale factor for the item and all associated overlays
	public static final float RENDER_SCALE_FACTOR = 0.5f;

	// Anvil specific constants for positioning
	private static final float ANVIL_TOP_Y = 1;
	private static final float Y_FIGHTING_OFFSET = 0.001f; // Small offset to prevent z-fighting
	private static final float MARKER_RENDER_OFFSET_Y = 0.04f; // Offset for marker/debug visuals above item surface


	// Cache for morph textures: key is block entity, value is a small cache of morph progress to BufferedImage
	private final Map<SmithingAnvilBlockEntity, MorphCache> morphTextureCache = new WeakHashMap<>();
	private final PositionPreservingMorpher morpher = new PositionPreservingMorpher();

	private static class MorphCache {
		double lastWeight = -1.0;
		BufferedImage lastImage = null;
		BufferedImage lastStart = null;
		BufferedImage lastResult = null;
		NativeImageBackedTexture lastDynamicTexture = null;
		Identifier lastTextureId = null;
	}

	public SmithingAnvilBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(SmithingAnvilBlockEntity entity, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemStack itemStack = entity.getInventory().getStack(0);

		if (itemStack.isEmpty()) {
			return;
		}

		matrices.push();

		// All transformations are applied in reverse order to how they are called.
		// The common transformations (translate, anvil rotation, item 180 rot, texture offset, overall scale)
		// establish a coordinate system where (0,0,0) is the visual center of the *scaled* item on the anvil,
		// and the XZ plane is horizontal (flat on the anvil surface).
		// The +Y axis at this point points "upwards" from the anvil surface.

		float itemRenderY = ANVIL_TOP_Y + Y_FIGHTING_OFFSET + 0.01f;

		matrices.translate(0.5f, itemRenderY, 0.5f);

		// Step 2: Rotate the entire visual setup (item + overlays) by the anvil's facing direction.
		Direction facing = entity.getCachedState().get(AnvilBlock.FACING);
		float anvilAngleDegrees = 0.0f;
		switch (facing) {
			case EAST -> anvilAngleDegrees = -180.0f;
			case SOUTH -> anvilAngleDegrees = 90.0f;
			case WEST -> anvilAngleDegrees = 0.0f;
			case NORTH -> anvilAngleDegrees = -90.0f;
		}
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(anvilAngleDegrees));

		// Step 3: Rotate the item 180 degrees around Y to make it face the player consistently.
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

		// Step 4: Apply centering offset from item's texture (dx, dz).
		// Use normalized morphed offset (item-local units) to match hit/particle mapping.
		Vec2f normOffset = MinigamePositioningUtil.getMorphedTextureOffsetVec2f(entity);
		matrices.translate(normOffset.x, 0, normOffset.y);

		// Step 5: Apply the uniform scaling factor. This affects everything after this point.
		matrices.scale(RENDER_SCALE_FACTOR, RENDER_SCALE_FACTOR, RENDER_SCALE_FACTOR);

		renderMarker(matrices, vertexConsumers, entity);
		if (MinecraftClient.getInstance().options.debugEnabled) {

		}

		// Step 6: Rotate the item to lay flat on the anvil.
		// This rotation makes the item's internal "up" axis (-Y in its model space) align with the anvil's Y.
		// If ModelTransformationMode.NONE is used, the item model is typically rendered standing upright.
		// A -90 degree rotation around the X-axis will lay it flat on the XZ plane.
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

		int lightLevel = getLightLevel(entity.getWorld(), entity.getPos());

		// Determine minigame active state: when ingot-crafting is ongoing and a planned product image exists
		boolean minigameActive = entity.isIngotCrafting() && entity.getPlannedProductImage() != null;

		// If the minigame is NOT active, render the default item as usual
		if (!minigameActive) {
			ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
			itemRenderer.renderItem(itemStack, ModelTransformationMode.NONE, lightLevel, overlay,
					matrices, vertexConsumers, entity.getWorld(), (int) entity.getPos().asLong());
		}

		// --- MORPHED TEXTURE RENDERING ---
		// Render morph if the minigame is active, or if a one-shot final overlay is requested
		boolean forceFinal = entity.isShowFinalMorphOnce();
		if (minigameActive || forceFinal) {
			BufferedImage startImage = entity.getStartingItemImage();
			BufferedImage resultImage = entity.getPlannedProductImage();

			double weight = forceFinal ? 1.0 : entity.getMorphProgress();

			NativeImageBackedTexture morphDynamicTexture = null;
			Identifier morphTextureId = null;

			MorphCache cache = morphTextureCache.computeIfAbsent(entity, k -> new MorphCache());
			boolean canRecompute = (startImage != null && resultImage != null);

			// Convert PNG -> NativeImage -> NativeImageBackedTexture (dynamic) and cache
			if (canRecompute && (cache.lastImage == null || cache.lastWeight != weight
					|| cache.lastStart != startImage || cache.lastResult != resultImage)) {
				BufferedImage[] padded = centerPadToSameSize(startImage, resultImage);
				cache.lastImage = morpher.morphStep(padded[0], padded[1], weight);
				cache.lastWeight = weight;
				cache.lastStart = startImage;
				cache.lastResult = resultImage;

				if (cache.lastDynamicTexture != null) {
					cache.lastDynamicTexture.close();
					cache.lastDynamicTexture = null;
					cache.lastTextureId = null;
				}
				NativeImage nativeImage = bufferedImageToNativeImage(cache.lastImage);
				cache.lastDynamicTexture = new NativeImageBackedTexture(nativeImage);
				cache.lastTextureId = MinecraftClient.getInstance().getTextureManager()
						.registerDynamicTexture("forgero_morph_" + entity.getPos().asLong(), cache.lastDynamicTexture);
			}

			morphDynamicTexture = cache.lastDynamicTexture;
			morphTextureId = cache.lastTextureId;

			// Render the morphed texture as a 3D extruded pixel mesh (only where colored)
			if (morphTextureId != null && morphDynamicTexture != null && cache.lastImage != null) {
				matrices.push();
				// Requested rotation: rotate morph 90 degrees in positive X
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
				renderMorphAs3DExtrudedPixels(matrices, vertexConsumers, morphTextureId, lightLevel, overlay, cache.lastImage);
				matrices.pop();
			} else if (minigameActive) {
				// Fallback to default item if morph texture isn't ready
				ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
				itemRenderer.renderItem(itemStack, ModelTransformationMode.NONE, lightLevel, overlay,
						matrices, vertexConsumers, entity.getWorld(), (int) entity.getPos().asLong());
			}

			// Clear the one-shot flag after rendering once
			if (forceFinal) {
				entity.clearFinalMorphOnce();
			}
		} else {
			// Minigame inactive: release cached texture to prevent leaks
			releaseMorphCache(entity);
		}

		matrices.pop();
	}

	// Helper: Convert BufferedImage (ARGB) to NativeImage (ABGR)
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

	// Render only opaque pixels of the morph texture as a thin extruded 3D mesh (top quads + edge sides)
	private void renderMorphAs3DExtrudedPixels(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, int light, int overlay, BufferedImage morphImage) {
		VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(textureId));
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f posMat = entry.getPositionMatrix();
		Matrix3f normalMat = entry.getNormalMatrix();

		int texW = Math.max(1, morphImage.getWidth());
		int texH = Math.max(1, morphImage.getHeight());

		// Coordinate space: X/Z in [-0.5, 0.5], Y up
		float min = -0.5f;
		float stepX = 1.0f / texW;
		float stepZ = 1.0f / texH;

		// Thickness and vertical placement: exactly 1 texel tall, centered around 0 with a tiny epsilon to prevent z-fighting
		float thickness = Math.min(stepX, stepZ); // 1 pixel height
		float epsilon = 0.001f;
		float yBottom = -thickness / 2.0f + epsilon;
		float yTop = yBottom + thickness;

		// Simple shading multipliers: top bright, sides slightly darker, bottom darkest
		int a = 255;
		int topR = 255, topG = 255, topB = 255;
		int sideR = (int)(255 * 0.85f), sideG = (int)(255 * 0.85f), sideB = (int)(255 * 0.85f);
		int botR = (int)(255 * 0.70f), botG = (int)(255 * 0.70f), botB = (int)(255 * 0.70f);

		// Iterate image and extrude only opaque pixels
		for (int y = 0; y < texH; y++) {
			for (int x = 0; x < texW; x++) {
				int argb = morphImage.getRGB(x, y);
				int alpha = (argb >>> 24) & 0xFF;
				if (alpha == 0) continue; // skip transparent pixels

				// Pixel quad bounds in local coords
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

				// Top face (quad facing +Y)
				vc.vertex(posMat, x0, yTop, z0).color(topR, topG, topB, a).texture(u0, v0).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x1, yTop, z0).color(topR, topG, topB, a).texture(u1, v0).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x1, yTop, z1).color(topR, topG, topB, a).texture(u1, v1).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();
				vc.vertex(posMat, x0, yTop, z1).color(topR, topG, topB, a).texture(u0, v1).overlay(overlay).light(light).normal(normalMat, 0, 1, 0).next();

				// Optional bottom cap (quad facing -Y) for completeness
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

	// Ensure both images share the same canvas size by center-padding to max dims
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

	private void renderMarker(MatrixStack matrices, VertexConsumerProvider vertexConsumers, SmithingAnvilBlockEntity entity) {
		if (!entity.getMarkerPositions().isEmpty()) {
			matrices.push();
			// The current matrix stack is set up such that XZ is the horizontal plane, and Y points up.
			// Markers are defined in item-local space (-0.5 to 0.5), matching this setup.


			// Apply extra 180° rotation for North and South facings
			Direction facing = entity.getCachedState().get(AnvilBlock.FACING);
			if (facing == Direction.NORTH || facing == Direction.SOUTH) {
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
			}

			Vec2f markerPos = entity.getMarkerPositions().get(0);

			// Translate to the marker's position within the item's local space
			matrices.translate(markerPos.x, MARKER_RENDER_OFFSET_Y, markerPos.y);

			// Draw the marker
			boolean isFast = entity.getFastMarkerIndices().contains(entity.getMarkerAttempts());
			float r = isFast ? 1.0f : 1.0f;
			float g = isFast ? 0.2f : 1.0f;
			float b = isFast ? 0.2f : 0.0f;
			float size = 0.0350f; // This is half the side length of the marker box (0.075 block total size) - 25% smaller

			VertexConsumer lineConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
			// Draw a wireframe box at the marker position
			WorldRenderer.drawBox(matrices, lineConsumer, -size, 0, -size, size, 0, size, r, g, b, 1.0f);

			matrices.pop();
		}
	}

	private int getLightLevel(World world, BlockPos pos) {
		if (world == null) {
			return 15728880;
		}

		int blockLight = world.getLightLevel(LightType.BLOCK, pos.up());
		int skyLight = world.getLightLevel(LightType.SKY, pos.up());
		return LightmapTextureManager.pack(blockLight, skyLight);
	}

	// NEW: release cached dynamic texture when not needed
	private void releaseMorphCache(SmithingAnvilBlockEntity entity) {
		MorphCache cache = morphTextureCache.get(entity);
		if (cache != null && cache.lastDynamicTexture != null) {
			cache.lastDynamicTexture.close();
			cache.lastDynamicTexture = null;
			cache.lastTextureId = null;
			cache.lastImage = null;
			cache.lastStart = null;
			cache.lastResult = null;
			cache.lastWeight = -1.0;
		}
	}
}
