package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock;
import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryExtensionBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BloomeryBlockEntity extends BlockEntity {
	// Fuel time tracking
	private int fuelTime = 0;
	private int maxFuelTime = 0;

	// Single-slot fuel inventory
	private ItemStack fuelSlot = ItemStack.EMPTY;

	public BloomeryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BLOOMERY, pos, state);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, BloomeryBlockEntity blockEntity) {
		boolean wasLit = state.get(BloomeryBlock.LIT);
		boolean dirty = false;

		// Handle fuel consumption
		if (blockEntity.isBurning()) {
			blockEntity.fuelTime--;
			if (blockEntity.fuelTime <= 0) {
				blockEntity.fuelTime = 0;
				blockEntity.maxFuelTime = 0;
				dirty = true;
			}
		}

		// Update block state if lighting changed
		boolean isLit = blockEntity.isBurning();
		if (wasLit != isLit) {
			state = state.with(BloomeryBlock.LIT, isLit);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);

			// Sync lit state with adjacent bloomery extension blocks
			blockEntity.syncLitStateWithExtensions(world, pos, isLit);
			dirty = true;
		}

		// Mark dirty if needed
		if (dirty) {
			blockEntity.markDirty();
		}
	}

	/**
	 * Lights the bloomery with the given fuel time
	 */
	public void lightWithFuel(int fuelTime) {
		this.fuelTime = fuelTime;
		this.maxFuelTime = fuelTime;
		markDirty();
	}

	/**
	 * Checks if the bloomery is currently burning
	 */
	public boolean isBurning() {
		return fuelTime > 0;
	}

	/**
	 * Gets the current fuel time remaining
	 */
	public int getFuelTime() {
		return fuelTime;
	}

	/**
	 * Gets the maximum fuel time for this burn cycle
	 */
	public int getMaxFuelTime() {
		return maxFuelTime;
	}

	/**
	 * Insert coal/charcoal into the fuel slot.
	 * Returns true if successful.
	 */
	public boolean insertFuel(ItemStack stack) {
		if (!isFuel(stack)) return false;
		if (fuelSlot.isEmpty()) {
			fuelSlot = new ItemStack(stack.getItem(), 1);
			return true;
		} else if (fuelSlot.isOf(stack.getItem()) && fuelSlot.getCount() < fuelSlot.getMaxCount()) {
			fuelSlot.increment(1);
			return true;
		}
		return false;
	}

	/**
	 * Consumes one fuel from inventory and returns its burn time.
	 * Returns 0 if no fuel available.
	 */
	public int consumeFuelForLighting() {
		if (fuelSlot.isEmpty()) return 0;
		int burnTime = getFuelTime(fuelSlot);
		fuelSlot.decrement(1);
		if (fuelSlot.getCount() <= 0) fuelSlot = ItemStack.EMPTY;
		return burnTime;
	}

	private boolean isFuel(ItemStack stack) {
		return stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL);
	}

	private int getFuelTime(ItemStack stack) {
		if (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL)) {
			return 1600;
		}
		return 0;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		fuelTime = nbt.getInt("FuelTime");
		maxFuelTime = nbt.getInt("MaxFuelTime");
		if (nbt.contains("FuelSlot")) {
			fuelSlot = ItemStack.fromNbt(nbt.getCompound("FuelSlot"));
		} else {
			fuelSlot = ItemStack.EMPTY;
		}
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putInt("FuelTime", fuelTime);
		nbt.putInt("MaxFuelTime", maxFuelTime);
		if (!fuelSlot.isEmpty()) {
			nbt.put("FuelSlot", fuelSlot.writeNbt(new NbtCompound()));
		}
	}

	/**
	 * Synchronizes the lit state with all adjacent bloomery extension blocks
	 */
	public void syncLitStateWithExtensions(World world, BlockPos pos, boolean isLit) {
		// Check all 6 directions for extension blocks and sync their lit state
		for (Direction direction : Direction.values()) {
			BlockPos adjacentPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(adjacentPos);

			// Check if it's a BloomeryExtensionBlock and update its LIT state
			if (adjacentState.getBlock() instanceof BloomeryExtensionBlock) {
				BlockState newState = adjacentState.with(BloomeryExtensionBlock.LIT, isLit);
				world.setBlockState(adjacentPos, newState, Block.NOTIFY_ALL);
			}
		}
	}
}
