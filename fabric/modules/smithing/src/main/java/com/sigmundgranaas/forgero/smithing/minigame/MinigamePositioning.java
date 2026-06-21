package com.sigmundgranaas.forgero.smithing.minigame;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.renderer.SmithingAnvilBlockEntityRenderer;
import com.sigmundgranaas.forgero.smithing.util.BoundingBoxUtil;
import com.sigmundgranaas.forgero.smithing.util.MorphingItemUtil;
import com.sigmundgranaas.forgero.smithing.util.PositionPreservingMorpher;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class MinigamePositioning {
	private static final Random random = new Random();
	private static final PositionPreservingMorpher MORPHER = new PositionPreservingMorpher();

	private MinigamePositioning() {
	}

	private record MorphImages(BufferedImage start, BufferedImage result) {
	}

	public static Vec2f worldHitToItemLocal(BlockHitResult hit, BlockState anvilState, Vec2f itemTextureOffset) {
		double localXBlockCenter = hit.getPos().x - hit.getBlockPos().getX() - 0.5;
		double localZBlockCenter = hit.getPos().z - hit.getBlockPos().getZ() - 0.5;

		Direction facing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		float anvilAngleDegrees = anvilAngleDegrees(facing);
		float invAnvilAngleRadians = (float) Math.toRadians(-anvilAngleDegrees);
		double cosInv = Math.cos(invAnvilAngleRadians);
		double sinInv = Math.sin(invAnvilAngleRadians);

		double xAfterAnvilRot = localXBlockCenter * cosInv - localZBlockCenter * sinInv;
		double zAfterAnvilRot = localXBlockCenter * sinInv + localZBlockCenter * cosInv;

		float xBeforeItemRot = (float) -xAfterAnvilRot;
		float zBeforeItemRot = (float) -zAfterAnvilRot;

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
		float transformedXPreScale = itemLocalPos.x + itemTextureOffset.x;
		float transformedZPreScale = itemLocalPos.y + itemTextureOffset.y;

		float transformedXScaled = transformedXPreScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
		float transformedZScaled = transformedZPreScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

		float transformedXAfterItemRot = -transformedXScaled;
		float transformedZAfterItemRot = -transformedZScaled;

		Direction facing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		float angleRadians = (float) Math.toRadians(anvilAngleDegrees(facing));
		float cos = (float) Math.cos(angleRadians);
		float sin = (float) Math.sin(angleRadians);
		double rotatedX = transformedXAfterItemRot * cos - transformedZAfterItemRot * sin;
		double rotatedZ = transformedXAfterItemRot * sin + transformedZAfterItemRot * cos;

		double worldX = anvilBlockPos.getX() + 0.5 + rotatedX;
		double worldY = anvilBlockPos.getY() + baseY;
		double worldZ = anvilBlockPos.getZ() + 0.5 + rotatedZ;

		return new Vec3d(worldX, worldY, worldZ);
	}

	private static boolean isInsideAnvilTopLayer(float itemLocalX, float itemLocalZ, BlockState anvilState, Vec2f textureOffset) {
		float transformedXPreScale = itemLocalX + textureOffset.x;
		float transformedZPreScale = itemLocalZ + textureOffset.y;

		float transformedXScaled = transformedXPreScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
		float transformedZScaled = transformedZPreScale * SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

		float transformedXAfterItemRot = -transformedXScaled;
		float transformedZAfterItemRot = -transformedZScaled;

		Direction anvilFacing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);
		float angleRadians = (float) Math.toRadians(anvilAngleDegrees(anvilFacing));
		float cos = (float) Math.cos(angleRadians);
		float sin = (float) Math.sin(angleRadians);

		float finalXBlockCenter = transformedXAfterItemRot * cos - transformedZAfterItemRot * sin;
		float finalZBlockCenter = transformedXAfterItemRot * sin + transformedZAfterItemRot * cos;

		double testX = finalXBlockCenter + 0.5;
		double testZ = finalZBlockCenter + 0.5;

		return testX >= 0.0 && testX < 1.0 && testZ >= 0.0 && testZ < 1.0;
	}

	private static float anvilAngleDegrees(Direction facing) {
		return switch (facing) {
			case EAST -> -180.0f;
			case SOUTH -> 90.0f;
			case WEST -> 0.0f;
			case NORTH -> -90.0f;
			default -> 0.0f;
		};
	}

	public static Vec2f getItemTextureOffsetVec2f(ItemStack stack) {
		if (stack.isEmpty()) return Vec2f.ZERO;
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, client);
			if (image != null) {
				return offsetFromImage(image);
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

			return randomMarkerFromImage(image, anvilState, offsetFromImage(image), false);
		} catch (Exception e) {
			return Vec2f.ZERO;
		}
	}

	public static Vec2f getMorphedTextureOffsetVec2f(SmithingAnvilBlockEntity entity) {
		try {
			ItemStack base = entity.getInventory().getStack(0);
			if (base.isEmpty()) return Vec2f.ZERO;

			MinecraftClient client = MinecraftClient.getInstance();
			MorphImages images = resolveMorphImages(entity, client);

			if (images == null) {
				return getItemTextureOffsetVec2f(base);
			}

			return offsetFromImage(morphImage(images, entity.getMorphProgress()));
		} catch (Exception e) {
			return Vec2f.ZERO;
		}
	}

	public static Vec2f getRandomMarkerPositionMorphed(SmithingAnvilBlockEntity entity) {
		try {
			ItemStack base = entity.getInventory().getStack(0);
			if (base.isEmpty()) return Vec2f.ZERO;

			MinecraftClient client = MinecraftClient.getInstance();
			MorphImages images = resolveMorphImages(entity, client);

			if (images == null) return Vec2f.ZERO;

			BufferedImage morph = morphImage(images, entity.getMorphProgress());
			Vec2f offsetVec = offsetFromImage(morph);
			BlockState anvilState = entity.getCachedState();
			Direction facing = anvilState.get(net.minecraft.block.AnvilBlock.FACING);

			return randomMarkerFromImage(morph, anvilState, offsetVec, facing == Direction.NORTH || facing == Direction.SOUTH);
		} catch (Exception e) {
			return Vec2f.ZERO;
		}
	}

	private static MorphImages resolveMorphImages(SmithingAnvilBlockEntity entity, MinecraftClient client) {
		ItemStack base = entity.getInventory().getStack(0);
		BufferedImage start = null;
		BufferedImage result = null;

		if (base.getItem() instanceof com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem) {
			start = MorphingItemUtil.getStartImage(base);
			result = MorphingItemUtil.getResultImage(base);
		}

		if ((start == null || result == null) && entity.getPlannedProductId() != null) {
			start = RuntimeModelUtil.getFirstQuadTextureImage(base, client);
			ItemStack planned = entity.createProductFromPlanned(entity.getPlannedProductId());
			if (!planned.isEmpty()) {
				result = RuntimeModelUtil.getFirstQuadTextureImage(planned, client);
			}
		}

		return start == null || result == null ? null : new MorphImages(start, result);
	}

	private static BufferedImage morphImage(MorphImages images, double progress) {
		return MORPHER.morphStep(images.start(), images.result(), progress);
	}

	private static Vec2f offsetFromImage(BufferedImage image) {
		int[] px = BoundingBoxUtil.getItemTextureOffsetFromImage(image);
		float w = Math.max(1, image.getWidth());
		float h = Math.max(1, image.getHeight());
		return new Vec2f(px[0] / w, px[1] / h);
	}

	private static Vec2f randomMarkerFromImage(BufferedImage image, BlockState anvilState, Vec2f offsetVec, boolean flipNorthSouth) {
		List<Point> validPixels = BoundingBoxUtil.collectValidPixels(image);
		if (validPixels.isEmpty()) return Vec2f.ZERO;

		int texW = Math.max(1, image.getWidth());
		int texH = Math.max(1, image.getHeight());

		for (int attempt = 0; attempt < 32; attempt++) {
			Point p = validPixels.get(random.nextInt(validPixels.size()));
			float markerXLocal = (p.x + 0.5f) / texW - 0.5f;
			float markerZLocal = (p.y + 0.5f) / texH - 0.5f;

			if (flipNorthSouth) {
				markerXLocal = -markerXLocal;
				markerZLocal = -markerZLocal;
			}

			if (isInsideAnvilTopLayer(markerXLocal, markerZLocal, anvilState, offsetVec)) {
				return new Vec2f(markerXLocal, markerZLocal);
			}
		}

		return Vec2f.ZERO;
	}
}
