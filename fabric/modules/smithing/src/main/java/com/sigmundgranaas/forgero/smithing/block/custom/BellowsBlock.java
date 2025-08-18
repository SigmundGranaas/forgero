package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.BellowsBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class BellowsBlock extends BlockWithEntity {
	public BellowsBlock(Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BellowsBlockEntity(pos, state);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state,
						 LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);

		if (!world.isClient && world.getBlockEntity(pos) instanceof BellowsBlockEntity be) {
			be.setRotation(placer.getHeadYaw()); // Store rotation based on player yaw
		}
	}
}

