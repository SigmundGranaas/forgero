package com.sigmundgranaas.forgero.smithingrework.block.entity;

import com.sigmundgranaas.forgero.smithingrework.block.custom.BloomeryBlock;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class BloomeryBlockEntity extends AbstractFurnaceBlockEntity {
	public BloomeryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BLOOMERY_BLOCK, pos, state, RecipeType.SMELTING);
	}

	public static final BooleanProperty LIT = Properties.LIT;

	@Override
	protected Text getContainerName() {
		return Text.translatable("container." + getBlock().type + "_furnace");
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
		int i;
		Random random = world.random;
		if (random.nextFloat() < 0.11f) {
			for (i = 0; i < random.nextInt(2) + 2; ++i) {
				CampfireBlock.spawnSmokeParticle(world, pos, state.get(CampfireBlock.SIGNAL_FIRE), false);
			}
		}
	}


	protected BloomeryBlock getBlock() {
		return (BloomeryBlock) getCachedState().getBlock();
	}

}






