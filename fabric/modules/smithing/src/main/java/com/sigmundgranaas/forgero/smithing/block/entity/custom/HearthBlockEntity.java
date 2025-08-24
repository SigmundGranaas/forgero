package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;
import com.sigmundgranaas.forgero.smithing.networking.S2C.HeartBlockSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.recipe.Custom.MetalSmeltingRecipe;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class HearthBlockEntity extends BlockEntity implements Inventory {
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

	// Smelting state
	private boolean smelting = false;
	private int smeltTime = 0;
	private int smeltTimeTotal = 0;

	// Crucible NBT keys (mirror MetalSmeltingRecipe)
	private static final String STORED_ITEM_KEY = "StoredItem";
	private static final String COUNT_KEY = "Count";

	// Add this field to track if smelting just finished
	private boolean smeltingJustFinished = false;

	public HearthBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public HearthBlockEntity(BlockPos pos, BlockState state) {
		this(com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities.HEARTH, pos, state);
	}

	public static final int CRUCIBLE_SLOT = 0;

	// Expose smelting state to block interactions
	public boolean isSmelting() {
		return smelting;
	}

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
		// Smelting state
		this.smelting = nbt.getBoolean("Smelting");
		this.smeltTime = nbt.getInt("SmeltTime");
		this.smeltTimeTotal = nbt.getInt("SmeltTimeTotal");
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory);
		// Smelting state
		nbt.putBoolean("Smelting", this.smelting);
		nbt.putInt("SmeltTime", this.smeltTime);
		nbt.putInt("SmeltTimeTotal", this.smeltTimeTotal);
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

	// Spawns smoke particles exactly like vanilla campfire (client), and drives smelting (server)
	public static void tick(World world, BlockPos pos, BlockState state, HearthBlockEntity blockEntity) {
		if (world.isClient) {
			Random random = world.random;
			// 50 pixels = 50/16 = 3.125 block units above base
			double yOffset = blockEntity.getStack(CRUCIBLE_SLOT).isEmpty() ? 1.0 : 2.225; // keep your custom offset
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

			// Play lava sound if smelting is done and crucible is still present
			if (!blockEntity.smelting && !blockEntity.getStack(CRUCIBLE_SLOT).isEmpty() && blockEntity.smeltTimeTotal > 0) {
				if (world.getTime() % 20 == 0) { // every second
					world.playSound(
						pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
						SoundEvents.BLOCK_LAVA_POP,
						SoundCategory.BLOCKS,
						0.5f,
						1.0f,
						false
					);
				}
			}
			return;
		}

		// Server-side: drive smelting and heating
		boolean lit = state.get(com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock.LIT);
		ItemStack slotStack = blockEntity.getStack(CRUCIBLE_SLOT);

		// Heat up TemperatureItems if present and hearth is lit
		if (lit && !slotStack.isEmpty() && TemperatureUtils.hasMaxTemperature(slotStack)) {
			int temp = TemperatureUtils.getTemperature(slotStack);
			int maxTemp = TemperatureUtils.getMaxTemp(slotStack);
			int heatRate = 20; // Amount to heat per tick, adjust as needed
			if (temp < maxTemp) {
				TemperatureUtils.setTemperature(slotStack, Math.min(maxTemp, temp + heatRate));
				blockEntity.markDirtyAndSync();
			}
			// Don't return; allow smelting logic for crucible below
		}

		// If we aren't smelting, try to start when conditions are met (only for CrucibleItem)
		if (!blockEntity.smelting) {
			if (lit && !slotStack.isEmpty() && slotStack.getItem() instanceof CrucibleItem) {
				blockEntity.tryStartSmelting();
			}
			return;
		}

		// If we are smelting, only progress while lit and crucible still present
		if (!lit || slotStack.isEmpty() || !(slotStack.getItem() instanceof CrucibleItem)) {
			// Pause smelting if unlit or crucible missing; do not reset to preserve progress while relighting
			return;
		}

		// Progress
		blockEntity.smeltTime++;
		if (blockEntity.smeltTime >= blockEntity.smeltTimeTotal) {
			// Finish: craft and apply result
			blockEntity.finishSmelting();
		}

		// Optionally sync CRUCIBLE_PRESENT if crucible is added/removed by other means
		if (!world.isClient) {
			boolean cruciblePresent = !blockEntity.getStack(CRUCIBLE_SLOT).isEmpty();
			if (state.contains(com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock.CRUCIBLE_PRESENT)
				&& state.get(com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock.CRUCIBLE_PRESENT) != cruciblePresent) {
				world.setBlockState(pos, state.with(com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock.CRUCIBLE_PRESENT, cruciblePresent), 3);
			}
		}
	}

	private void tryStartSmelting() {
		if (world == null || world.isClient) return;
		ItemStack crucible = getStack(CRUCIBLE_SLOT);
		if (crucible.isEmpty() || !(crucible.getItem() instanceof CrucibleItem)) return;

		world.getRecipeManager()
			.getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, this, world)
			.ifPresent(recipe -> {
				int count = getStoredOreCount(crucible);
				if (count <= 0) return;

				this.smelting = true;
				this.smeltTime = 0;
				this.smeltTimeTotal = Math.max(1, recipe.getCookTime() * count);
				markDirtyAndSync();
			});
	}

	private void finishSmelting() {
		if (world == null || world.isClient) return;

		// Revalidate recipe and craft
		world.getRecipeManager()
			.getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, this, world)
			.ifPresentOrElse(recipe -> {
				// Ensure matches recomputes lastOutput internally
				if (!recipe.matches(this, world)) {
					cancelSmelting();
					return;
				}
				ItemStack out = recipe.craft(this, world.getRegistryManager());
				// Put finished crucible back in the slot (setStack keeps CustomModelData=1)
				setStack(CRUCIBLE_SLOT, out);
				this.smelting = false;
				this.smeltTime = 0;
				this.smeltTimeTotal = 0;
				this.smeltingJustFinished = true;
				markDirtyAndSync();
				// Play sound when smelting is done
				world.playSound(
					null, // player
					pos,
					SoundEvents.BLOCK_ANVIL_FALL, // or another fitting sound
					SoundCategory.BLOCKS,
					1.0f,
					1.0f
				);
			}, this::cancelSmelting);
	}

	private void cancelSmelting() {
		this.smelting = false;
		this.smeltTime = 0;
		this.smeltTimeTotal = 0;
		markDirtyAndSync();
	}

	private int getStoredOreCount(ItemStack crucible) {
		if (!crucible.hasNbt()) return 0;
		return crucible.getNbt().getInt(COUNT_KEY);
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
