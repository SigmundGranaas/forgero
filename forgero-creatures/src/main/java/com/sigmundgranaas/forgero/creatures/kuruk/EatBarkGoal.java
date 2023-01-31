package com.sigmundgranaas.forgero.creatures.kuruk;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class EatBarkGoal extends Goal {
    protected static final Map<Block, Block> STRIPPED_BLOCKS;
    private static final Predicate<BlockState> BARK_PREDICATE = (BlockState state) -> state.isIn(TagKey.of(Registry.BLOCK_KEY, BlockTags.LOGS.id()));

    static {
        STRIPPED_BLOCKS = new ImmutableMap.Builder<Block, Block>().put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG).put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG).put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG).put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG).put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG).put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG).put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE).put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE).put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD).put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG).build();
    }

    private final MobEntity mob;
    private final World world;
    private int timer;

    public EatBarkGoal(MobEntity mob) {
        this.mob = mob;
        this.world = mob.world;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
            return false;
        }
        HitResult hit = this.mob.raycast(1f, 1f, false);
        if (hit instanceof BlockHitResult result) {
            return BARK_PREDICATE.test(this.world.getBlockState(result.getBlockPos()));
        }

        return false;
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
        HitResult hit = this.mob.raycast(1f, 1f, false);
        if (hit instanceof BlockHitResult result) {
            if (BARK_PREDICATE.test(this.world.getBlockState(result.getBlockPos()))) {
                if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    var stripped = getStrippedState(world.getBlockState(result.getBlockPos()));
                    if (stripped.isPresent()) {
                        world.setBlockState(result.getBlockPos(), stripped.get(), 11);
                        world.emitGameEvent(GameEvent.BLOCK_CHANGE, result.getBlockPos(), GameEvent.Emitter.of(mob, stripped.get()));
                    }
                }
                this.world.sendEntityStatus(mob, (byte) 112);
                this.mob.onEatingGrass();
            }
        }
    }

    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(STRIPPED_BLOCKS.get(state.getBlock())).map((block) ->
                block.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS))
        );
    }
}
