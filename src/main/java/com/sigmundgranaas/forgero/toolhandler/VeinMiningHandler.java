package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.active.PatternBreaking;
import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        if(isBreakableBlock(world, rootPos, player) && depth >= 1){
            list.add(new Pair<>(rootState, rootPos));
            blockSet.add(rootPos);
            queue.add(rootPos);
            depth -= 1;
        }

        calculateNextBlocks(blockSet, queue, depth, rootBlock, world, player);

        for(BlockPos pos: blockSet){
            list.add(new Pair<>(world.getBlockState(pos), pos));
        }

        return list;
    }

    private void calculateNextBlocks(@NotNull HashSet<BlockPos> blockSet, @NotNull PriorityQueue<BlockPos> queue, int depth, @NotNull Block rootBlock,  @NotNull BlockView world, @NotNull PlayerEntity player) {
        if(depth < 1){
            return;
        }

        BlockPos pos = queue.peek();
        if(pos == null){
            return;
        }
        queue.remove();
        for (Direction direction: Direction.values()) {
            BlockPos newBlock = pos.offset(direction, 1);

            if(world.getBlockState(newBlock).getBlock() == rootBlock
                    && isBreakableBlock(world, newBlock, player)
            && !blockSet.contains(newBlock)){
                if(depth < 1){
                    return;
                }
                depth -= 1;
                blockSet.add(newBlock);
                queue.add(newBlock);
            }
        }
        calculateNextBlocks(blockSet, queue, depth, rootBlock, world, player);
    }


    boolean isBreakableBlock(BlockView world, BlockPos pos, PlayerEntity player){
        BlockState state = world.getBlockState(pos);
        if(state.isAir()){
            return false;
        } else if (state.getHardness(world, pos) <= 0) {
            return false;
        }else if(player.canHarvest(state)) {
            return true;
        }else{
            return true;
        }
    }
}
