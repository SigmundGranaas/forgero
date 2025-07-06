package com.sigmundgranaas.forgero.smithing.block.entity;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock;
import com.sigmundgranaas.forgero.smithing.block.inventory.BloomeryInventory;
import com.sigmundgranaas.forgero.smithing.block.inventory.ImplementedInventory;
import com.sigmundgranaas.forgero.smithing.item.custom.LiquidMetalCrucibleItem;
import com.sigmundgranaas.forgero.smithing.recipe.MetalSmeltingRecipe;
import com.sigmundgranaas.forgero.smithing.screen.BloomeryScreenHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;

public class BloomeryBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(BloomeryInventory.INVENTORY_SIZE, ItemStack.EMPTY);

	private int progress = 0;
	private int maxProgress = 200; // Default value, will be overridden by recipe

	// Adjust fuel times to match vanilla furnace
	private static final int COAL_FUEL_TIME = 1600; // 80 seconds
	private static final int CHARCOAL_FUEL_TIME = 1600; // 80 seconds
	private int fuelTime = 0;
	private int maxFuelTime = 0;

	public BloomeryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BLOOMERY, pos, state);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return inventory;
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		if (slot == BloomeryInventory.CRUCIBLE_SLOT) {
			return isCrucible(stack);
		} else if (slot == BloomeryInventory.INGREDIENT_SLOT) {
			return isOre(stack);
		} else if (slot == BloomeryInventory.FUEL_SLOT) {
			return isFuel(stack);
		} else if (slot == BloomeryInventory.OUTPUT_SLOT) {
			// Don't allow inserting into the output slot directly
			return false;
		}
		return false;
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		// Only allow extraction from the output slot
		return slot == BloomeryInventory.OUTPUT_SLOT;
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, BloomeryBlockEntity blockEntity) {
        boolean wasLit = state.get(BloomeryBlock.LIT);
        boolean dirty = false;
        boolean hasValidCrucible = !blockEntity.inventory.get(BloomeryInventory.CRUCIBLE_SLOT).isEmpty();
        boolean hasValidRecipe = blockEntity.hasRecipe();
        boolean canSmelt = hasValidCrucible && hasValidRecipe;
        
        // Handle fuel consumption (independent of smelting)
        if (blockEntity.isBurning()) {
            blockEntity.fuelTime--;
            if (blockEntity.fuelTime <= 0) {
                blockEntity.fuelTime = 0;
                blockEntity.maxFuelTime = 0;
                dirty = true;
            }
        }
        
        // Try to start burning if we have fuel and can smelt something
        if (!blockEntity.isBurning() && blockEntity.hasFuel() && canSmelt) {
            blockEntity.burnFuel();
            dirty = true;
        }
        
        // Update block state if lighting changed
        boolean isLit = blockEntity.isBurning();
        if (wasLit != isLit) {
            state = state.with(BloomeryBlock.LIT, isLit);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            dirty = true;
        }
        
        // Handle smelting progress (only if burning and can smelt)
        if (blockEntity.isBurning() && canSmelt) {
            blockEntity.progress++;
            if (blockEntity.progress >= blockEntity.maxProgress) {
                blockEntity.craftItem();
                blockEntity.progress = 0; // Reset progress after crafting
                dirty = true;
            }
        } else if (blockEntity.progress > 0) {
            // Reset progress if we can't smelt or not burning
            // Reset progress if recipe is invalid
            blockEntity.progress = 0;
            dirty = true;
        }

        // Mark dirty and sync to client if needed
        if (dirty) {
            blockEntity.markDirty();
            // Ensure client receives updates by marking the chunk dirty
            world.markDirty(pos);
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
        }
    }

	private boolean canSmelt() {
		ItemStack crucible = inventory.get(BloomeryInventory.CRUCIBLE_SLOT);
		ItemStack input = inventory.get(BloomeryInventory.INGREDIENT_SLOT);
		
		if (crucible.isEmpty() || input.isEmpty()) {
			return false;
		}
		
		World world = getWorld();
		if (world == null) return false;
		
		// Create a dummy inventory for recipe matching
		BloomeryInventory recipeInventory = new BloomeryInventory();
		recipeInventory.setStack(BloomeryInventory.CRUCIBLE_SLOT, crucible.copy());
		recipeInventory.setStack(BloomeryInventory.INGREDIENT_SLOT, input.copy());
		
		// Check if there's a matching recipe
		return world.getRecipeManager()
			.getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, recipeInventory, world)
			.isPresent();
	}

	public boolean hasRecipe() {
		ItemStack crucible = inventory.get(BloomeryInventory.CRUCIBLE_SLOT);
		ItemStack input = inventory.get(BloomeryInventory.INGREDIENT_SLOT);
		ItemStack output = inventory.get(BloomeryInventory.OUTPUT_SLOT);

		if (crucible.isEmpty() || input.isEmpty()) {
			return false;
		}

		// Check if crucible is valid
		if (!isCrucible(crucible)) {
			return false;
		}

		// Try to find a matching recipe
		World world = getWorld();
		if (world == null) {
			return false;
		}
		
		// Get the recipe and update maxProgress
		BloomeryInventory recipeInventory = new BloomeryInventory();
		recipeInventory.setStack(BloomeryInventory.CRUCIBLE_SLOT, crucible.copy());
		recipeInventory.setStack(BloomeryInventory.INGREDIENT_SLOT, input.copy());
		
		return world.getRecipeManager()
			.getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, recipeInventory, world)
			.map(recipe -> {
				// Update maxProgress based on the recipe's cooking time
				int oldMax = this.maxProgress;
				this.maxProgress = recipe.getCookingTime();
				// Check if we can output the result
				if (output.isEmpty()) {
					return true;
				} else if (output.getItem() instanceof LiquidMetalCrucibleItem crucibleItem) {
					return crucibleItem.canAddLiquid(output, recipe.getLiquid(), recipe.getLiquidAmount());
				}
				return false;
			})
			.orElse(false);
	}

	@Nullable
	private ItemStack getRecipeResult(ItemStack crucible, ItemStack input) {
		// Only process if crucible is a LiquidMetalCrucibleItem
		if (!(crucible.getItem() instanceof LiquidMetalCrucibleItem)) {
			return ItemStack.EMPTY;
		}

		World world = getWorld();
		if (world == null) return ItemStack.EMPTY;

		// Create a dummy inventory for recipe matching
		BloomeryInventory recipeInventory = new BloomeryInventory();
		recipeInventory.setStack(BloomeryInventory.CRUCIBLE_SLOT, crucible.copy());
		recipeInventory.setStack(BloomeryInventory.INGREDIENT_SLOT, input.copy());

		// Find and execute the recipe
		return world.getRecipeManager()
			.getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, recipeInventory, world)
			.map(recipe -> {
				ItemStack resultCrucible = crucible.copy();
				if (resultCrucible.getItem() instanceof LiquidMetalCrucibleItem crucibleItem) {
					crucibleItem.addLiquid(resultCrucible, recipe.getLiquid(), recipe.getLiquidAmount());
					return resultCrucible;
				}
				return ItemStack.EMPTY;
			})
			.orElse(ItemStack.EMPTY);
	}

	private void craftItem() {
		ItemStack crucible = inventory.get(BloomeryInventory.CRUCIBLE_SLOT);
		ItemStack input = inventory.get(BloomeryInventory.INGREDIENT_SLOT);
		ItemStack output = inventory.get(BloomeryInventory.OUTPUT_SLOT);

		if (crucible.isEmpty() || input.isEmpty()) {
			return;
		}

		World world = getWorld();
		if (world == null) {
			return;
		}

		// Create a dummy inventory for recipe matching
		BloomeryInventory recipeInventory = new BloomeryInventory();
		recipeInventory.setStack(BloomeryInventory.CRUCIBLE_SLOT, crucible.copy());
		recipeInventory.setStack(BloomeryInventory.INGREDIENT_SLOT, input.copy());

		// Find and execute the recipe
		var recipeOpt = world.getRecipeManager().getFirstMatch(MetalSmeltingRecipe.Type.INSTANCE, recipeInventory, world);
		if (recipeOpt.isEmpty()) {
			return;
		}
		
		var recipe = recipeOpt.get();
		
		// Create a copy of the crucible to modify
		ItemStack resultCrucible = crucible.copy();
		if (resultCrucible.getItem() instanceof LiquidMetalCrucibleItem crucibleItem) {
			// Consume the input
			input.decrement(1);
			inventory.set(BloomeryInventory.INGREDIENT_SLOT, input);

			// Add the liquid to the crucible
			crucibleItem.addLiquid(resultCrucible, recipe.getLiquid(), recipe.getLiquidAmount());
			inventory.set(BloomeryInventory.CRUCIBLE_SLOT, resultCrucible);

			// Check if we can add more liquid to the crucible
			boolean canAddMore = crucibleItem.canAddLiquid(resultCrucible, recipe.getLiquid(), recipe.getLiquidAmount());
			boolean isFull = crucibleItem.getLiquidAmount(resultCrucible) >= crucibleItem.getMaxCapacity();

			if (output.isEmpty()) {
				if (!canAddMore || isFull) {
					// If we can't add more liquid or crucible is full, move to output slot
					inventory.set(BloomeryInventory.OUTPUT_SLOT, resultCrucible.copy());
					inventory.set(BloomeryInventory.CRUCIBLE_SLOT, ItemStack.EMPTY);
				} else {
					// Otherwise keep it in the crucible slot
					inventory.set(BloomeryInventory.CRUCIBLE_SLOT, resultCrucible);
				}
			} else if (output.getItem() instanceof LiquidMetalCrucibleItem outputCrucibleItem) {
				// If output is a crucible, try to add the liquid to it
				if (outputCrucibleItem.canAddLiquid(output, recipe.getLiquid(), recipe.getLiquidAmount())) {
					outputCrucibleItem.addLiquid(output, recipe.getLiquid(), recipe.getLiquidAmount());
				}
			}
		}
	}

	private boolean isBurning() {
		return fuelTime > 0;
	}

	private void burnFuel() {
        ItemStack fuelStack = inventory.get(BloomeryInventory.FUEL_SLOT);
        if (!fuelStack.isEmpty()) {
            // Get the fuel time for this item
            int newFuelTime = this.getFuelTime(fuelStack);
            
            // Vanilla behavior: when adding fuel while already burning, add (fuelTime / 2) ticks
            if (this.fuelTime > 0) {
                this.fuelTime += newFuelTime / 2;
            } else {
                this.fuelTime = newFuelTime;
            }
            this.maxFuelTime = Math.max(this.maxFuelTime, this.fuelTime);
            
            // Consume the fuel item (if not in creative mode)
            if (!fuelStack.isIn(ItemTags.NON_FLAMMABLE_WOOD)) {
                fuelStack.decrement(1);
                if (fuelStack.isEmpty()) {
                    Item remainder = fuelStack.getItem().getRecipeRemainder();
                    inventory.set(BloomeryInventory.FUEL_SLOT, remainder != null ? new ItemStack(remainder) : ItemStack.EMPTY);
                } else {
                    inventory.set(BloomeryInventory.FUEL_SLOT, fuelStack);
                }
            }
            this.markDirty();
        }
    }

	private boolean hasFuel() {
		return !inventory.get(BloomeryInventory.FUEL_SLOT).isEmpty() && this.getFuelTime(inventory.get(BloomeryInventory.FUEL_SLOT)) > 0;
	}

	private boolean isCrucible(ItemStack stack) {
		return stack.getItem() instanceof LiquidMetalCrucibleItem;
	}

	private boolean isOre(ItemStack stack) {
		// Check if the item is in the ores tag
		return stack.isIn(ConventionalItemTags.ORES);
	}

	private boolean isFuel(ItemStack stack) {
		return getFuelTime(stack) > 0;
	}


	private int getFuelTime(ItemStack fuel) {
		if (fuel.isEmpty()) {
			return 0;
		}
		Integer fuelTime = FuelRegistry.INSTANCE.get(fuel.getItem());
		if (fuelTime != null && fuelTime > 0) {
			return fuelTime;
		}
		if (fuel.isOf(Items.BLAZE_ROD)) {
			return 2400; // 120 seconds
		}
		return 0;
	}

	public ItemStack insertItem(ItemStack stack) {
		// Try to insert into the crucible slot first
		if (canInsert(BloomeryInventory.CRUCIBLE_SLOT, stack, null)) {
			ItemStack existing = inventory.get(BloomeryInventory.CRUCIBLE_SLOT);
			if (existing.isEmpty()) {
				inventory.set(BloomeryInventory.CRUCIBLE_SLOT, stack.copy());
				markDirty();
				return ItemStack.EMPTY;
			} else if (ItemStack.canCombine(existing, stack) && existing.getCount() < existing.getMaxCount()) {
				int canInsert = existing.getMaxCount() - existing.getCount();
				int toInsert = Math.min(canInsert, stack.getCount());
				existing.increment(toInsert);
				stack.decrement(toInsert);
				markDirty();
				return stack.isEmpty() ? ItemStack.EMPTY : stack;
			}
		}

		// Then try the ingredient slot
		if (canInsert(BloomeryInventory.INGREDIENT_SLOT, stack, null)) {
			ItemStack existing = inventory.get(BloomeryInventory.INGREDIENT_SLOT);
			if (existing.isEmpty()) {
				inventory.set(BloomeryInventory.INGREDIENT_SLOT, stack.copy());
				markDirty();
				return ItemStack.EMPTY;
			} else if (ItemStack.canCombine(existing, stack) && existing.getCount() < existing.getMaxCount()) {
				int canInsert = existing.getMaxCount() - existing.getCount();
				int toInsert = Math.min(canInsert, stack.getCount());
				existing.increment(toInsert);
				stack.decrement(toInsert);
				markDirty();
				return stack.isEmpty() ? ItemStack.EMPTY : stack;
			}
		}

		// Then try the fuel slot
		if (canInsert(BloomeryInventory.FUEL_SLOT, stack, null)) {
			ItemStack existing = inventory.get(BloomeryInventory.FUEL_SLOT);
			if (existing.isEmpty()) {
				inventory.set(BloomeryInventory.FUEL_SLOT, stack.copy());
				markDirty();
				return ItemStack.EMPTY;
			} else if (ItemStack.canCombine(existing, stack) && existing.getCount() < existing.getMaxCount()) {
				int canInsert = existing.getMaxCount() - existing.getCount();
				int toInsert = Math.min(canInsert, stack.getCount());
				existing.increment(toInsert);
				stack.decrement(toInsert);
				markDirty();
				return stack.isEmpty() ? ItemStack.EMPTY : stack;
			}
		}

		// Don't allow inserting into the output slot directly

		return stack;
	}

	public ItemStack extractItem() {
		// Try to extract from output slot first
		ItemStack output = inventory.get(BloomeryInventory.OUTPUT_SLOT);
		if (!output.isEmpty()) {
			ItemStack extracted = output.copy();
			inventory.set(BloomeryInventory.OUTPUT_SLOT, ItemStack.EMPTY);
			markDirty();
			return extracted;
		}

		// Then try other slots for extraction
		for (int i = 0; i < inventory.size(); i++) {
			// Skip output slot (already checked)
			if (i == BloomeryInventory.OUTPUT_SLOT) continue;

			ItemStack stack = inventory.get(i);
			if (!stack.isEmpty()) {
				ItemStack extracted = stack.copy();
				inventory.set(i, ItemStack.EMPTY);
				markDirty();
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}

	public void dropContents(World world, BlockPos pos) {
		for (ItemStack stack : inventory) {
			if (!stack.isEmpty()) {
				ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}
	}

	// Getter methods for renderer
	public int getProgress() {
		return progress;
	}

	public int getMaxProgress() {
		return maxProgress;
	}

	public int getFuelTime() {
		return fuelTime;
	}

	public int getMaxFuelTime() {
		return maxFuelTime;
	}

	public DefaultedList<ItemStack> getInventory() {
		return inventory;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory);
		progress = nbt.getInt("progress");
		fuelTime = nbt.getInt("fuelTime");
		maxFuelTime = nbt.getInt("maxFuelTime");
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory);
		nbt.putInt("progress", progress);
		nbt.putInt("fuelTime", fuelTime);
		nbt.putInt("maxFuelTime", maxFuelTime);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound nbt = new NbtCompound();
		writeNbt(nbt);
		return nbt;
	}

    // Move propertyDelegate definition before createMenu
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> fuelTime;
                case 3 -> maxFuelTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, Math.min(value, maxProgress));
                case 1 -> maxProgress = Math.max(1, value);
                case 2 -> fuelTime = Math.max(0, value);
                case 3 -> maxFuelTime = Math.max(1, value);
            }
            markDirty();
        }

        @Override
        public int size() {
            return 4;
        }
    };

	@Override
	public Text getDisplayName() {
		return Text.translatable("block.forgero.bloomery");
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BloomeryScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }
}
