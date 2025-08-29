package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.google.common.collect.Sets;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingManager;
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
		// Invoker content
	}

	@Inject(at = @At("HEAD"), method = "drawBlockOutline")
	private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (this.client.world == null || this.client.player == null) {
			return;
		}
		BlockBreakingManager.getBreakingResult(this.client.player, pos)
				.ifPresent(result -> {
					for (BlockPos extraPos : result.getAoe()) {
						drawCuboidShapeOutline(matrices,
								vertexConsumer,
								world.getBlockState(extraPos).getOutlineShape(this.world, extraPos, ShapeContext.of(entity)),
								(double) extraPos.getX() - cameraX,
								(double) extraPos.getY() - cameraY,
								(double) extraPos.getZ() - cameraZ,
								0.0F,
								0.0F,
								0.0F,
								0.4F);
					}
				});
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ljava/util/SortedSet;add(Ljava/lang/Object;)Z"), method = "setBlockBreakingInfo")
	private void appendBlockBreakingProgressions(int entityId, BlockPos pos, int stage, CallbackInfo ci) {
		if (this.client.world == null || this.client.player == null) {
			return;
		}
		BlockBreakingManager.getBreakingResult(this.client.player, pos)
				.ifPresent(result -> {
					forgero$extraBlockBreakingInfos.put(pos.asLong(), new HashSet<>());
					Set<Long> extraBlockPositions = forgero$extraBlockBreakingInfos.get(pos.asLong());

					for (BlockPos extraPos : result.getAoe()) {
						BlockBreakingInfo info = new BlockBreakingInfo(entityId, extraPos);
						info.setStage(stage);
						info.setLastUpdateTick(this.ticks);
						extraBlockPositions.add(extraPos.asLong());
						this.blockBreakingProgressions.computeIfAbsent(extraPos.asLong(), (l) -> Sets.newTreeSet()).add(info);
					}
				});
	}

	@Inject(at = @At(value = "HEAD"), method = "removeBlockBreakingInfo")
	private void removeBlockBreaking(BlockBreakingInfo info, CallbackInfo ci) {
		Long pos = info.getPos().asLong();
		if (forgero$extraBlockBreakingInfos.containsKey(pos)) {
			forgero$extraBlockBreakingInfos.get(pos).forEach(l -> {
				if (blockBreakingProgressions.containsKey(l)) {
					this.blockBreakingProgressions.remove(l);
				}
			});
			forgero$extraBlockBreakingInfos.remove(pos);
		}
	}
}
