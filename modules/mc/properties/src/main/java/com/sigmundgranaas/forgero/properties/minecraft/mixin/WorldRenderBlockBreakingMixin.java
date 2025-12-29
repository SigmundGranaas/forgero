package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to WorldRenderer that adds multi-block breaking support.
 *
 * <p>This mixin handles both the visual aspects (outlines) and the breaking
 * progress synchronization for AOE block breaking. It leverages Minecraft's
 * existing block breaking lifecycle by recursively calling setBlockBreakingInfo
 * for each block in the AOE, eliminating the need for manual tracking.
 */
@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class WorldRenderBlockBreakingMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Nullable
	private ClientWorld world;

	/**
	 * Invoker for the private drawCuboidShapeOutline method.
	 * Used to render block outlines for AOE blocks.
	 */
	@Invoker("drawCuboidShapeOutline")
	public static void drawCuboidShapeOutline(
			MatrixStack matrices,
			VertexConsumer vertexConsumer,
			VoxelShape shape,
			double offsetX,
			double offsetY,
			double offsetZ,
			float red,
			float green,
			float blue,
			float alpha
	) {
		throw new AssertionError("Mixin invoker should not be called directly");
	}

	/**
	 * Draws selection outlines for all blocks in the AOE when looking at a block.
	 *
	 * <p>Injects at HEAD of drawBlockOutline to add additional outlines for AOE blocks
	 * before the main block outline is drawn. Uses black color (0,0,0) with 40% opacity.
	 */
	@Inject(at = @At("HEAD"), method = "drawBlockOutline")
	private void forgero$drawAOEOutlines(
			MatrixStack matrices,
			VertexConsumer vertexConsumer,
			Entity entity,
			double cameraX,
			double cameraY,
			double cameraZ,
			BlockPos pos,
			BlockState state,
			CallbackInfo ci
	) {
		if (this.client.player == null || this.world == null) {
			return;
		}

		BlockBreakingManager.getBreakingResult(this.client.player, pos)
				.ifPresent(result -> {
					for (BlockPos aoePos : result.getAoe()) {
						drawCuboidShapeOutline(
								matrices,
								vertexConsumer,
								world.getBlockState(aoePos).getOutlineShape(this.world, aoePos, ShapeContext.of(entity)),
								(double) aoePos.getX() - cameraX,
								(double) aoePos.getY() - cameraY,
								(double) aoePos.getZ() - cameraZ,
								0.0F, // Red
								0.0F, // Green
								0.0F, // Blue
								0.4F  // Alpha
						);
					}
				});
	}

	/**
	 * Synchronizes block breaking progress across all AOE blocks.
	 *
	 * <p>Injects at HEAD of setBlockBreakingInfo and recursively calls the same method
	 * for each AOE block. This allows Minecraft to handle the full lifecycle (creation,
	 * updating, and cleanup) of BlockBreakingInfo objects automatically, eliminating
	 * the need for manual tracking and cleanup.
	 *
	 * <p>When a block breaking stage is updated:
	 * 1. This method is called for the primary block
	 * 2. We query the AOE blocks for this position
	 * 3. We recursively call setBlockBreakingInfo for each AOE block
	 * 4. Minecraft handles creation, validation, and cleanup automatically
	 * 5. When stage becomes invalid (< 0 or >= 10), Minecraft removes all entries
	 */
	@Inject(at = @At("HEAD"), method = "setBlockBreakingInfo")
	private void forgero$setAOEBreakingInfo(int entityId, BlockPos pos, int stage, CallbackInfo ci) {
		if (this.client.player == null) {
			return;
		}

		BlockBreakingManager.getBreakingResult(this.client.player, pos)
				.ifPresent(result -> {
					for (BlockPos aoePos : result.getAoe()) {
						if (!aoePos.equals(pos)) {
							// Recursive call - Minecraft handles the full lifecycle
							((WorldRenderer) (Object) this).setBlockBreakingInfo(entityId, aoePos, stage);
						}
					}
				});
	}

	// Note: No cleanup injection needed!
	// Minecraft's setBlockBreakingInfo automatically calls removeBlockBreakingInfo
	// when stage < 0 or stage >= 10, which handles cleanup for all blocks including AOE blocks.
}
