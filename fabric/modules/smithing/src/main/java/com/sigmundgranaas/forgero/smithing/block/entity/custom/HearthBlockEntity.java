package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import com.sigmundgranaas.forgero.smithing.networking.S2C.HearthBlockSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class HearthBlockEntity extends BlockEntity implements Inventory {
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

	public HearthBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public HearthBlockEntity(BlockPos pos, BlockState state) {
		this(com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities.HEARTH, pos, state);
	}

	public static final int ITEM_SLOT = 0;

	// Inventory methods
	@Override
	public int size() {
		return inventory.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : inventory) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStack(int slot) {
		return inventory.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack result = Inventories.splitStack(inventory, slot, amount);
		if (!result.isEmpty()) {
			markDirty();
		}
		return result;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack result = Inventories.removeStack(inventory, slot);
		if (!result.isEmpty()) {
			markDirty();
		}
		return result;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (stack.getCount() > getMaxCountPerStack()) {
			stack.setCount(getMaxCountPerStack());
		}
		markDirty();
	}

	@Override
	public void clear() {
		inventory.clear();
	}

	@Override
	public void markDirty() {
		super.markDirty();
	}

	@Override
	public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
		return true;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory);
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory);
	}

	// Sync to client when the inventory changes
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	public void markDirtyAndSync() {
		markDirty();
		if (world != null && !world.isClient) {
			HearthBlockSyncS2CPacket.send(this);
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
	}

	public void requestModelDataRefresh() {
		if (world != null && world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
	}

	// Spawns smoke particles and heats temperature items
	public static void tick(World world, BlockPos pos, BlockState state, HearthBlockEntity blockEntity) {
		if (world.isClient) {
			Random random = world.random;
			double yOffset = 1.0;
			if (state.get(com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock.LIT) && random.nextFloat() < 0.11F) {
				for (int i = 0; i < random.nextInt(2) + 2; i++) {
					net.minecraft.block.CampfireBlock.spawnSmokeParticle(
							world,
							new BlockPos(pos.getX(), pos.getY() + (int)(yOffset - 1.0), pos.getZ()),
							state.get(net.minecraft.block.CampfireBlock.SIGNAL_FIRE),
							false
					);
				}
			}
			return;
		}

		// Server-side: heat temperature items
		boolean lit = state.get(com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock.LIT);
		ItemStack slotStack = blockEntity.getStack(ITEM_SLOT);

		if (lit && !slotStack.isEmpty() && TemperatureUtils.hasMaxTemperature(slotStack)) {
			int temp = TemperatureUtils.getTemperature(slotStack);
			int maxTemp = TemperatureUtils.getMaxTemp(slotStack);
			int heatRate = 2;
			if (temp < maxTemp) {
				TemperatureUtils.setTemperature(slotStack, Math.min(maxTemp, temp + heatRate));
				blockEntity.markDirtyAndSync();
			}
		}
	}
}
