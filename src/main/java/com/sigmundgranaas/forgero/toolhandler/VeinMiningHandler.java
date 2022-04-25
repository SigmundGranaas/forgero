package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record VeinMiningHandler(VeinBreaking veinMiningHandler) {
    public float getHardness(BlockState rootState, BlockPos rootPos, BlockView world, PlayerEntity player) {
        float hardness = rootState.getHardness(world, rootPos);
        if (hardness == -1.0f) {
            return hardness;
        }
        float breakingSpeed = 0.0f;
        hardness = 0.0f;
        var availableBlocks = getAvailableBlocks(world, rootPos, player);
        for (Pair<BlockState, BlockPos> state : availableBlocks) {
            // I don't know which parameters should be here
            float harvestable = player.canHarvest(state.getLeft()) ? 30 : 100;
            hardness += state.getLeft().getHardness(world, state.getRight()) * harvestable;
            breakingSpeed += player.getBlockBreakingSpeed(state.getLeft());
        }
        breakingSpeed = breakingSpeed / availableBlocks.size();
        return breakingSpeed / hardness;
    }

    public List<Pair<BlockState, BlockPos>> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
        BlockState rootState = world.getBlockState(rootPos);
        Block rootBlock = world.getBlockState(rootPos).getBlock();
        int depth = veinMiningHandler.depth();
        var list = new ArrayList<Pair<BlockState, BlockPos>>();
        var blockSet = new HashSet<BlockPos>();
        var queue = new PriorityQueue<BlockPos>();
        if (isBreakableBlock(world, rootPos, player) && depth >= 1) {
            list.add(new Pair<>(rootState, rootPos));
            blockSet.add(rootPos);
            queue.add(rootPos);
            depth -= 1;
        }

        if (rootState.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(veinMiningHandler.tag())))) {
            calculateNextBlocks(blockSet, queue, depth, rootBlock, world, player);
        }

        for (BlockPos pos : blockSet) {
            list.add(new Pair<>(world.getBlockState(pos), pos));
        }

        return list;
    }

    private void calculateNextBlocks(@NotNull HashSet<BlockPos> blockSet, @NotNull PriorityQueue<BlockPos> queue, int depth, @NotNull Block rootBlock, @NotNull BlockView world, @NotNull PlayerEntity player) {
        if (depth < 1) {
            return;
        }

        BlockPos pos = queue.peek();
        if (pos == null) {
            return;
        }
        queue.remove();
        var directions = Arrays.asList(Direction.values());
        //Reversing the direction to make the algorithm prioritize blocks in other direction that up and down
        Collections.reverse(directions);
        for (Direction direction : directions) {
            BlockPos newBlock = pos.offset(direction, 1);

            if (
                    !blockSet.contains(newBlock)
            ) {
                if (world.getBlockState(newBlock).getBlock() == rootBlock) {
                    depth -= 1;
                    blockSet.add(newBlock);
                    queue.add(newBlock);
                }

                for (Direction direction2 : directions) {
                    BlockPos newBlock2 = newBlock.offset(direction2, 1);
                    if (world.getBlockState(newBlock2).getBlock() == rootBlock && !blockSet.contains(newBlock2)) {
                        depth -= 1;
                        blockSet.add(newBlock2);
                        queue.add(newBlock2);
                    }
                }
                if (depth < 1) {
                    return;
                }
            }
        }
        calculateNextBlocks(blockSet, queue, depth, rootBlock, world, player);
    }


    boolean isBreakableBlock(BlockView world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return false;
        } else if (state.getHardness(world, pos) <= 0) {
            return false;
        } else if (player.canHarvest(state)) {
            return true;
        } else {
            return true;
        }
    }
}
