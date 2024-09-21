package com.sigmundgranaas.forgero.fabric.mixins;

import com.google.common.collect.Sets;
import com.sigmundgranaas.forgero.toolhandler.PropertyHelper;
import com.sigmundgranaas.forgero.toolhandler.block.ToolBlockHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class WorldRenderBlockBreakingMixin {
	@Unique
	@Final
	private final Map<Long, Set<Long>> forgero$extraBlockBreakingInfos = new ConcurrentHashMap<>();
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

	@Invoker("drawCuboidShapeOutline")
	public static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {

	}

	@Shadow
	protected abstract void removeBlockBreakingInfo(BlockBreakingInfo info);

	@Inject(at = @At("HEAD"), method = "drawBlockOutline")
	private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (this.client.world == null) {
			return;
		}
		PropertyHelper.ofPlayerHands(this.client.player)
				.flatMap(container -> ToolBlockHandler.of(container, pos, this.client.player))
				.ifPresent(handler -> handler.handleExceptOrigin(info -> drawCuboidShapeOutline(matrices,
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
				.flatMap(container -> ToolBlockHandler.of(container, pos, this.client.player))
				.ifPresent(handler -> {
					forgero$extraBlockBreakingInfos.put(pos.asLong(), new HashSet<>());
					handler.handleExceptOrigin(blockInfo -> {
						BlockBreakingInfo info = new BlockBreakingInfo(entityId, blockInfo);
						info.setStage(stage);
						info.setLastUpdateTick(this.ticks);
						this.forgero$extraBlockBreakingInfos.get(pos.asLong()).add(info.getPos().asLong());
						this.blockBreakingProgressions.computeIfAbsent(blockInfo.asLong(), (l) -> Sets.newTreeSet()).add(info);
					});
				});
	}

	@Inject(at = @At(value = "HEAD"), method = "removeBlockBreakingInfo")
	private void removeBlockBreaking(BlockBreakingInfo info, CallbackInfo ci) {
		Long pos = info.getPos().asLong();
		if (forgero$extraBlockBreakingInfos.containsKey(pos)) {
			forgero$extraBlockBreakingInfos.get(pos).forEach(l -> {
				long l1 = l;
				if (blockBreakingProgressions.containsKey(l1)) {
					this.blockBreakingProgressions.remove(l1);
				}
			});
			forgero$extraBlockBreakingInfos.remove(pos);
		}
	}
}
