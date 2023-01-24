package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ToolBlockHandler {

    public static ToolBlockHandler EMPTY = new ToolBlockHandler(Collections.emptyList(), new BlockPos(0, 0, 0), 0f);
    public static String BLOCK_BREAKING_PATTERN_KEY = "BLOCK_BREAKING_PATTERN";
    public static String VEIN_MINING_KEY = "VEIN_MINING";

    private final List<BlockInfo> availableBLocks;

    private final BlockPos originPos;

    private final float hardness;

    public ToolBlockHandler(List<Pair<BlockState, BlockPos>> availableBLocks, BlockPos originPos, float hardness) {
        this.availableBLocks = availableBLocks.stream().map(pair -> new BlockInfo(pair.getRight(), pair.getLeft())).toList();
        this.originPos = originPos;
        this.hardness = hardness;
    }

    public static Optional<ToolBlockHandler> of(PropertyContainer container, BlockView world, BlockPos pos, PlayerEntity player) {
        boolean has = ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BLOCK_BREAKING_PATTERN_KEY)) || ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, VEIN_MINING_KEY));
        if (has) {
            var key = new BlockHandlerCache.BlockStateCacheKey(new BlockInfo(pos, world.getBlockState(pos)), container, Direction.getEntityFacingOrder(player)[0]);
            var handler = BlockHandlerCache.computeIfAbsent(key, () -> createHandler(container, world, pos, player));
            return Optional.of(handler);
        }
        return Optional.empty();
    }

    public static ToolBlockHandler createHandler(PropertyContainer container, BlockView world, BlockPos pos, PlayerEntity player) {
        var blockMiningData = container.stream().features().filter(data -> data.type().equals(BLOCK_BREAKING_PATTERN_KEY) || data.type().equals(VEIN_MINING_KEY)).toList();
        BlockBreakingHandler handler;
        var data = blockMiningData.get(0);
        if (data.type().equals(BLOCK_BREAKING_PATTERN_KEY)) {
            handler = new BlockBreakingHandler(new PatternBreakingStrategy(data));
        } else {
            handler = new BlockBreakingHandler(new VeinMiningStrategy(data));
        }
        return new ToolBlockHandler(handler.getAvailableBlocks(world, pos, player), pos, handler.getHardness(world.getBlockState(pos), pos, world, player));
    }

    public float getHardness() {
        return hardness;
    }

    public void handle(Consumer<BlockInfo> consumer) {
        availableBLocks.forEach(consumer);
    }

    public void handleExceptOrigin(Consumer<BlockInfo> consumer) {
        availableBLocks.stream().filter(info -> !info.pos.equals(originPos)).forEach(consumer);
    }

    public record BlockInfo(BlockPos pos, BlockState state) implements CacheAbleKey {
        @Override
        public String key() {
            return pos.asLong() + state.toString();
        }
    }
}
