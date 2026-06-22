package com.sigmundgranaas.forgero.smithing.minigame;

import java.util.Random;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class MinigamePositioning {
	private static final MinigameTextureResolver FALLBACK_TEXTURE_RESOLVER = new FallbackTextureResolver();

	private static MinigameTextureResolver textureResolver = FALLBACK_TEXTURE_RESOLVER;

	private MinigamePositioning() {
	}

	public static void setTextureResolver(MinigameTextureResolver resolver) {
		textureResolver = resolver == null ? FALLBACK_TEXTURE_RESOLVER : resolver;
	}

	public static Vec2f worldHitToItemLocal(
			BlockHitResult hit,
			BlockState anvilState,
			Vec2f itemTextureOffset
	) {
		return MinigameTransforms.worldHitToItemLocal(hit, anvilState, itemTextureOffset);
	}

	public static Vec3d itemLocalToWorld(
			Vec2f itemLocalPos,
			BlockPos anvilBlockPos,
			BlockState anvilState,
			Vec2f itemTextureOffset,
			float baseY
	) {
		return MinigameTransforms.itemLocalToWorld(
				itemLocalPos,
				anvilBlockPos,
				anvilState,
				itemTextureOffset,
				baseY
		);
	}

	public static Vec2f getItemTextureOffsetVec2f(ItemStack stack) {
		return textureResolver.itemTextureOffset(stack);
	}

	public static Vec2f getMorphedTextureOffsetVec2f(SmithingAnvilBlockEntity entity) {
		return textureResolver.morphedTextureOffset(entity);
	}

	public static Vec2f getRandomMarkerPosition(ItemStack stack, BlockState anvilState) {
		return textureResolver.randomMarkerPosition(stack, anvilState);
	}

	public static Vec2f getRandomMarkerPositionMorphed(SmithingAnvilBlockEntity entity) {
		return textureResolver.randomMarkerPositionMorphed(entity);
	}

	private static final class FallbackTextureResolver implements MinigameTextureResolver {
		private static final int MARKER_POSITION_ATTEMPTS = 32;
		private static final float MARKER_LOCAL_BOUND = 0.45f;

		private final Random random = new Random();

		@Override
		public Vec2f itemTextureOffset(ItemStack stack) {
			return Vec2f.ZERO;
		}

		@Override
		public Vec2f morphedTextureOffset(SmithingAnvilBlockEntity entity) {
			return Vec2f.ZERO;
		}

		@Override
		public Vec2f randomMarkerPosition(ItemStack stack, BlockState anvilState) {
			return randomMarkerPosition(anvilState);
		}

		@Override
		public Vec2f randomMarkerPositionMorphed(SmithingAnvilBlockEntity entity) {
			return randomMarkerPosition(entity.getCachedState());
		}

		private Vec2f randomMarkerPosition(BlockState anvilState) {
			for (int attempt = 0; attempt < MARKER_POSITION_ATTEMPTS; attempt++) {
				float markerXLocal = randomLocalCoordinate();
				float markerZLocal = randomLocalCoordinate();

				if (MinigameTransforms.isInsideAnvilTopLayer(
						markerXLocal,
						markerZLocal,
						anvilState,
						Vec2f.ZERO
				)) {
					return new Vec2f(markerXLocal, markerZLocal);
				}
			}

			return Vec2f.ZERO;
		}

		private float randomLocalCoordinate() {
			return (random.nextFloat() * 2.0f - 1.0f) * MARKER_LOCAL_BOUND;
		}
	}
}
