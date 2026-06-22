package com.sigmundgranaas.forgero.smithing.client;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameTextureResolver;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameTransforms;
import com.sigmundgranaas.forgero.smithing.util.BoundingBoxUtil;
import com.sigmundgranaas.forgero.smithing.util.MorphingItemUtil;
import com.sigmundgranaas.forgero.smithing.util.PositionPreservingMorpher;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientMinigameTextureResolver implements MinigameTextureResolver {
	private static final int MARKER_POSITION_ATTEMPTS = 32;

	private final Random random = new Random();
	private final PositionPreservingMorpher morpher = new PositionPreservingMorpher();

	@Override
	public Vec2f itemTextureOffset(ItemStack stack) {
		if (stack.isEmpty()) {
			return Vec2f.ZERO;
		}

		try {
			BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, MinecraftClient.getInstance());

			if (image != null) {
				return offsetFromImage(image);
			}
		} catch (Exception ignored) {
		}

		return Vec2f.ZERO;
	}

	@Override
	public Vec2f morphedTextureOffset(SmithingAnvilBlockEntity entity) {
		try {
			ItemStack base = entity.getInventory().getStack(0);

			if (base.isEmpty()) {
				return Vec2f.ZERO;
			}

			MorphImages images = resolveMorphImages(entity);

			if (images == null) {
				return itemTextureOffset(base);
			}

			return offsetFromImage(morphImage(images, entity.getMorphProgress()));
		} catch (Exception ignored) {
			return Vec2f.ZERO;
		}
	}

	@Override
	public Vec2f randomMarkerPosition(ItemStack stack, BlockState anvilState) {
		try {
			BufferedImage image = RuntimeModelUtil.getFirstQuadTextureImage(stack, MinecraftClient.getInstance());

			if (image == null) {
				return Vec2f.ZERO;
			}

			return randomMarkerFromImage(image, anvilState, offsetFromImage(image), false);
		} catch (Exception ignored) {
			return Vec2f.ZERO;
		}
	}

	@Override
	public Vec2f randomMarkerPositionMorphed(SmithingAnvilBlockEntity entity) {
		try {
			ItemStack base = entity.getInventory().getStack(0);

			if (base.isEmpty()) {
				return Vec2f.ZERO;
			}

			MorphImages images = resolveMorphImages(entity);

			if (images == null) {
				return Vec2f.ZERO;
			}

			BufferedImage morph = morphImage(images, entity.getMorphProgress());
			Vec2f offsetVec = offsetFromImage(morph);
			BlockState anvilState = entity.getCachedState();
			Direction facing = anvilState.get(AnvilBlock.FACING);

			return randomMarkerFromImage(
					morph,
					anvilState,
					offsetVec,
					facing == Direction.NORTH || facing == Direction.SOUTH
			);
		} catch (Exception ignored) {
			return Vec2f.ZERO;
		}
	}

	private MorphImages resolveMorphImages(SmithingAnvilBlockEntity entity) {
		MinecraftClient client = MinecraftClient.getInstance();
		ItemStack base = entity.getInventory().getStack(0);
		BufferedImage start = null;
		BufferedImage result = null;

		if (base.getItem() instanceof MorphedItem) {
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

	private BufferedImage morphImage(MorphImages images, double progress) {
		return morpher.morphStep(images.start(), images.result(), progress);
	}

	private Vec2f offsetFromImage(BufferedImage image) {
		int[] px = BoundingBoxUtil.getItemTextureOffsetFromImage(image);
		float width = Math.max(1, image.getWidth());
		float height = Math.max(1, image.getHeight());

		return new Vec2f(px[0] / width, px[1] / height);
	}

	private Vec2f randomMarkerFromImage(
			BufferedImage image,
			BlockState anvilState,
			Vec2f offsetVec,
			boolean flipNorthSouth
	) {
		List<Point> validPixels = BoundingBoxUtil.collectValidPixels(image);

		if (validPixels.isEmpty()) {
			return Vec2f.ZERO;
		}

		int textureWidth = Math.max(1, image.getWidth());
		int textureHeight = Math.max(1, image.getHeight());

		for (int attempt = 0; attempt < MARKER_POSITION_ATTEMPTS; attempt++) {
			Point pixel = validPixels.get(random.nextInt(validPixels.size()));
			float markerXLocal = (pixel.x + 0.5f) / textureWidth - 0.5f;
			float markerZLocal = (pixel.y + 0.5f) / textureHeight - 0.5f;

			if (flipNorthSouth) {
				markerXLocal = -markerXLocal;
				markerZLocal = -markerZLocal;
			}

			if (MinigameTransforms.isInsideAnvilTopLayer(markerXLocal, markerZLocal, anvilState, offsetVec)) {
				return new Vec2f(markerXLocal, markerZLocal);
			}
		}

		return Vec2f.ZERO;
	}

	private record MorphImages(BufferedImage start, BufferedImage result) {
	}
}
