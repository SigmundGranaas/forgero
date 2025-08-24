package com.sigmundgranaas.forgero.smithing.block.renderer;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.util.MinigamePositioningUtil;

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
	public static final float RENDER_SCALE_FACTOR = 0.5f;

	private static final float ANVIL_TOP_Y = 1;
	private static final float Y_FIGHTING_OFFSET = 0.001f;
	private static final float MARKER_RENDER_OFFSET_Y = 0.025f;

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

		float itemRenderY = ANVIL_TOP_Y + Y_FIGHTING_OFFSET + 0.01f;
		matrices.translate(0.5f, itemRenderY, 0.5f);

		Direction facing = entity.getCachedState().get(AnvilBlock.FACING);
		float anvilAngleDegrees = 0.0f;
		switch (facing) {
			case EAST -> anvilAngleDegrees = -180.0f;
			case SOUTH -> anvilAngleDegrees = 90.0f;
			case WEST -> anvilAngleDegrees = 0.0f;
			case NORTH -> anvilAngleDegrees = -90.0f;
		}
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(anvilAngleDegrees));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

		Vec2f normOffset = MinigamePositioningUtil.getMorphedTextureOffsetVec2f(entity);
		matrices.translate(normOffset.x, 0, normOffset.y);
		matrices.scale(RENDER_SCALE_FACTOR, RENDER_SCALE_FACTOR, RENDER_SCALE_FACTOR);

		renderMarker(matrices, vertexConsumers, entity);
		if (MinecraftClient.getInstance().options.debugEnabled) {

		}

		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));

		int lightLevel = getLightLevel(entity.getWorld(), entity.getPos());

		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		itemRenderer.renderItem(itemStack, ModelTransformationMode.NONE, lightLevel, overlay,
					matrices, vertexConsumers, entity.getWorld(), (int) entity.getPos().asLong());

		matrices.pop();
	}

	private void renderMarker(MatrixStack matrices, VertexConsumerProvider vertexConsumers, SmithingAnvilBlockEntity entity) {
		if (!entity.getMarkerPositions().isEmpty()) {
			matrices.push();

			// Apply extra 180° rotation for North and South facings
			Direction facing = entity.getCachedState().get(AnvilBlock.FACING);
			if (facing == Direction.NORTH || facing == Direction.SOUTH) {
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
			}

			Vec2f markerPos = entity.getMarkerPositions().get(0);

			matrices.translate(markerPos.x, MARKER_RENDER_OFFSET_Y, markerPos.y);

			boolean isFast = entity.getFastMarkerIndices().contains(entity.getMarkerAttempts());
			float r = isFast ? 1.0f : 1.0f;
			float g = isFast ? 0.2f : 1.0f;
			float b = isFast ? 0.2f : 0.0f;
			float size = 0.0350f;

			VertexConsumer lineConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
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
}
