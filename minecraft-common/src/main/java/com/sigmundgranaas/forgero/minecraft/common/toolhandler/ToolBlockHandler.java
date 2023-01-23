package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ToolBlockHandler {
    public static String BLOCK_BREAKING_PATTERN_KEY = "BLOCK_BREAKING_PATTERN";
    public static String VEIN_MINING_KEY = "VEIN_MINING";

    private final List<BlockInfo> availableBLocks;

    private final BlockPos originPos;

    public ToolBlockHandler(List<Pair<BlockState, BlockPos>> availableBLocks, BlockPos originPos) {
        this.availableBLocks = availableBLocks.stream().map(pair -> new BlockInfo(pair.getRight(), pair.getLeft())).toList();
        this.originPos = originPos;
    }

    public static Optional<ToolBlockHandler> of(PropertyContainer container, World world, BlockPos pos, PlayerEntity player) {
        boolean has = ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BLOCK_BREAKING_PATTERN_KEY)) || ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, VEIN_MINING_KEY));
        if (has) {
            List<Pair<BlockState, BlockPos>> availableBlocks;
            var blockMiningData = container.stream().features().filter(data -> data.type().equals(BLOCK_BREAKING_PATTERN_KEY) || data.type().equals(VEIN_MINING_KEY)).toList();
            var data = blockMiningData.get(0);
            if (data.type().equals(BLOCK_BREAKING_PATTERN_KEY)) {
                availableBlocks = new BlockBreakingHandler(new PatternBreakingStrategy(data)).getAvailableBlocks(world, pos, player);
            } else {
                availableBlocks = new BlockBreakingHandler(new VeinMiningStrategy(data)).getAvailableBlocks(world, pos, player);
            }
            return Optional.of(new ToolBlockHandler(availableBlocks, pos));
        }
        return Optional.empty();
    }

    public void handle(Consumer<BlockInfo> consumer) {
        availableBLocks.forEach(consumer);
    }

    public void handleExceptOrigin(Consumer<BlockInfo> consumer) {
        availableBLocks.stream().filter(info -> !info.pos.equals(originPos)).forEach(consumer);
    }

    public record BlockInfo(BlockPos pos, BlockState state) {
    }
}
