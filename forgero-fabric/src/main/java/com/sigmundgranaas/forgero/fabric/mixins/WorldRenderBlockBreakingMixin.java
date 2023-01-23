package com.sigmundgranaas.forgero.fabric.mixins;

import com.google.common.collect.Sets;
import com.sigmundgranaas.forgero.core.property.ActivePropertyType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.TargetTypes;
import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.core.property.attribute.SingleTarget;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.PatternBreaking;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.BlockBreakingHandler;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.PatternBreakingStrategy;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.VeinMiningStrategy;
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
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
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

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

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
    @Final
    private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

    @Shadow
    @Final
    private Int2ObjectMap<BlockBreakingInfo> blockBreakingInfos;

    @Invoker("drawCuboidShapeOutline")
    public static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {

    }

    @Inject(at = @At("HEAD"), method = "drawBlockOutline")
    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this.client.world == null) {
            return;
        }
        if (this.client.player.getMainHandStack().getItem() instanceof StateItem stateItem && this.client.player != null) {
            State tool = stateItem.dynamicState(this.client.player.getMainHandStack());
            var activeProperties = Property.stream(tool.applyProperty(new SingleTarget(TargetTypes.BLOCK, Collections.emptySet()))).getActiveProperties().toList();
            if (!activeProperties.isEmpty()) {
                List<Pair<BlockState, BlockPos>> availableBlocks;
                if (activeProperties.get(0).getActiveType() == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
                    availableBlocks = new BlockBreakingHandler(new PatternBreakingStrategy((PatternBreaking) activeProperties.get(0))).getAvailableBlocks(this.client.world, pos, this.client.player);
                } else {
                    availableBlocks = new BlockBreakingHandler(new VeinMiningStrategy((VeinBreaking) activeProperties.get(0))).getAvailableBlocks(this.client.world, pos, this.client.player);
                }
                for (var block : availableBlocks) {
                    if (!block.getRight().equals(pos)) {
                        drawCuboidShapeOutline(matrices,
                                vertexConsumer,
                                block.getLeft().getOutlineShape(this.world, block.getRight(), ShapeContext.of(entity)),
                                (double) block.getRight().getX() - cameraX, (double) block.getRight().getY() - cameraY, (double) block.getRight().getZ() - cameraZ,
                                0.0F,
                                0.0F,
                                0.0F,
                                0.4F);
                    }
                }
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ljava/util/SortedSet;add(Ljava/lang/Object;)Z"), method = "setBlockBreakingInfo")
    private void appendBlockBreakingProgressions(int entityId, BlockPos pos, int stage, CallbackInfo ci) {
        if (this.client.world == null) {
            return;
        }
        if (this.client.player.getMainHandStack().getItem() instanceof StateItem stateItem && this.client.player != null) {
            State tool = stateItem.dynamicState(this.client.player.getMainHandStack());
            var activeProperties = Property.stream(tool.applyProperty(new SingleTarget(TargetTypes.BLOCK, Collections.emptySet()))).getActiveProperties().toList();
            if (!activeProperties.isEmpty()) {
                List<Pair<BlockState, BlockPos>> availableBlocks;
                if (activeProperties.get(0).getActiveType() == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
                    availableBlocks = new BlockBreakingHandler(new PatternBreakingStrategy((PatternBreaking) activeProperties.get(0))).getAvailableBlocks(this.client.world, pos, this.client.player);
                } else {
                    availableBlocks = new BlockBreakingHandler(new VeinMiningStrategy((VeinBreaking) activeProperties.get(0))).getAvailableBlocks(this.client.world, pos, this.client.player);
                }
                HitResult cross = client.crosshairTarget;
                if (cross instanceof BlockHitResult result) {
                    var map = this.blockBreakingProgressions.get(result.getBlockPos().asLong());
                    if (map != null) {
                        BlockBreakingInfo original = map.last();
                        for (var block : availableBlocks) {
                            if (!block.getRight().equals(pos)) {
                                BlockBreakingInfo info = new BlockBreakingInfo(original.getActorId(), block.getRight());
                                info.setStage(stage);
                                this.blockBreakingProgressions.computeIfAbsent(block.getRight().asLong(), (l) -> Sets.newTreeSet()).add(info);
                            }
                        }
                    }
                }
            }
        }
    }
}
