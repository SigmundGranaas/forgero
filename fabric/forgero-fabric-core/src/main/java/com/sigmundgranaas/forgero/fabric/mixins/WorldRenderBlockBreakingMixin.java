package com.sigmundgranaas.forgero.fabric.mixins;

import java.util.SortedSet;

import com.google.common.collect.Sets;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.PropertyHelper;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.ToolBlockHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class WorldRenderBlockBreakingMixin {
	@Shadow
	@Final
	private MinecraftClient client;
	@Shadow
	@Nullable
	private ClientWorld world;


	@Shadow
	private int ticks;
	@Shadow
	@Final
	private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

	@Shadow
	@Final
	private Int2ObjectMap<BlockBreakingInfo> blockBreakingInfos;

	@Shadow
	private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
	}

	@Inject(at = @At("HEAD"), method = "drawBlockOutline")
	private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (this.client.world == null) {
			return;
		}
		PropertyHelper.ofPlayerHands(this.client.player)
				.flatMap(container -> ToolBlockHandler.of(container, world, pos, this.client.player))
				.ifPresent(handler -> handler.handleExceptOrigin(info -> drawShapeOutline(matrices,
						vertexConsumer,
						world.getBlockState(info).getOutlineShape(this.world, info, ShapeContext.of(entity)),
						(double) info.getX() - cameraX,
						(double) info.getY() - cameraY,
						(double) info.getZ() - cameraZ,
						0.0F,
						0.0F,
						0.0F,
						0.4F)));
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ljava/util/SortedSet;add(Ljava/lang/Object;)Z"), method = "setBlockBreakingInfo")
	private void appendBlockBreakingProgressions(int entityId, BlockPos pos, int stage, CallbackInfo ci) {
		if (this.client.world == null) {
			return;
		}

		PropertyHelper.ofPlayerHands(this.client.player)
				.flatMap(container -> ToolBlockHandler.of(container, world, pos, this.client.player))
				.ifPresent(handler -> handler.handleExceptOrigin(blockInfo -> {
					BlockBreakingInfo info = new BlockBreakingInfo(entityId, blockInfo);
					info.setStage(stage);
					info.setLastUpdateTick(this.ticks);
					//this.blockBreakingInfos.put(entityId, info);
					this.blockBreakingProgressions.computeIfAbsent(blockInfo.asLong(), (l) -> Sets.newTreeSet()).add(info);
				}));

	}

	@Inject(at = @At(value = "HEAD"), method = "removeBlockBreakingInfo")
	private void removeBlockBreaking(BlockBreakingInfo info, CallbackInfo ci) {
		if (this.client.world == null) {
			return;
		}
		PropertyHelper.ofPlayerHands(this.client.player)
				.flatMap(container -> ToolBlockHandler.of(container, world, info.getPos(), this.client.player))
				.ifPresent(handler -> handler.handleExceptOrigin(blockInfo -> {
					this.blockBreakingProgressions.remove(blockInfo.asLong());
				}));
	}
}
