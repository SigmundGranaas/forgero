package com.sigmundgranaas.forgero.smithing.util;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

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

public class MinigamePositioningUtil {
	private static final BoundingBoxUtil boundingBoxUtil = new BoundingBoxUtil();
	private static final Random random = new Random();

	public static Vec2f worldHitToItemLocal(BlockHitResult hit, BlockState anvilState, Vec2f itemTextureOffset) {
		// ...logic from Positioning.worldHitToItemLocal...
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

		float xBeforeOffset = xBeforeItemRot - itemTextureOffset.x;
		float zBeforeOffset = zBeforeItemRot - itemTextureOffset.y;

		float xLocal = xBeforeOffset / SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;
		float zLocal = zBeforeOffset / SmithingAnvilBlockEntityRenderer.RENDER_SCALE_FACTOR;

		return new Vec2f(xLocal, zLocal);
	}

	public static Vec3d itemLocalToWorld(Vec2f itemLocalPos, BlockPos anvilBlockPos, BlockState anvilState, Vec2f itemTextureOffset, float baseY) {
		// ...logic from Positioning.itemLocalToWorld...
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
		VoxelShape shape = switch (facing) {
			case NORTH -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_NORTH;
			case SOUTH -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_SOUTH;
			case EAST -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_EAST;
			case WEST -> com.sigmundgranaas.forgero.smithing.block.custom.SmithingAnvil.SHAPE_WEST;
			default -> VoxelShapes.fullCube();
		};

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

	public static Vec2f getRandomMarkerPosition(ItemStack stack, BlockState anvilState) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, client);
			if (image == null) return Vec2f.ZERO;

			List<java.awt.Point> validPixels = boundingBoxUtil.collectValidPixels(image);
			if (validPixels.isEmpty()) return Vec2f.ZERO;

			for (int attempt = 0; attempt < 32; attempt++) {
				java.awt.Point p = validPixels.get(random.nextInt(validPixels.size()));
				float markerX_local = (p.x + 0.5f) / 16.0f - 0.5f;
				float markerZ_local = (p.y + 0.5f) / 16.0f - 0.5f;

				if (isInsideAnvilTopLayer(markerX_local, markerZ_local, anvilState, stack)) {
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
}
