package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HearthBlockEntity extends CampfireBlockEntity {
	public HearthBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}
}
