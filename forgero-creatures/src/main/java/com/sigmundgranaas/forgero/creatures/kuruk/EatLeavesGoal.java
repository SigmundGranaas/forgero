package com.sigmundgranaas.forgero.creatures.kuruk;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.EnumSet;
import java.util.function.Predicate;

public class EatLeavesGoal extends Goal {
    private static final int MAX_TIMER = 40;
    private static final Predicate<BlockState> LEAVES_PREDICATE = (BlockState state) -> state.isIn(TagKey.of(Registry.BLOCK_KEY, BlockTags.LEAVES.id()));
    private final MobEntity mob;
    private final World world;
    private int timer;

    public EatLeavesGoal(MobEntity mob) {
        this.mob = mob;
        this.world = mob.world;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
            return false;
        }
        BlockPos blockPos = this.mob.getBlockPos();
        if (LEAVES_PREDICATE.test(this.world.getBlockState(blockPos))) {
            return true;
        }
        return this.world.getBlockState(blockPos.down()).isOf(Blocks.GRASS_BLOCK);
    }

    @Override
    public void start() {
        this.timer = this.getTickCount(40);
        this.world.sendEntityStatus(this.mob, EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART);
        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.timer = 0;
    }

    @Override
    public boolean shouldContinue() {
        return this.timer > 0;
    }

    public int getTimer() {
        return this.timer;
    }

    @Override
    public void tick() {
        this.timer = Math.max(0, this.timer - 1);
        if (this.timer != this.getTickCount(4)) {
            return;
        }
        BlockPos blockPos = this.mob.getBlockPos();
        if (LEAVES_PREDICATE.test(this.world.getBlockState(blockPos))) {
            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                this.world.breakBlock(blockPos, false);
            }
            this.mob.onEatingGrass();
        } else {
            BlockPos blockPos2 = blockPos.down();
            if (this.world.getBlockState(blockPos2).isOf(Blocks.GRASS_BLOCK)) {
                if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    this.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, blockPos2, Block.getRawIdFromState(Blocks.GRASS_BLOCK.getDefaultState()));
                    this.world.setBlockState(blockPos2, Blocks.DIRT.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
                this.mob.onEatingGrass();
            }
        }
    }
}
