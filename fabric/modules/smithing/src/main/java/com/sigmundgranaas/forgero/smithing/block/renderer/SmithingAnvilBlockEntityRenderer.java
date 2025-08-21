package com.sigmundgranaas.forgero.smithing.block.renderer;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.util.BoundingBoxUtil;
import com.sigmundgranaas.forgero.smithing.util.MinigamePositioningUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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


	public SmithingAnvilBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(SmithingAnvilBlockEntity entity, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
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
		int[] offset = itemTextureOffsetCache.computeIfAbsent(itemStack, MinigamePositioningUtil::getItemTextureOffset);
		float dx = offset[0] / 16.0f;
		float dz = offset[1] / 16.0f;
		dx = Math.max(-0.2f, Math.min(0.2f, dx));
		dz = Math.max(-0.2f, Math.min(0.2f, dz));
		matrices.translate(dx, 0, dz);

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

		// Render the actual 3D item model
		int lightLevel = getLightLevel(entity.getWorld(), entity.getPos());
		itemRenderer.renderItem(itemStack, ModelTransformationMode.NONE, lightLevel, overlay,
				matrices, vertexConsumers, entity.getWorld(), (int) entity.getPos().asLong());


		matrices.pop();
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
			matrices.translate(markerPos.x, MARKER_RENDER_OFFSET_Y, markerPos.y); // Use MARKER_RENDER_OFFSET_Y

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
			return 15728880; // Full brightness fallback
		}
		// Get light level from the block position *above* the anvil, where the item is rendered
		int blockLight = world.getLightLevel(LightType.BLOCK, pos.up());
		int skyLight = world.getLightLevel(LightType.SKY, pos.up());
		return LightmapTextureManager.pack(blockLight, skyLight);
	}
}
