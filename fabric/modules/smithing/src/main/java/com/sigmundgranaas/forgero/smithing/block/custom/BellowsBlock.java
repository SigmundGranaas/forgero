package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.BellowsBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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
	public ActionResult onUse(BlockState state, World world, BlockPos pos,
							  PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.getBlockEntity(pos) instanceof BellowsBlockEntity be) {
			be.startPumping(); // run on both client and server for visuals
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return type == ModBlockEntities.BELLOWS
				? (w, p, s, be) -> ((BellowsBlockEntity) be).tick() // tick on both sides for smooth animation
				: null;
	}
}
