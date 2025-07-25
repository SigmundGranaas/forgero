package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock;
import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryExtensionBlock;
import com.sigmundgranaas.forgero.smithing.fuel.BloomeryFuelSystem;
import com.sigmundgranaas.forgero.smithing.fuel.FuelType;
import com.sigmundgranaas.forgero.smithing.block.custom.BellowsBlock;

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

	// Fuel temperature system
	private BloomeryFuelSystem fuelSystem = new BloomeryFuelSystem();

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
	 * Gets the fuel slot for visual indicator purposes
	 */
	public ItemStack getFuelSlot() {
		return fuelSlot;
	}

	/**
	 * Insert coal/charcoal into the fuel slot.
	 * Returns true if successful.
	 */
	public boolean insertFuel(ItemStack stack) {
		FuelType fuelType = getFuelType(stack);
		if (fuelType == null) return false;

		if (fuelSlot.isEmpty()) {
			fuelSlot = new ItemStack(stack.getItem(), 1);
			fuelSystem.addFuel(fuelType, 1);
			markDirty();
			syncToClient(); // Sync fuel inventory change to client
			return true;
		} else if (fuelSlot.isOf(stack.getItem()) && fuelSlot.getCount() < fuelSlot.getMaxCount()) {
			fuelSlot.increment(1);
			fuelSystem.addFuel(fuelType, 1);
			markDirty();
			syncToClient(); // Sync fuel inventory change to client
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
		fuelSystem.consumeFuel();
		markDirty();
		syncToClient(); // Sync fuel inventory change to client
		return burnTime;
	}

	/**
	 * Gets the current temperature of the bloomery based on fuel
	 */
	public int getCurrentTemperature() {
		int baseTemperature = fuelSystem.getCurrentTemperature();

		// If fuel system returns 0 but bloomery is lit, use the fuel type's max temperature
		if (baseTemperature == 0 && isBurning()) {
			// Determine base temperature from available fuel types
			if (canReachTemperature(900)) {
				baseTemperature = 1000; // Charcoal available
			} else if (canReachTemperature(750)) {
				baseTemperature = 800;  // Coal available
			}
		}

		int bellowsBoost = getBellowsTemperatureBoost();
		return baseTemperature + bellowsBoost;
	}

	/**
	 * Gets the total temperature boost from all connected bellows
	 */
	private int getBellowsTemperatureBoost() {
		if (world == null) return 0;

		int totalBoost = 0;

		// Check all horizontal directions for active bellows
		for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
			BlockPos bellowsPos = pos.offset(direction);
			BlockEntity entity = world.getBlockEntity(bellowsPos);

			if (entity instanceof BellowsBlockEntity bellows) {
				totalBoost += bellows.getTemperatureBoost();
			}
		}

		return totalBoost;
	}

	/**
	 * Checks if the bloomery can reach the target temperature
	 */
	public boolean canReachTemperature(int targetTemp) {
		// Check base fuel system capability plus potential bellows boost
		int maxPossibleTemp = fuelSystem.getCurrentTemperature() + getMaxPotentialBellowsBoost();
		return maxPossibleTemp >= targetTemp || fuelSystem.canReachTemperature(targetTemp);
	}

	/**
	 * Gets the maximum potential temperature boost from connected bellows
	 */
	private int getMaxPotentialBellowsBoost() {
		if (world == null) return 0;

		int maxBoost = 0;

		// Check all horizontal directions for bellows (even if not currently active)
		for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
			BlockPos bellowsPos = pos.offset(direction);
			BlockEntity entity = world.getBlockEntity(bellowsPos);

			if (entity instanceof BellowsBlockEntity) {
				maxBoost += 200; // Each bellows can provide 200 temperature boost
			}
		}

		return maxBoost;
	}

	private FuelType getFuelType(ItemStack stack) {
		if (stack.isOf(Items.COAL)) return FuelType.COAL;
		if (stack.isOf(Items.CHARCOAL)) return FuelType.CHARCOAL;
		return null;
	}

	private boolean isFuel(ItemStack stack) {
		return getFuelType(stack) != null;
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

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound nbt = new NbtCompound();
		writeNbt(nbt);
		return nbt;
	}

	@Override
	public net.minecraft.network.packet.Packet<net.minecraft.network.listener.ClientPlayPacketListener> toUpdatePacket() {
		return net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket.create(this);
	}

	/**
	 * Syncs the lit state with adjacent bloomery extension blocks
	 */
	public void syncLitStateWithExtensions(World world, BlockPos pos, boolean isLit) {
		// Check all horizontal directions for bloomery extension blocks
		for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
			BlockPos extensionPos = pos.offset(direction);
			BlockState extensionState = world.getBlockState(extensionPos);

			if (extensionState.getBlock() instanceof BloomeryExtensionBlock) {
				// Update the extension block's lit state to match the main bloomery
				BlockState newState = extensionState.with(BloomeryExtensionBlock.LIT, isLit);
				world.setBlockState(extensionPos, newState, Block.NOTIFY_ALL);
			}
		}
	}

	/**
	 * Syncs data to client when fuel inventory changes
	 */
	private void syncToClient() {
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
		}
	}
}
