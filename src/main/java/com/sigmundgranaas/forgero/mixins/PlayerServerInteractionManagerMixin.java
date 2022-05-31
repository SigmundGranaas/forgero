package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.core.property.ActivePropertyType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.TargetTypes;
import com.sigmundgranaas.forgero.core.property.active.PatternBreaking;
import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.core.property.attribute.SingleTarget;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.toolhandler.BlockBreakingHandler;
import com.sigmundgranaas.forgero.toolhandler.PatternBreakingStrategy;
import com.sigmundgranaas.forgero.toolhandler.VeinMiningStrategy;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerServerInteractionManagerMixin {
    @Shadow
    @Final
    protected ServerPlayerEntity player;
    @Shadow
    protected ServerWorld world;

    @Shadow
    public abstract void finishMining(BlockPos pos, PlayerActionC2SPacket.Action action, String reason);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Ljava/lang/String;)V"), method = "processBlockBreakingAction")
    public void processBlockBreakingAction(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        if (player.getMainHandStack().getItem() instanceof ForgeroToolItem toolItem) {
            ForgeroTool tool = toolItem.convertItemStack(player.getMainHandStack(), toolItem.getTool());
            var activeProperties = Property.stream(tool.getProperties(new SingleTarget(TargetTypes.BLOCK, Collections.emptySet()))).getActiveProperties().toList();
            if (!activeProperties.isEmpty()) {
                List<Pair<BlockState, BlockPos>> availableBlocks;
                if (activeProperties.get(0).getActiveType() == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
                    availableBlocks = new BlockBreakingHandler(new PatternBreakingStrategy((PatternBreaking) activeProperties.get(0))).getAvailableBlocks(world, pos, player);
                } else {
                    availableBlocks = new BlockBreakingHandler(new VeinMiningStrategy((VeinBreaking) activeProperties.get(0))).getAvailableBlocks(world, pos, player);
                }
                for (var block : availableBlocks) {
                    if (!block.getRight().equals(pos)) {
                        this.finishMining(block.getRight(), action, "destroyed");
                    }
                }
            }
        }
    }
}
