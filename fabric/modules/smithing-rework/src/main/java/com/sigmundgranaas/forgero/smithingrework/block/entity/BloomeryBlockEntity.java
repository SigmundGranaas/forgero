package com.sigmundgranaas.forgero.smithingrework.block.entity;

import net.minecraft.block.BlockState;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;


public class BloomeryBlockEntity extends AbstractBloomeryBlockEntity
{
	public BloomeryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BLOOMERY, pos, state, RecipeType.SMELTING);
	}
	@Override
	protected Text getContainerName() {
		return Text.translatable("container.bloomery");
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}
}
