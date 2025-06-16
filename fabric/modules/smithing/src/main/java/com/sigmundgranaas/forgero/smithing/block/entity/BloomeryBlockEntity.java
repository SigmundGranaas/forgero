package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock;
import com.sigmundgranaas.forgero.smithing.block.inventory.ImplementedInventory;
import com.sigmundgranaas.forgero.smithing.item.custom.LiquidMetalCrucibleItem;
import com.sigmundgranaas.forgero.smithing.screen.BloomeryScreenHandler;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BloomeryBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);

	private int progress = 0;
	private int maxProgress = 200; // 10 seconds at 20 ticks per second
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
		return switch (slot) {
			case 0 -> isCrucible(stack); // Crucible slot
			case 1 -> isOre(stack);      // Ore slot
			case 2 -> isFuel(stack);     // Fuel slot
			case 3 -> false;             // Output slot - no direct insertion
			default -> false;
		};
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return slot == 3; // Only allow extraction from output slot automatically
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, BloomeryBlockEntity blockEntity) {
        boolean wasLit = state.get(BloomeryBlock.LIT);
        boolean dirty = false;

        // Handle fuel consumption
        if (blockEntity.isBurning()) {
            blockEntity.fuelTime--;
            dirty = true;
        }

        // Check if we can process
        if (blockEntity.hasRecipe()) {
            // Start burning new fuel if needed
            if (!blockEntity.isBurning()) {
                if (blockEntity.hasFuel()) {
                    blockEntity.burnFuel();
                    dirty = true;
                }
            }

            // Process recipe if burning
            if (blockEntity.isBurning()) {
                blockEntity.progress++;
                dirty = true;

                if (blockEntity.progress >= blockEntity.maxProgress) {
                    blockEntity.craftItem();
                    blockEntity.progress = 0;
                    dirty = true;
                }
            }
        } else if (blockEntity.progress > 0) {
            // Reset progress if recipe is invalid
            blockEntity.progress = 0;
            dirty = true;
        }

        // Update block state if lighting changed
        boolean isLit = blockEntity.isBurning();
        if (wasLit != isLit) {
            state = state.with(BloomeryBlock.LIT, isLit);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
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
		ItemStack crucible = inventory.get(0);
		ItemStack ore = inventory.get(1);
		ItemStack result = getRecipeResult(crucible, ore);
		return !crucible.isEmpty() && !ore.isEmpty() && !result.isEmpty();
	}

	public boolean hasRecipe() {
		ItemStack crucible = inventory.get(0);
		ItemStack ore = inventory.get(1);
		ItemStack output = inventory.get(3);

		if (crucible.isEmpty() || ore.isEmpty()) {
			return false;
		}

		// Check if crucible and ore are valid
		if (!isCrucible(crucible) || !isOre(ore)) {
			return false;
		}

		// Get the recipe result
		ItemStack result = getRecipeResult(crucible, ore);
		if (result.isEmpty()) {
			return false;
		}

		// Check if output slot can accept the result
		if (output.isEmpty()) {
			return true;
		}

		// For liquid crucibles, check if they can be merged
		if (output.getItem() instanceof LiquidMetalCrucibleItem &&
				result.getItem() instanceof LiquidMetalCrucibleItem) {
			LiquidMetalCrucibleItem outputCrucible = (LiquidMetalCrucibleItem) output.getItem();
			LiquidMetalCrucibleItem resultCrucible = (LiquidMetalCrucibleItem) result.getItem();

			Identifier resultLiquidType = resultCrucible.getLiquidType(result);
			int resultLiquidAmount = resultCrucible.getLiquidAmount(result);

			return resultLiquidType != null && outputCrucible.canAddLiquid(output, resultLiquidType, resultLiquidAmount);
		}

		return output.getItem() == result.getItem() &&
				output.getCount() + result.getCount() <= output.getMaxCount();
	}

	private ItemStack getRecipeResult(ItemStack crucible, ItemStack ore) {
		// Only process if crucible is a LiquidMetalCrucibleItem
		if (!(crucible.getItem() instanceof LiquidMetalCrucibleItem crucibleItem)) {
			return ItemStack.EMPTY;
		}

		// Define liquid metal recipes based on ore type
		Identifier liquidType = null;
		int liquidAmount = 0;

		if (ore.getItem() == Items.IRON_ORE || ore.getItem() == Items.DEEPSLATE_IRON_ORE) {
			liquidType = new Identifier("forgero", "molten_iron");
			liquidAmount = 100;
		} else if (ore.getItem() == Items.GOLD_ORE || ore.getItem() == Items.DEEPSLATE_GOLD_ORE) {
			liquidType = new Identifier("forgero", "molten_gold");
			liquidAmount = 100;
		} else if (ore.getItem() == Items.COPPER_ORE || ore.getItem() == Items.DEEPSLATE_COPPER_ORE) {
			liquidType = new Identifier("forgero", "molten_copper");
			liquidAmount = 100;
		}
		// Add more ore types as needed

		if (liquidType != null) {
			// Create a copy of the crucible to modify
			ItemStack resultCrucible = crucible.copy();

			// Check if we can add the liquid to the crucible
			if (crucibleItem.canAddLiquid(resultCrucible, liquidType, liquidAmount)) {
				crucibleItem.addLiquid(resultCrucible, liquidType, liquidAmount);
				return resultCrucible;
			}
		}

		return ItemStack.EMPTY;
	}

	private void craftItem() {
		ItemStack crucible = inventory.get(0);
		ItemStack ore = inventory.get(1);

		if (crucible.isEmpty() || ore.isEmpty()) return;

		if (!(crucible.getItem() instanceof LiquidMetalCrucibleItem crucibleItem)) return;

		Identifier liquidType = null;
		int liquidAmount = 0;

		if (ore.getItem() == Items.IRON_ORE || ore.getItem() == Items.DEEPSLATE_IRON_ORE) {
			liquidType = new Identifier("forgero", "molten_iron");
			liquidAmount = 100;
		} else if (ore.getItem() == Items.GOLD_ORE || ore.getItem() == Items.DEEPSLATE_GOLD_ORE) {
			liquidType = new Identifier("forgero", "molten_gold");
			liquidAmount = 100;
		} else if (ore.getItem() == Items.COPPER_ORE || ore.getItem() == Items.DEEPSLATE_COPPER_ORE) {
			liquidType = new Identifier("forgero", "molten_copper");
			liquidAmount = 100;
		}

		if (liquidType == null) return;

		// Check if crucible can accept liquid
		if (crucibleItem.canAddLiquid(crucible, liquidType, liquidAmount)) {
			// Add liquid directly to input crucible (update the input stack)
			crucibleItem.addLiquid(crucible, liquidType, liquidAmount);

			// Consume ore
			ore.decrement(1);

			// If crucible is now full (can't add more), move to output
			if (!crucibleItem.canAddLiquid(crucible, liquidType, liquidAmount)) {
				ItemStack output = inventory.get(3);
				if (output.isEmpty()) {
					inventory.set(3, crucible.copy());
					inventory.set(0, ItemStack.EMPTY);
				} else if (output.getItem() instanceof LiquidMetalCrucibleItem) {
					LiquidMetalCrucibleItem outputCrucible = (LiquidMetalCrucibleItem) output.getItem();
					Identifier outputLiquidType = outputCrucible.getLiquidType(output);
					int outputLiquidAmount = outputCrucible.getLiquidAmount(output);

					if (outputLiquidType.equals(liquidType) &&
							outputCrucible.canAddLiquid(output, liquidType, liquidAmount)) {
						outputCrucible.addLiquid(output, liquidType, liquidAmount);
						inventory.set(0, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	private boolean isBurning() {
		return fuelTime > 0;
	}

	private void burnFuel() {
		ItemStack fuelStack = inventory.get(2);  // Changed from 1 to 2 to match the fuel slot
		if (!fuelStack.isEmpty()) {
			this.maxFuelTime = this.getFuelTime(fuelStack);
			this.fuelTime = this.maxFuelTime;
			fuelStack.decrement(1);
			this.markDirty();
		}
	}

	private boolean hasFuel() {
		return !inventory.get(2).isEmpty() && this.getFuelTime(inventory.get(2)) > 0;  // Changed from 1 to 2 to match the fuel slot
	}

	private boolean isCrucible(ItemStack stack) {
		return stack.getItem() instanceof LiquidMetalCrucibleItem;
	}

	private boolean isOre(ItemStack stack) {
		return stack.getItem() == Items.IRON_ORE ||
				stack.getItem() == Items.GOLD_ORE ||
				stack.getItem() == Items.COPPER_ORE ||
				stack.getItem() == Items.DEEPSLATE_IRON_ORE ||
				stack.getItem() == Items.DEEPSLATE_GOLD_ORE ||
				stack.getItem() == Items.DEEPSLATE_COPPER_ORE ||
				stack.isIn(ConventionalItemTags.ORES);
	}

	private boolean isFuel(ItemStack stack) {
		return stack.getItem() == Items.COAL ||
				stack.getItem() == Items.CHARCOAL ||
				stack.getItem() == Items.LAVA_BUCKET ||
				stack.getItem() == Items.BLAZE_ROD;
	}

	private int getFuelTime(ItemStack fuel) {
		if (fuel.isEmpty()) return 0;
		if (fuel.isOf(Items.COAL)) return 1600; // 80 seconds
		if (fuel.isOf(Items.CHARCOAL)) return 1600;
		if (fuel.isIn(ConventionalItemTags.COAL)) return 1600;
		return 0;
	}

	public ItemStack insertItem(ItemStack stack) {
		for (int i = 0; i < 3; i++) { // Only insert into input slots
			if (canInsert(i, stack, null)) {
				ItemStack existing = inventory.get(i);
				if (existing.isEmpty()) {
					inventory.set(i, stack);
					markDirty();
					return ItemStack.EMPTY;
				} else if (existing.getItem() == stack.getItem() &&
						existing.getCount() < existing.getMaxCount()) {
					int canInsert = existing.getMaxCount() - existing.getCount();
					int toInsert = Math.min(canInsert, stack.getCount());
					existing.increment(toInsert);
					stack.decrement(toInsert);
					markDirty();
					return stack.isEmpty() ? ItemStack.EMPTY : stack;
				}
			}
		}
		return stack;
	}

	public ItemStack extractItem() {
		// Extract from output slot first
		ItemStack output = inventory.get(3);
		if (!output.isEmpty()) {
			ItemStack extracted = output.copy();
			inventory.set(3, ItemStack.EMPTY);
			markDirty();
			return extracted;
		}

		// Then try other slots
		for (int i = 0; i < 3; i++) {
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
                case 2 -> Math.max(0, fuelTime);  // Ensure non-negative
                case 3 -> Math.max(1, maxFuelTime);  // Ensure non-zero for division
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
