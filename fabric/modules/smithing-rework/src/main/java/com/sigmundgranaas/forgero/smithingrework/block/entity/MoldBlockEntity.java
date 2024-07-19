package com.sigmundgranaas.forgero.smithingrework.block.entity;

import static com.sigmundgranaas.forgero.smithingrework.ForgeroSmithingInitializer.MOLD_BLOCK_ENTITY;

import com.sigmundgranaas.forgero.smithingrework.block.custom.MoldBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MoldBlockEntity extends BlockEntity {
	private Identifier liquid;
	private int liquidAmount;
	private int coolingTime;
	private int currentCoolingTime;
	private boolean isSolidified;
	private ItemStack result;

	public MoldBlockEntity(BlockPos pos, BlockState state) {
		super(MOLD_BLOCK_ENTITY, pos, state);
	}

	public static void tick(World world, BlockPos pos, BlockState state, MoldBlockEntity be) {
		if (!world.isClient && state.get(MoldBlock.FILLED) && state.get(MoldBlock.PROGRESS) < 100) {
			be.currentCoolingTime++;
			int newProgress = (int) ((float) be.currentCoolingTime / be.coolingTime * 100);
			world.setBlockState(pos, state.with(MoldBlock.PROGRESS, newProgress));

			if (newProgress >= 100) {
				be.isSolidified = true;
				world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}
	}


	public boolean isSolidified() {
		return isSolidified;
	}

	public ItemStack getResult() {
		ItemStack resultCopy = result.copy();
		clear();
		return resultCopy;
	}

	public boolean isEmpty() {
		return !getCachedState().get(MoldBlock.FILLED);
	}

	public void pourLiquid(Identifier liquid, int amount, int coolingTime, ItemStack result) {
		this.liquid = liquid;
		this.liquidAmount = amount;
		this.coolingTime = coolingTime;
		this.currentCoolingTime = 0;
		this.result = result;
		World world = getWorld();
		if (world != null) {
			world.setBlockState(getPos(), getCachedState().with(MoldBlock.FILLED, true).with(MoldBlock.PROGRESS, 0));
		}
		markDirty();
	}

	private void clear() {
		this.liquid = null;
		this.liquidAmount = 0;
		this.coolingTime = 0;
		this.currentCoolingTime = 0;
		this.result = null;
		World world = getWorld();
		if (world != null) {
			world.setBlockState(getPos(), getCachedState().with(MoldBlock.FILLED, false).with(MoldBlock.PROGRESS, 0));
		}
		markDirty();
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if (liquid != null) {
			nbt.putString("Liquid", liquid.toString());
		}
		nbt.putInt("LiquidAmount", liquidAmount);
		nbt.putInt("CoolingTime", coolingTime);
		nbt.putInt("CurrentCoolingTime", currentCoolingTime);
		if (liquid != null) {
			nbt.put("Result", result.writeNbt(new NbtCompound()));
		}
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		if (nbt.contains("Liquid")) {
			liquid = new Identifier(nbt.getString("Liquid"));
		}
		liquidAmount = nbt.getInt("LiquidAmount");
		coolingTime = nbt.getInt("CoolingTime");
		currentCoolingTime = nbt.getInt("CurrentCoolingTime");
		result = ItemStack.fromNbt(nbt.getCompound("Result"));
	}
}
