package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;
import com.sigmundgranaas.forgero.smithing.networking.packet.HeartBlockSyncS2CPacket;

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

	public static final int CRUCIBLE_SLOT = 0;

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
			// Remove CustomModelData if it's a CrucibleItem
			if (result.getItem() instanceof CrucibleItem) {
				removeCustomModelData(result);
			}
			markDirty();
		}
		return result;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack result = Inventories.removeStack(inventory, slot);
		if (!result.isEmpty()) {
			// Remove CustomModelData if it's a CrucibleItem
			if (result.getItem() instanceof CrucibleItem) {
				removeCustomModelData(result);
			}
			markDirty();
		}
		return result;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		// If a CrucibleItem is added, set CustomModelData = 1
		if (stack.getItem() instanceof CrucibleItem && slot == CRUCIBLE_SLOT && stack.getCount() > 0) {
			ItemStack crucibleStack = createCustomCrucibleStack(stack);
			inventory.set(slot, crucibleStack);
		} else {
			inventory.set(slot, stack);
		}
		if (stack.getCount() > getMaxCountPerStack()) {
			stack.setCount(getMaxCountPerStack());
		}
		markDirty();
	}

	// Creates a crucible stack with CustomModelData = 1, copying other NBT if present
	public ItemStack createCustomCrucibleStack(ItemStack original) {
		ItemStack stack = new ItemStack(original.getItem(), 1);
		NbtCompound nbt = original.getOrCreateNbt().copy();
		nbt.putInt("CustomModelData", 1);
		stack.setNbt(nbt);
		return stack;
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
			HeartBlockSyncS2CPacket.send(this);
			// Force clients to re-render this block even if state didn't change
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
	}

	// Call this on the client to refresh the renderer
	public void requestModelDataRefresh() {
		if (world != null && world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
	}

	// Spawns smoke particles exactly like vanilla campfire
	public static void tick(World world, BlockPos pos, BlockState state, HearthBlockEntity blockEntity) {
		if (world.isClient && state.get(com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock.LIT)) {
			Random random = world.random;
			// 50 pixels = 50/16 = 3.125 block units above base
			double yOffset = blockEntity.getStack(CRUCIBLE_SLOT).isEmpty() ? 1.0 : 2.225;
			if (random.nextFloat() < 0.11F) {
				for (int i = 0; i < random.nextInt(2) + 2; i++) {
					net.minecraft.block.CampfireBlock.spawnSmokeParticle(
						world,
						new BlockPos(pos.getX(), pos.getY() + (int)(yOffset - 1.0), pos.getZ()),
						state.get(net.minecraft.block.CampfireBlock.SIGNAL_FIRE),
						false
					);
				}
			}
		}
	}

	// Utility to remove CustomModelData from a stack
	private void removeCustomModelData(ItemStack stack) {
		if (stack.hasNbt() && stack.getNbt().contains("CustomModelData")) {
			NbtCompound nbt = stack.getNbt();
			nbt.remove("CustomModelData");
			stack.setNbt(nbt);
		}
	}
}
