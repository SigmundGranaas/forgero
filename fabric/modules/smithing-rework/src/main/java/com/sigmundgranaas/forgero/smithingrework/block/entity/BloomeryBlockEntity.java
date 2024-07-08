package com.sigmundgranaas.forgero.smithingrework.block.entity;

import com.sigmundgranaas.forgero.smithingrework.block.custom.BloomeryBlock;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BloomeryBlockEntity extends AbstractFurnaceBlockEntity {
	public BloomeryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BLOOMERY_BLOCK, pos, state, RecipeType.SMELTING);
	}

	@Override
	protected Text getContainerName() {
		return Text.translatable("container." + getBlock().type + "_furnace");
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}

	protected BloomeryBlock getBlock() {
		return (BloomeryBlock) getCachedState().getBlock();
	}
}






