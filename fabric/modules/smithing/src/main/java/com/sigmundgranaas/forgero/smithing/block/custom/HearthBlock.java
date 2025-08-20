package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.HearthBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class HearthBlock extends CampfireBlock implements Waterloggable {
	public HearthBlock(boolean emitsParticles, int fireDamage, Settings settings) {
		super(emitsParticles, fireDamage, settings);
		this.setDefaultState(this.getStateManager().getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(LIT, true)
			.with(SIGNAL_FIRE, false)
			.with(WATERLOGGED, false));
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		super.randomDisplayTick(state, world, pos, random);
	}

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HearthBlockEntity(pos, state);
    }


}
