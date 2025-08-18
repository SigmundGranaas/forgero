package com.sigmundgranaas.forgero.smithing.block.entity;


import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.block.inventory.BloomeryInventory;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.item.custom.LiquidMetalCrucibleItem;
import com.sigmundgranaas.forgero.smithing.recipe.MetalSmeltingRecipe;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class BloomeryExtensionBlockEntity extends BlockEntity {
	// I am assuming you have a class for your packet IDs, if not, replace with your ID
	public static final Identifier SYNC_PACKET_ID = new Identifier("forgero", "bloomery_extension_sync");
	private static final int INVENTORY_SIZE = 3; // 1 tool slot + 1 crucible slot + 1 ore slot
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

	// Slot indices
	public static final int TOOL_SLOT = 0;
	public static final int CRUCIBLE_SLOT = 1;
	public static final int ORE_SLOT = 2;

	// Temperature system constants
	private static final int HEAT_PER_TICK = 10; // Heating rate when bloomery is lit
	private static final int COOL_PER_TICK = 1; // Cooling rate when bloomery is not lit
	private static final int TICK_INTERVAL = 2; // Update every 10 ticks (twice as fast as regular cooling)
	private int tickCounter = 0;

	// Smelting progress
	public int smeltingProgress = 0;
	public int maxSmeltingTime = 200; // Default smelting time

	public BloomeryExtensionBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BLOOMERY_EXTENSION, pos, state);
	}

	public DefaultedList<ItemStack> getInventory() {
		return inventory;
	}

	public ItemStack getStack(int slot) {
		// Only allow slot 0
		if (slot >= 0 && slot < INVENTORY_SIZE) {
			return inventory.get(slot);
		}
		return ItemStack.EMPTY;
	}

	public void setStack(int slot, ItemStack stack) {
		if (slot >= 0 && slot < INVENTORY_SIZE) {
			inventory.set(slot, stack);
			markDirty();
		}
	}

	public boolean isEmpty() {
		for (ItemStack stack : inventory) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public int getInventorySize() {
		return INVENTORY_SIZE;
	}

	/**
	 * Tries to add an item to the first available slot
	 * @param stack The item to add
	 * @return The remaining stack (empty if fully added)
	 */
	public ItemStack addItem(ItemStack stack) {
		for (int i = 0; i < INVENTORY_SIZE; i++) {
			if (inventory.get(i).isEmpty()) {
				inventory.set(i, stack);
				markDirty();
				return ItemStack.EMPTY;
			}
		}
		return stack;
	}

	/**
	 * Finds the first non-empty slot and removes its item
	 * @return The removed item or empty stack if no items
	 */
	public ItemStack removeFirstItem() {
		for (int i = 0; i < INVENTORY_SIZE; i++) {
			ItemStack stack = inventory.get(i);
			if (!stack.isEmpty()) {
				inventory.set(i, ItemStack.EMPTY);
				// Remove CustomModelData tag if crucible is removed
				if (i == CRUCIBLE_SLOT && stack.hasNbt()) {
					stack.removeSubNbt("CustomModelData");
				}
				markDirty();
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory);
		smeltingProgress = nbt.getInt("smeltingProgress");
		maxSmeltingTime = nbt.getInt("maxSmeltingTime");
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory);
		nbt.putInt("smeltingProgress", smeltingProgress);
		nbt.putInt("maxSmeltingTime", maxSmeltingTime);
	}



	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound nbt = new NbtCompound();
		writeNbt(nbt);
		return nbt;
	}

	// Client synchronization methods for real-time renderer updates
	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}


	public NbtCompound toSyncedNbt() {
		return createNbt();
	}

	/**
	 * Forces client synchronization when inventory changes
	 */
	private void syncToClient() {
		if (world != null && !world.isClient) {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBlockPos(pos);
			buf.writeInt(inventory.size());
			for (ItemStack itemStack : inventory) {
				buf.writeItemStack(itemStack);
			}

			for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
				ServerPlayNetworking.send(player, SYNC_PACKET_ID, buf);
			}
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isClient) {
			syncToClient();
		}
	}

	// Static server tick method for heating/cooling tools and smelting
	public static void serverTick(World world, BlockPos pos, BlockState state, BloomeryExtensionBlockEntity entity) {
		entity.tickCounter++;
		if (entity.tickCounter % TICK_INTERVAL != 0) {
			return;
		}

		// Get actual temperature from connected bloomery fuel system
		int bloomeryTemperature = entity.getCurrentTemperature();
		boolean isLit = entity.isAdjacentBloomeryLit(world, pos);
		boolean inventoryChanged = false;

		// Process tool heating/cooling
		ItemStack toolStack = entity.getStack(TOOL_SLOT);
		if (!toolStack.isEmpty() && toolStack.getItem() instanceof StateItem stateItem) {
			var type = stateItem.dynamicState(toolStack).type();
			if (ToolPartTypeUtils.isToolPartType(type)) {
				int currentTemp = TemperatureUtils.getTemperature(toolStack);
				int newTemp = currentTemp;

				if (isLit) {
					// When bloomery is lit, determine max temperature based on fuel system
					int maxAllowedTemp;
					if (bloomeryTemperature > 0) {
						// Use actual fuel system temperature if available (includes bellows boost)
						maxAllowedTemp = bloomeryTemperature;
					} else {
						// Fallback: check if connected bloomery can reach high temps (charcoal) or limited (coal)
						if (entity.canReachTemperature(900)) {
							maxAllowedTemp = 1000; // Charcoal available
						} else {
							maxAllowedTemp = 800;  // Only coal available
						}
					}

					int targetTemp = Math.min(maxAllowedTemp, TemperatureUtils.getMaxTemp(toolStack));

					// Always heat toward the target temperature when lit
					if (currentTemp < targetTemp) {
						newTemp = Math.min(targetTemp, currentTemp + HEAT_PER_TICK);
					}
					// Don't cool down when bloomery is active - let it maintain higher temps with bellows
				} else {
					// Cool the tool when bloomery is not lit
					newTemp = Math.max(TemperatureUtils.DEFAULT_TEMPERATURE, currentTemp - COOL_PER_TICK);
				}

				if (newTemp != currentTemp) {
					TemperatureUtils.setTemperature(toolStack, newTemp);
					inventoryChanged = true;
				}
			}
		}

		// Process crucible smelting
		if (isLit && entity.hasValidSmeltingRecipe()) {
			entity.smeltingProgress++;
			if (entity.smeltingProgress >= entity.maxSmeltingTime) {
				entity.processSmeltingRecipe();
				entity.smeltingProgress = 0;
				inventoryChanged = true;
			}
		} else if (entity.smeltingProgress > 0) {
			// Reset progress if recipe is no longer valid or bloomery is not lit
			entity.smeltingProgress = 0;
			inventoryChanged = true;
		}

		if (inventoryChanged) {
			entity.markDirty();
		}
	}

	/**
	 * Checks if there's a valid smelting recipe with current inventory
	 */
	private boolean hasValidSmeltingRecipe() {
		ItemStack crucible = getStack(CRUCIBLE_SLOT);
		ItemStack ore = getStack(ORE_SLOT);

		if (crucible.isEmpty() || ore.isEmpty()) {
			return false;
		}

		if (!(crucible.getItem() instanceof LiquidMetalCrucibleItem)) {
			return false;
		}

		// Create a dummy BloomeryInventory for recipe matching
		BloomeryInventory recipeInventory = new BloomeryInventory();
		recipeInventory.setStack(BloomeryInventory.CRUCIBLE_SLOT, crucible.copy());
		recipeInventory.setStack(BloomeryInventory.INGREDIENT_SLOT, ore.copy());

		var recipeOpt = world.getRecipeManager().getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, recipeInventory, world);
		if (recipeOpt.isPresent()) {
			maxSmeltingTime = recipeOpt.get().getCookingTime();
			return true;
		}
		return false;
	}

	/**
	 * Processes the smelting recipe and updates inventory
	 */
	private void processSmeltingRecipe() {
		ItemStack crucible = getStack(CRUCIBLE_SLOT);
		ItemStack ore = getStack(ORE_SLOT);

		if (crucible.isEmpty() || ore.isEmpty() || !(crucible.getItem() instanceof LiquidMetalCrucibleItem)) {
			return;
		}

		// Create a dummy BloomeryInventory for recipe matching
		BloomeryInventory recipeInventory = new BloomeryInventory();
		recipeInventory.setStack(BloomeryInventory.CRUCIBLE_SLOT, crucible.copy());
		recipeInventory.setStack(BloomeryInventory.INGREDIENT_SLOT, ore.copy());

		var recipeOpt = world.getRecipeManager().getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, recipeInventory, world);
		if (recipeOpt.isPresent()) {
			MetalSmeltingRecipe recipe = recipeOpt.get();
			LiquidMetalCrucibleItem crucibleItem = (LiquidMetalCrucibleItem) crucible.getItem();

			// Add liquid to crucible
			crucibleItem.addLiquid(crucible, recipe.getLiquid(), recipe.getLiquidAmount());

			// Consume ore
			ore.decrement(1);
			if (ore.isEmpty()) {
				setStack(ORE_SLOT, ItemStack.EMPTY);
			} else {
				setStack(ORE_SLOT, ore);
			}

			// Update crucible
			setStack(CRUCIBLE_SLOT, crucible);
		}
	}

	/**
	 * Creates a crucible ItemStack with CustomModelData = 1
	 */
	public ItemStack createCustomCrucibleStack() {
		ItemStack crucible = new ItemStack(ModItems.CRUCIBLE);
		crucible.getOrCreateNbt().putInt("CustomModelData", 1);
		return crucible;
	}

	/**
	 * Checks if there's an adjacent lit bloomery block
	 */
	private boolean isAdjacentBloomeryLit(World world, BlockPos pos) {
		// Check all 6 directions for a lit bloomery block
		for (net.minecraft.util.math.Direction direction : net.minecraft.util.math.Direction.values()) {
			BlockPos adjacentPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(adjacentPos);

			// Check if it's a BloomeryBlock and if it's lit
			if (adjacentState.getBlock() instanceof com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock bloomeryBlock) {
				// Double check that the state actually has the LIT property before accessing it
				if (adjacentState.contains(com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock.LIT)) {
					if (adjacentState.get(com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock.LIT)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Gets the current temperature from the connected main bloomery
	 */
	public int getCurrentTemperature() {
		if (world == null) return 0;

		// Get the extension's facing direction to find the connected bloomery
		BlockState extensionState = getCachedState();
		if (!extensionState.contains(com.sigmundgranaas.forgero.smithing.block.custom.BloomeryExtensionBlock.FACING)) {
			return 0;
		}

		net.minecraft.util.math.Direction extensionFacing = extensionState.get(com.sigmundgranaas.forgero.smithing.block.custom.BloomeryExtensionBlock.FACING);
		// Since the extension is to the LEFT of the bloomery, the bloomery is to the RIGHT of the extension
		// So we rotate clockwise from the extension's facing to find the bloomery
		net.minecraft.util.math.Direction bloomeryDirection = extensionFacing.rotateYClockwise();
		BlockPos bloomeryPos = pos.offset(bloomeryDirection);
		BlockEntity bloomeryEntity = world.getBlockEntity(bloomeryPos);

		if (bloomeryEntity instanceof BloomeryBlockEntity bloomery) {
			return bloomery.getCurrentTemperature();
		}

		return 0; // No connected bloomery or no temperature
	}

	/**
	 * Checks if the connected bloomery can reach the target temperature
	 */
	public boolean canReachTemperature(int targetTemp) {
		if (world == null) return false;

		// Get the extension's facing direction to find the connected bloomery
		BlockState extensionState = getCachedState();
		if (!extensionState.contains(com.sigmundgranaas.forgero.smithing.block.custom.BloomeryExtensionBlock.FACING)) {
			return false;
		}

		net.minecraft.util.math.Direction extensionFacing = extensionState.get(com.sigmundgranaas.forgero.smithing.block.custom.BloomeryExtensionBlock.FACING);
		// Since the extension is to the LEFT of the bloomery, the bloomery is to the RIGHT of the extension
		// So we rotate clockwise from the extension's facing to find the bloomery
		net.minecraft.util.math.Direction bloomeryDirection = extensionFacing.rotateYClockwise();
		BlockPos bloomeryPos = pos.offset(bloomeryDirection);
		BlockEntity bloomeryEntity = world.getBlockEntity(bloomeryPos);

		if (bloomeryEntity instanceof BloomeryBlockEntity bloomery) {
			return bloomery.canReachTemperature(targetTemp);
		}

		return false;
	}
}
