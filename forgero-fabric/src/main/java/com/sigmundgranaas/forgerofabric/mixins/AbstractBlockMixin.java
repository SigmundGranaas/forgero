package com.sigmundgranaas.forgerofabric.mixins;

import com.sigmundgranaas.forgerocommon.item.StateItem;
import com.sigmundgranaas.forgero.property.ActivePropertyType;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.TargetTypes;
import com.sigmundgranaas.forgero.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.property.attribute.SingleTarget;
import com.sigmundgranaas.forgerocommon.property.handler.PatternBreaking;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgerocommon.toolhandler.BlockBreakingHandler;
import com.sigmundgranaas.forgerocommon.toolhandler.PatternBreakingStrategy;
import com.sigmundgranaas.forgerocommon.toolhandler.VeinMiningStrategy;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(at = @At("HEAD"), method = "calcBlockBreakingDelta", cancellable = true)
    public void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (player.getMainHandStack().getItem() instanceof StateItem stateItem) {
            State toolState = stateItem.dynamicState(player.getMainHandStack());
            var activeProperties = Property.stream(toolState.applyProperty(new SingleTarget(TargetTypes.BLOCK, Collections.emptySet()))).getActiveProperties().toList();
            if (!activeProperties.isEmpty()) {
                float f;
                if (activeProperties.get(0).getActiveType() == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
                    f = new BlockBreakingHandler(new PatternBreakingStrategy((PatternBreaking) activeProperties.get(0))).getHardness(state, pos, world, player);
                } else {
                    f = new BlockBreakingHandler(new VeinMiningStrategy((VeinBreaking) activeProperties.get(0))).getHardness(state, pos, world, player);
                }

                if (f == -1.0F) {
                    cir.setReturnValue(0.0F);
                } else {
                    cir.setReturnValue(f);
                }
            }
        }
    }
}
