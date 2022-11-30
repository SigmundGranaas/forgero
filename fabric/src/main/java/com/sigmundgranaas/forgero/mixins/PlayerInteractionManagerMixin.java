package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.property.ActivePropertyType;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.TargetTypes;
import com.sigmundgranaas.forgero.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.property.attribute.SingleTarget;
import com.sigmundgranaas.forgero.property.handler.PatternBreaking;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.toolhandler.BlockBreakingHandler;
import com.sigmundgranaas.forgero.toolhandler.PatternBreakingStrategy;
import com.sigmundgranaas.forgero.toolhandler.VeinMiningStrategy;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private float currentBreakingProgress;

    @Shadow
    public abstract boolean breakBlock(BlockPos pos);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", shift = At.Shift.AFTER), method = "updateBlockBreakingProgress")
    public void calcBlockBreakingDelta(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (this.currentBreakingProgress >= 1.0F) {
            if (this.client.player.getMainHandStack().getItem() instanceof StateItem stateItem && client.player != null) {
                State tool = stateItem.dynamicState(client.player.getMainHandStack());
                var activeProperties = Property.stream(tool.getProperties(new SingleTarget(TargetTypes.BLOCK, Collections.emptySet()))).getActiveProperties().toList();
                if (!activeProperties.isEmpty()) {
                    List<Pair<BlockState, BlockPos>> availableBlocks;
                    if (activeProperties.get(0).getActiveType() == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
                        availableBlocks = new BlockBreakingHandler(new PatternBreakingStrategy((PatternBreaking) activeProperties.get(0))).getAvailableBlocks(this.client.world, pos, this.client.player);
                    } else {
                        availableBlocks = new BlockBreakingHandler(new VeinMiningStrategy((VeinBreaking) activeProperties.get(0))).getAvailableBlocks(this.client.world, pos, this.client.player);
                    }

                    for (var block : availableBlocks) {
                        if (!block.getRight().equals(pos)) {
                            this.breakBlock(block.getRight());
                        }
                    }
                }
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V"), method = "updateBlockBreakingProgress", cancellable = true)
    public void updateSecondaryBlocks(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (this.client.player.getMainHandStack().getItem() instanceof StateItem tool) {
            //this.client.world.setBlockBreakingInfo(this.client.player.getId(), new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), (int) (this.currentBreakingProgress * 10.0F) - 1);
        }
    }
}

