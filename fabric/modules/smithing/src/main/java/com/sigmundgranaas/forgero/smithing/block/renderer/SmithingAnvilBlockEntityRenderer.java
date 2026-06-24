package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameLogic;
import com.sigmundgranaas.forgero.smithing.minigame.MinigamePositioning;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameTransforms;
import com.sigmundgranaas.forgero.smithing.util.SchematicMaterialCost;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<SmithingAnvilBlockEntity> {
	private static final float RENDER_SCALE_FACTOR = MinigameTransforms.ITEM_RENDER_SCALE;
	private static final float ITEM_RENDER_Y = 1.0f + 0.001f + 0.01f;
	private static final float BASE_ANVIL_ANGLE = 180.0f;

	// Marker rendering constants
	private static final float MARKER_RENDER_OFFSET_Y = 0.025f;
	private static final float MARKER_SIZE = 0.0350f;
	private static final float MARKER_NORMAL_RED = 1.0f;
	private static final float MARKER_NORMAL_GREEN = 1.0f;
	private static final float MARKER_NORMAL_BLUE = 0.0f;
	private static final float MARKER_COOLING_RED = 0.0f;
	private static final float MARKER_COOLING_GREEN = 0.85f;
	private static final float MARKER_COOLING_BLUE = 1.0f;
	private static final float MARKER_ALPHA = 1.0f;
	private static final float MARKER_FILL_Y_OFFSET = 0.002f;
	private static final int MARKER_FILL_ALPHA = 190;
	private static final int MARKER_POOR_RED = 255;
	private static final int MARKER_POOR_GREEN = 56;
	private static final int MARKER_POOR_BLUE = 42;
	private static final int MARKER_GOOD_RED = 255;
	private static final int MARKER_GOOD_GREEN = 214;
	private static final int MARKER_GOOD_BLUE = 48;
	private static final int MARKER_CLEAN_RED = 80;
	private static final int MARKER_CLEAN_GREEN = 255;
	private static final int MARKER_CLEAN_BLUE = 96;

	// Lighting constants
	private static final int DEFAULT_LIGHT_LEVEL = 15728880;

	public SmithingAnvilBlockEntityRenderer(@SuppressWarnings("unused") BlockEntityRendererFactory.Context context) {
		// Context parameter required by interface but not used
	}

	@Override
	public void render(SmithingAnvilBlockEntity entity, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemStack itemStack = entity.getInventory().getStack(0);

		if (itemStack.isEmpty()) {
			return;
		}

		matrices.push();
		setupItemTransforms(matrices, entity);
		renderMarker(matrices, vertexConsumers, entity, overlay);
		renderItem(matrices, vertexConsumers, entity, itemStack, light, overlay);
		matrices.pop();
	}

	private void setupItemTransforms(MatrixStack matrices, SmithingAnvilBlockEntity entity) {
		matrices.translate(0.5f, ITEM_RENDER_Y, 0.5f);

		Direction facing = entity.getCachedState().get(AnvilBlock.FACING);
		applyAnvilFacingRotation(matrices, facing);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(BASE_ANVIL_ANGLE));

		Vec2f normOffset = MinigamePositioning.getMorphedTextureOffsetVec2f(entity);
		matrices.translate(normOffset.x, 0, normOffset.y);
		matrices.scale(RENDER_SCALE_FACTOR, RENDER_SCALE_FACTOR, RENDER_SCALE_FACTOR);
	}

	private void applyAnvilFacingRotation(MatrixStack matrices, Direction facing) {
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MinigameTransforms.anvilAngleDegrees(facing)));
	}

	private void renderMarker(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			SmithingAnvilBlockEntity entity, int overlay) {
		if (entity.getMarkerPositions().isEmpty()) {
			return;
		}

		matrices.push();
		applyMarkerFacingAdjustment(matrices, entity);

		Vec2f markerPos = entity.getMarkerPositions().get(0);
		matrices.translate(markerPos.x, MARKER_RENDER_OFFSET_Y, markerPos.y);

		boolean isCoolingMarker = entity.getCoolingMarkerIndices().contains(entity.getMarkerAttempts());
		MinigameLogic logic = entity.getMinigameLogic();
		double timingProgress = logic.getMarkerTimingProgress(isCoolingMarker);
		MinigameLogic.StrikeQuality timingQuality = logic.getMarkerTimingQuality(isCoolingMarker);

		drawMarkerBox(matrices, vertexConsumers, isCoolingMarker, timingProgress, timingQuality);

		matrices.pop();
	}

	private void applyMarkerFacingAdjustment(MatrixStack matrices, SmithingAnvilBlockEntity entity) {
		Direction facing = entity.getCachedState().get(AnvilBlock.FACING);
		if (facing == Direction.NORTH || facing == Direction.SOUTH) {
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
		}
	}

	private void drawMarkerBox(
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			boolean isCoolingMarker,
			double timingProgress,
			MinigameLogic.StrikeQuality timingQuality
	) {
		float red = isCoolingMarker ? MARKER_COOLING_RED : MARKER_NORMAL_RED;
		float green = isCoolingMarker ? MARKER_COOLING_GREEN : MARKER_NORMAL_GREEN;
		float blue = isCoolingMarker ? MARKER_COOLING_BLUE : MARKER_NORMAL_BLUE;

		VertexConsumer lineConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
		WorldRenderer.drawBox(matrices, lineConsumer,
			-MARKER_SIZE, 0, -MARKER_SIZE,
			MARKER_SIZE, 0, MARKER_SIZE,
			red, green, blue, MARKER_ALPHA);

		drawMarkerTimingFill(matrices, vertexConsumers, timingProgress, timingQuality);
	}

	private void drawMarkerTimingFill(
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			double timingProgress,
			MinigameLogic.StrikeQuality timingQuality
	) {
		double centerCharge = 1.0d - Math.min(1.0d, Math.abs(timingProgress - 0.5d) * 2.0d);

		if (centerCharge <= 0.0d) {
			return;
		}

		float fillSize = (float) (MARKER_SIZE * centerCharge);
		MarkerColor color = markerColor(timingQuality);
		VertexConsumer quadConsumer = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
		Matrix4f matrix = matrices.peek().getPositionMatrix();

		quadConsumer.vertex(matrix, -fillSize, MARKER_FILL_Y_OFFSET, -fillSize)
				.color(color.red(), color.green(), color.blue(), MARKER_FILL_ALPHA)
				.next();
		quadConsumer.vertex(matrix, -fillSize, MARKER_FILL_Y_OFFSET, fillSize)
				.color(color.red(), color.green(), color.blue(), MARKER_FILL_ALPHA)
				.next();
		quadConsumer.vertex(matrix, fillSize, MARKER_FILL_Y_OFFSET, fillSize)
				.color(color.red(), color.green(), color.blue(), MARKER_FILL_ALPHA)
				.next();
		quadConsumer.vertex(matrix, fillSize, MARKER_FILL_Y_OFFSET, -fillSize)
				.color(color.red(), color.green(), color.blue(), MARKER_FILL_ALPHA)
				.next();
	}

	private MarkerColor markerColor(MinigameLogic.StrikeQuality timingQuality) {
		return switch (timingQuality) {
			case PERFECT -> new MarkerColor(MARKER_CLEAN_RED, MARKER_CLEAN_GREEN, MARKER_CLEAN_BLUE);
			case GOOD -> new MarkerColor(MARKER_GOOD_RED, MARKER_GOOD_GREEN, MARKER_GOOD_BLUE);
			case POOR -> new MarkerColor(MARKER_POOR_RED, MARKER_POOR_GREEN, MARKER_POOR_BLUE);
		};
	}

	private record MarkerColor(int red, int green, int blue) {
	}

	private void renderItem(
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			SmithingAnvilBlockEntity entity,
			ItemStack itemStack,
			int light,
			int overlay
	) {
		if (!(itemStack.getItem() instanceof MorphedItem) && itemStack.getCount() > 1) {
			renderMaterialStack(matrices, vertexConsumers, entity, itemStack, light, overlay);
			return;
		}

		renderSingleItem(matrices, vertexConsumers, entity, itemStack, light, overlay);
	}

	private void renderMaterialStack(
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			SmithingAnvilBlockEntity entity,
			ItemStack itemStack,
			int light,
			int overlay
	) {
		int count = Math.min(itemStack.getCount(), SchematicMaterialCost.MAX_MATERIAL_COST);

		for (int i = 0; i < count; i++) {
			matrices.push();

			Vec2f offset = getMaterialStackOffset(i, count);

			matrices.translate(offset.x, 0.002f * i, offset.y);

			ItemStack single = itemStack.copy();
			single.setCount(1);

			renderSingleItem(matrices, vertexConsumers, entity, single, light, overlay);

			matrices.pop();
		}
	}

	private Vec2f getMaterialStackOffset(int index, int count) {
		if (count <= 1) {
			return Vec2f.ZERO;
		}

		if (count == 2) {
			return switch (index) {
				case 0 -> new Vec2f(-0.075f, 0.0f);
				case 1 -> new Vec2f(0.075f, 0.0f);
				default -> Vec2f.ZERO;
			};
		}

		if (count == 4) {
			return switch (index) {
				case 0 -> new Vec2f(-0.08f, -0.055f);
				case 1 -> new Vec2f(0.08f, -0.055f);
				case 2 -> new Vec2f(-0.08f, 0.055f);
				case 3 -> new Vec2f(0.08f, 0.055f);
				default -> Vec2f.ZERO;
			};
		}

		return switch (index) {
			case 0 -> new Vec2f(-0.08f, -0.045f);
			case 1 -> new Vec2f(0.08f, -0.045f);
			case 2 -> new Vec2f(0.0f, 0.065f);
			default -> Vec2f.ZERO;
		};
	}

	private void renderSingleItem(
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			SmithingAnvilBlockEntity entity,
			ItemStack itemStack,
			int light,
			int overlay
	) {
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

		int lightLevel = getLightLevel(entity.getWorld(), entity.getPos());
		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

		itemRenderer.renderItem(
				itemStack,
				ModelTransformationMode.NONE,
				lightLevel,
				overlay,
				matrices,
				vertexConsumers,
				entity.getWorld(),
				(int) entity.getPos().asLong()
		);
	}

	private int getLightLevel(World world, BlockPos pos) {
		if (world == null) {
			return DEFAULT_LIGHT_LEVEL;
		}

		int blockLight = world.getLightLevel(LightType.BLOCK, pos.up());
		int skyLight = world.getLightLevel(LightType.SKY, pos.up());
		return LightmapTextureManager.pack(blockLight, skyLight);
	}
}
