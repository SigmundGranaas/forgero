package com.sigmundgranaas.forgero.smithing.util;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

//TODO it seems particles spawning in north and south faces is off probably also requires 180 degrees somewhere

public class MinigamePositioningUtil {
	private static final BoundingBoxUtil boundingBoxUtil = new BoundingBoxUtil();
	private static final Random random = new Random();
	private static final PositionPreservingMorpher MORPHER = new PositionPreservingMorpher();

	public static Vec2f worldHitToItemLocal(BlockHitResult hit, BlockState anvilState, Vec2f itemTextureOffset) {
		double localX_block_center = hit.getPos().x - hit.getBlockPos().getX() - 0.5;
		double localZ_block_center = hit.getPos().z - hit.getBlockPos().getZ() - 0.5;

		Direction facing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		float anvilAngleDegrees = switch (facing) {
			case EAST -> -180.0f;
			case SOUTH -> 90.0f;
			case WEST -> 0.0f;
			case NORTH -> -90.0f;
			default -> 0.0f;
		};
		float invAnvilAngleRadians = (float) Math.toRadians(-anvilAngleDegrees);
		double cosInv = Math.cos(invAnvilAngleRadians);
		double sinInv = Math.sin(invAnvilAngleRadians);

		double xAfterAnvilRot = localX_block_center * cosInv - localZ_block_center * sinInv;
		double zAfterAnvilRot = localX_block_center * sinInv + localZ_block_center * cosInv;

		float xBeforeItemRot = (float) -xAfterAnvilRot;
		float zBeforeItemRot = (float) -zAfterAnvilRot;

		// Invert offset for NORTH/SOUTH facings to match marker and renderer logic
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			itemTextureOffset = new Vec2f(-itemTextureOffset.x, -itemTextureOffset.y);
		}

		float xBeforeOffset = xBeforeItemRot - itemTextureOffset.x;
		float zBeforeOffset = zBeforeItemRot - itemTextureOffset.y;

		float xLocal = xBeforeOffset / SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
		float zLocal = zBeforeOffset / SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

		return new Vec2f(xLocal, zLocal);
	}

	public static Vec3d itemLocalToWorld(Vec2f itemLocalPos, BlockPos anvilBlockPos, BlockState anvilState, Vec2f itemTextureOffset, float baseY) {
		float transformedX_preScale = itemLocalPos.x + itemTextureOffset.x;
		float transformedZ_preScale = itemLocalPos.y + itemTextureOffset.y;

		float transformedX_scaled = transformedX_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
		float transformedZ_scaled = transformedZ_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

		float transformedX_afterItemRot = -transformedX_scaled;
		float transformedZ_afterItemRot = -transformedZ_scaled;

		Direction facing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		float anvilAngleDegrees = switch (facing) {
			case EAST -> -180.0f;
			case SOUTH -> 90.0f;
			case WEST -> 0.0f;
			case NORTH -> -90.0f;
			default -> 0.0f;
		};
		float angleRadians = (float) Math.toRadians(anvilAngleDegrees);
		float cos = (float) Math.cos(angleRadians);
		float sin = (float) Math.sin(angleRadians);
		double rotatedX = transformedX_afterItemRot * cos - transformedZ_afterItemRot * sin;
		double rotatedZ = transformedX_afterItemRot * sin + transformedZ_afterItemRot * cos;

		double worldX = anvilBlockPos.getX() + 0.5 + rotatedX;
		double worldY = anvilBlockPos.getY() + baseY;
		double worldZ = anvilBlockPos.getZ() + 0.5 + rotatedZ;

		return new Vec3d(worldX, worldY, worldZ);
	}

	private static boolean isInsideAnvilTopLayer(float itemLocalX, float itemLocalZ, BlockState anvilState, ItemStack anvilItemStack) {
		Direction facing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		VoxelShape shape = VoxelShapes.fullCube();

		Vec2f itemTextureOffset = new Vec2f(getItemTextureOffset(anvilItemStack)[0] / 16.0f, getItemTextureOffset(anvilItemStack)[1] / 16.0f);

		float transformedX_preScale = itemLocalX + itemTextureOffset.x;
		float transformedZ_preScale = itemLocalZ + itemTextureOffset.y;

		float transformedX_scaled = transformedX_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
		float transformedZ_scaled = transformedZ_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

		float transformedX_afterItemRot = -transformedX_scaled;
		float transformedZ_afterItemRot = -transformedZ_scaled;

		Direction anvilFacing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		float anvilAngleDegrees = switch (anvilFacing) {
			case EAST -> -180.0f;
			case SOUTH -> 90.0f;
			case WEST -> 0.0f;
			case NORTH -> -90.0f;
			default -> 0.0f;
		};
		float angleRadians = (float) Math.toRadians(anvilAngleDegrees);
		float cos = (float) Math.cos(angleRadians);
		float sin = (float) Math.sin(angleRadians);

		float finalX_block_center = transformedX_afterItemRot * cos - transformedZ_afterItemRot * sin;
		float finalZ_block_center = transformedX_afterItemRot * sin + transformedZ_afterItemRot * cos;

		double testX = finalX_block_center + 0.5;
		double testZ = finalZ_block_center + 0.5;
		double testY = 1.0 - 1e-6;

		for (net.minecraft.util.math.Box box : shape.getBoundingBoxes()) {
			if (box.contains(testX, testY, testZ)) {
				return true;
			}
		}
		return false;
	}

	// NEW: Overload using an explicit texture offset (used for morphed offset)
	private static boolean isInsideAnvilTopLayer(float itemLocalX, float itemLocalZ, BlockState anvilState, Vec2f textureOffset) {
		Direction anvilFacing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		VoxelShape shape = VoxelShapes.fullCube();

		// Apply offset and scale identical to renderer pipeline
		float transformedX_preScale = itemLocalX + textureOffset.x;
		float transformedZ_preScale = itemLocalZ + textureOffset.y;

		float transformedX_scaled = transformedX_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
		float transformedZ_scaled = transformedZ_preScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

		float transformedX_afterItemRot = -transformedX_scaled;
		float transformedZ_afterItemRot = -transformedZ_scaled;

		float anvilAngleDegrees = switch (anvilFacing) {
			case EAST -> -180.0f;
			case SOUTH -> 90.0f;
			case WEST -> 0.0f;
			case NORTH -> -90.0f;
			default -> 0.0f;
		};
		float angleRadians = (float) Math.toRadians(anvilAngleDegrees);
		float cos = (float) Math.cos(angleRadians);
		float sin = (float) Math.sin(angleRadians);

		float finalX_block_center = transformedX_afterItemRot * cos - transformedZ_afterItemRot * sin;
		float finalZ_block_center = transformedX_afterItemRot * sin + transformedZ_afterItemRot * cos;

		double testX = finalX_block_center + 0.5;
		double testZ = finalZ_block_center + 0.5;
		double testY = 1.0 - 1e-6;

		for (net.minecraft.util.math.Box box : shape.getBoundingBoxes()) {
			if (box.contains(testX, testY, testZ)) {
				return true;
			}
		}
		return false;
	}

	// NEW: normalized offset (item-local units) for an ItemStack using its texture size
	public static Vec2f getItemTextureOffsetVec2f(ItemStack stack) {
		if (stack.isEmpty()) return Vec2f.ZERO;
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, client);
			if (image != null) {
				int[] px = BoundingBoxUtil.getItemTextureOffsetFromImage(image);
				float w = Math.max(1, image.getWidth());
				float h = Math.max(1, image.getHeight());
				return new Vec2f(px[0] / w, px[1] / h);
			}
		} catch (Exception ignored) {
		}
		return Vec2f.ZERO;
	}

	public static Vec2f getRandomMarkerPosition(ItemStack stack, BlockState anvilState) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, client);
			if (image == null) return Vec2f.ZERO;

			List<java.awt.Point> validPixels = boundingBoxUtil.collectValidPixels(image);
			if (validPixels.isEmpty()) return Vec2f.ZERO;

			int texW = Math.max(1, image.getWidth());
			int texH = Math.max(1, image.getHeight());
			Vec2f offsetVec = getItemTextureOffsetVec2f(stack);

			for (int attempt = 0; attempt < 32; attempt++) {
				java.awt.Point p = validPixels.get(random.nextInt(validPixels.size()));
				float markerX_local = (p.x + 0.5f) / texW - 0.5f;
				float markerZ_local = (p.y + 0.5f) / texH - 0.5f;

				if (isInsideAnvilTopLayer(markerX_local, markerZ_local, anvilState, offsetVec)) {
					return new Vec2f(markerX_local, markerZ_local);
				}
			}
			return Vec2f.ZERO;
		} catch (Exception e) {
			return Vec2f.ZERO;
		}
	}

	public static int[] getItemTextureOffset(ItemStack stack) {
		if (stack.isEmpty()) {
			return new int[]{0, 0};
		}
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, client);
			if (image != null) {
				return BoundingBoxUtil.getItemTextureOffsetFromImage(image);
			}
		} catch (Exception e) {
		}
		return new int[]{0, 0};
	}

	// NEW: center-pad two images to identical canvas
	private static BufferedImage[] centerPadToSameSize(BufferedImage a, BufferedImage b) {
		int w = Math.max(a.getWidth(), b.getWidth());
		int h = Math.max(a.getHeight(), b.getHeight());
		if (a.getWidth() == w && a.getHeight() == h && b.getWidth() == w && b.getHeight() == h) {
			return new BufferedImage[]{a, b};
		}
		BufferedImage aa = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bb = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var ga = aa.createGraphics();
		var gb = bb.createGraphics();
		ga.drawImage(a, (w - a.getWidth()) / 2, (h - a.getHeight()) / 2, null);
		gb.drawImage(b, (w - b.getWidth()) / 2, (h - b.getHeight()) / 2, null);
		ga.dispose();
		gb.dispose();
		return new BufferedImage[]{aa, bb};
	}

	// NEW: normalized offset (item-local units) based on the current morphed image size
	public static Vec2f getMorphedTextureOffsetVec2f(SmithingAnvilBlockEntity entity) {
		try {
			ItemStack base = entity.getInventory().getStack(0);
			if (base.isEmpty()) return Vec2f.ZERO;

			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage start = RuntimeModelUtil.getFirstQuadTextureImage(base, client);
			if (start == null || entity.getPlannedProductId() == null) {
				return getItemTextureOffsetVec2f(base);
			}
			ItemStack planned = entity.createProductFromPlanned(entity.getPlannedProductId());
			if (planned.isEmpty()) return getItemTextureOffsetVec2f(base);
			BufferedImage result = RuntimeModelUtil.getFirstQuadTextureImage(planned, client);
			if (result == null) return getItemTextureOffsetVec2f(base);

			double weight = entity.getMorphProgress();
			BufferedImage[] padded = centerPadToSameSize(start, result);
			BufferedImage morph = MORPHER.morphStep(padded[0], padded[1], weight);
			int[] px = BoundingBoxUtil.getItemTextureOffsetFromImage(morph);
			float w = Math.max(1, morph.getWidth());
			float h = Math.max(1, morph.getHeight());
			return new Vec2f(px[0] / w, px[1] / h);
		} catch (Throwable t) {
			return Vec2f.ZERO;
		}
	}

	// Existing int[] version retained for compatibility (used elsewhere)
	public static int[] getMorphedTextureOffset(SmithingAnvilBlockEntity entity) {
		try {
			ItemStack base = entity.getInventory().getStack(0);
			if (base.isEmpty()) return new int[]{0, 0};

			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage start = RuntimeModelUtil.getFirstQuadTextureImage(base, client);
			if (start == null || entity.getPlannedProductId() == null) {
				return getItemTextureOffset(base);
			}
			ItemStack planned = entity.createProductFromPlanned(entity.getPlannedProductId());
			if (planned.isEmpty()) return getItemTextureOffset(base);
			BufferedImage result = RuntimeModelUtil.getFirstQuadTextureImage(planned, client);
			if (result == null) return getItemTextureOffset(base);

			double weight = entity.getMorphProgress();
			BufferedImage[] padded = centerPadToSameSize(start, result);
			BufferedImage morph = MORPHER.morphStep(padded[0], padded[1], weight);
			return BoundingBoxUtil.getItemTextureOffsetFromImage(morph);
		} catch (Throwable t) {
			return new int[]{0, 0};
		}
	}

	public static Vec2f getRandomMarkerPositionMorphed(SmithingAnvilBlockEntity entity) {
		try {
			ItemStack base = entity.getInventory().getStack(0);
			if (base.isEmpty()) return Vec2f.ZERO;

			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage start = RuntimeModelUtil.getFirstQuadTextureImage(base, client);
			if (start == null || entity.getPlannedProductId() == null) return Vec2f.ZERO;

			ItemStack planned = entity.createProductFromPlanned(entity.getPlannedProductId());
			if (planned.isEmpty()) return Vec2f.ZERO;

			BufferedImage result = RuntimeModelUtil.getFirstQuadTextureImage(planned, client);
			if (result == null) return Vec2f.ZERO;

			double weight = entity.getMorphProgress();
			BufferedImage[] padded = centerPadToSameSize(start, result);
			BufferedImage morph = MORPHER.morphStep(padded[0], padded[1], weight);

			List<java.awt.Point> validPixels = boundingBoxUtil.collectValidPixels(morph);
			if (validPixels.isEmpty()) return Vec2f.ZERO;

			int texW = Math.max(1, morph.getWidth());
			int texH = Math.max(1, morph.getHeight());

			Vec2f offsetVec = getMorphedTextureOffsetVec2f(entity);
			BlockState anvilState = entity.getCachedState();
			Direction facing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);

			for (int attempt = 0; attempt < 32; attempt++) {
				java.awt.Point p = validPixels.get(random.nextInt(validPixels.size()));
				float markerX_local = (p.x + 0.5f) / texW - 0.5f;
				float markerZ_local = (p.y + 0.5f) / texH - 0.5f;

				// Apply 180° rotation for NORTH and SOUTH facings to match renderer
				if (facing == Direction.NORTH || facing == Direction.SOUTH) {
					markerX_local = -markerX_local;
					markerZ_local = -markerZ_local;
				}

				if (isInsideAnvilTopLayer(markerX_local, markerZ_local, anvilState, offsetVec)) {
					return new Vec2f(markerX_local, markerZ_local);
				}
			}
			return Vec2f.ZERO;
		} catch (Throwable t) {
			return Vec2f.ZERO;
		}
	}
}
