package com.sigmundgranaas.forgero.smithing.minigame;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class MinigameTransforms {
	public static final float ITEM_RENDER_SCALE = 0.5f;
	private static final double BLOCK_CENTER_OFFSET = 0.5D;

	private MinigameTransforms() {
	}

	public static Vec2f worldHitToItemLocal(
			BlockHitResult hit,
			BlockState anvilState,
			Vec2f itemTextureOffset
	) {
		Vec2f blockCenteredHit = new Vec2f(
				(float) (hit.getPos().x - hit.getBlockPos().getX() - BLOCK_CENTER_OFFSET),
				(float) (hit.getPos().z - hit.getBlockPos().getZ() - BLOCK_CENTER_OFFSET)
		);

		Direction facing = anvilFacing(anvilState);
		Vec2f afterAnvilRotation = rotate(blockCenteredHit, -anvilAngleDegrees(facing));
		Vec2f beforeItemRotation = new Vec2f(-afterAnvilRotation.x, -afterAnvilRotation.y);
		Vec2f adjustedTextureOffset = hitTextureOffset(facing, itemTextureOffset);

		return new Vec2f(
				(beforeItemRotation.x - adjustedTextureOffset.x) / ITEM_RENDER_SCALE,
				(beforeItemRotation.y - adjustedTextureOffset.y) / ITEM_RENDER_SCALE
		);
	}

	public static Vec3d itemLocalToWorld(
			Vec2f itemLocalPos,
			BlockPos anvilBlockPos,
			BlockState anvilState,
			Vec2f itemTextureOffset,
			float baseY
	) {
		Vec2f blockCentered = itemLocalToBlockCentered(itemLocalPos, anvilState, itemTextureOffset);

		return new Vec3d(
				anvilBlockPos.getX() + BLOCK_CENTER_OFFSET + blockCentered.x,
				anvilBlockPos.getY() + baseY,
				anvilBlockPos.getZ() + BLOCK_CENTER_OFFSET + blockCentered.y
		);
	}

	public static boolean isInsideAnvilTopLayer(
			float itemLocalX,
			float itemLocalZ,
			BlockState anvilState,
			Vec2f textureOffset
	) {
		Vec2f blockCentered = itemLocalToBlockCentered(
				new Vec2f(itemLocalX, itemLocalZ),
				anvilState,
				textureOffset
		);

		double blockX = blockCentered.x + BLOCK_CENTER_OFFSET;
		double blockZ = blockCentered.y + BLOCK_CENTER_OFFSET;

		return blockX >= 0.0D && blockX < 1.0D && blockZ >= 0.0D && blockZ < 1.0D;
	}

	public static float anvilAngleDegrees(Direction facing) {
		return switch (facing) {
			case EAST -> -180.0f;
			case SOUTH -> 90.0f;
			case WEST -> 0.0f;
			case NORTH -> -90.0f;
			default -> 0.0f;
		};
	}

	private static Vec2f itemLocalToBlockCentered(
			Vec2f itemLocalPos,
			BlockState anvilState,
			Vec2f itemTextureOffset
	) {
		float xScaled = (itemLocalPos.x + itemTextureOffset.x) * ITEM_RENDER_SCALE;
		float zScaled = (itemLocalPos.y + itemTextureOffset.y) * ITEM_RENDER_SCALE;
		Vec2f afterItemRotation = new Vec2f(-xScaled, -zScaled);

		return rotate(afterItemRotation, anvilAngleDegrees(anvilFacing(anvilState)));
	}

	private static Vec2f hitTextureOffset(Direction facing, Vec2f itemTextureOffset) {
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			return new Vec2f(-itemTextureOffset.x, -itemTextureOffset.y);
		}

		return itemTextureOffset;
	}

	private static Direction anvilFacing(BlockState anvilState) {
		return anvilState.get(AnvilBlock.FACING);
	}

	private static Vec2f rotate(Vec2f pos, float angleDegrees) {
		double angleRadians = Math.toRadians(angleDegrees);
		double cos = Math.cos(angleRadians);
		double sin = Math.sin(angleRadians);

		return new Vec2f(
				(float) (pos.x * cos - pos.y * sin),
				(float) (pos.x * sin + pos.y * cos)
		);
	}
}
