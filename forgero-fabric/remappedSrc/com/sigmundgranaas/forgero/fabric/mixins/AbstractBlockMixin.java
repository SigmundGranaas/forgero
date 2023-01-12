package com.sigmundgranaas.forgero.fabric.mixins;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    private static final LoadingCache<State, BlockBreakingHandler> handlerCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull BlockBreakingHandler load(@NotNull State stack) {
                    return new BlockBreakingHandler(new VeinMiningStrategy(new VeinBreaking(3, "", "")));
                }
            });

    private static final LoadingCache<BlockBreakingHandler, Map<BlockState, Float>> hardnessCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Map<BlockState, Float> load(@NotNull BlockBreakingHandler stack) {
                    return new ConcurrentHashMap<>();
                }
            });

    @Inject(at = @At("HEAD"), method = "calcBlockBreakingDelta", cancellable = true)
    public void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (player.getMainHandStack().getItem() instanceof StateItem stateItem) {
            State toolState = stateItem.dynamicState(player.getMainHandStack());
            var strategy = getHandler(toolState);

            if (strategy.isPresent()) {
                var handler = strategy.get();
                Map<BlockState, Float> map;
                try {
                    map = hardnessCache.get(handler, ConcurrentHashMap::new);
                } catch (Exception e) {
                    map = new ConcurrentHashMap<>();
                }
                if (map.containsKey(state)) {
                    cir.setReturnValue(map.get(state));
                    cir.cancel();
                }
                float f = strategy.get().getHardness(state, pos, world, player);
                if (f == -1.0F) {
                    cir.setReturnValue(0.0F);
                } else {
                    hardnessCache.getUnchecked(handler).put(state, f);
                    cir.setReturnValue(f);
                }
            }
        }
    }

    private Optional<BlockBreakingHandler> getHandler(State state) {
        if (handlerCache.asMap().containsKey(state)) {
            return Optional.of(handlerCache.getUnchecked(state));
        }

        var activeProperties = Property.stream(state.applyProperty(new SingleTarget(TargetTypes.BLOCK, Collections.emptySet()))).getActiveProperties().toList();
        if (!activeProperties.isEmpty()) {
            BlockBreakingHandler handler;
            if (activeProperties.get(0).getActiveType() == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
                handler = new BlockBreakingHandler(new PatternBreakingStrategy((PatternBreaking) activeProperties.get(0)));
            } else {
                handler = new BlockBreakingHandler(new VeinMiningStrategy((VeinBreaking) activeProperties.get(0)));
            }
            handlerCache.put(state, handler);
            return Optional.of(handler);
        }
        return Optional.empty();
    }
}
