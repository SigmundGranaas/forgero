package com.sigmundgranaas.forgero.smithing.block.entity;

import static com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities.MOLD;

import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MoldBlockEntity extends BlockEntity {
	private Identifier liquid;
	private int liquidAmount;
	private int coolingTime;
	private int currentCoolingTime;
	private boolean isSolidified;
	private ItemStack result = ItemStack.EMPTY;

	public MoldBlockEntity(BlockPos pos, BlockState state) {
		super(MOLD, pos, state);
	}

	public static void tick(World world, BlockPos pos, BlockState state, MoldBlockEntity be) {
		if (!world.isClient && state.get(MoldBlock.FILLED) && !be.isSolidified) {
			be.currentCoolingTime++;
			int newProgress;
			if (be.currentCoolingTime >= be.coolingTime) {
				newProgress = 100;
			} else {
				newProgress = Math.min(100, (int) ((float) be.currentCoolingTime / be.coolingTime * 100));
			}

			// Update block state with new progress
			BlockState newState = state.with(MoldBlock.PROGRESS, newProgress);
			if (!state.equals(newState)) {
				world.setBlockState(pos, newState, 3);
			}

			// Check if solidification is complete
			if (newProgress >= 100 && !be.isSolidified) {
				be.isSolidified = true;
				world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 1.2F);
				be.markDirty();
			}
		}
	}

	public float getCoolingProgress() {
		if (coolingTime == 0) return 0f;
		return Math.min(1f, (float) currentCoolingTime / coolingTime);
	}

	public boolean isSolidified() {
		return isSolidified;
	}

	public ItemStack getResult() {
		// Only return a copy, do not clear here
		return result == null ? ItemStack.EMPTY : result.copy();
	}

	/**
	 * Returns the result and clears it from the mold.
	 */
	public ItemStack takeResult() {
		ItemStack toReturn = result == null ? ItemStack.EMPTY : result.copy();
		result = ItemStack.EMPTY;
		clear(); // Reset the mold state
		return toReturn;
	}

	public boolean isEmpty() {
		return !getCachedState().get(MoldBlock.FILLED);
	}

	public void pourLiquid(Identifier liquid, int amount, int coolingTime, ItemStack result) {
		this.liquid = liquid;
		this.liquidAmount = amount;
		this.coolingTime = coolingTime;
		this.currentCoolingTime = 0;
		this.isSolidified = false;
		this.result = result.copy();


		World world = getWorld();
		if (world != null) {
			world.setBlockState(getPos(), getCachedState()
					.with(MoldBlock.FILLED, true)
					.with(MoldBlock.PROGRESS, 0), 3);
		}
		markDirty();
	}

	private void clear() {
		this.liquid = null;
		this.liquidAmount = 0;
		this.coolingTime = 0;
		this.currentCoolingTime = 0;
		this.isSolidified = false;
		this.result = ItemStack.EMPTY;

		World world = getWorld();
		if (world != null) {
			world.setBlockState(getPos(), getCachedState()
					.with(MoldBlock.FILLED, false)
					.with(MoldBlock.PROGRESS, 0), 3);
		}
		markDirty();
	}

	public void dropContents(World world, BlockPos pos) {
		if (!result.isEmpty()) {
			ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), result);
		}
	}

	public Identifier getLiquid() {
		return liquid;
	}

	public int getLiquidAmount() {
		return liquidAmount;
	}

	public int getProgress() {
		return getCachedState().get(MoldBlock.PROGRESS);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
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
		nbt.putBoolean("IsSolidified", isSolidified);
		if (!result.isEmpty()) {
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
		isSolidified = nbt.getBoolean("IsSolidified");
		if (nbt.contains("Result")) {
			result = ItemStack.fromNbt(nbt.getCompound("Result"));
		} else {
			result = ItemStack.EMPTY;
		}
	}
}
