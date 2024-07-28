package com.sigmundgranaas.forgero.smithingrework.block.entity;

import static com.sigmundgranaas.forgero.smithingrework.ForgeroSmithingInitializer.BLOOMERY_BLOCK_ENTITY;

import java.util.Optional;

import com.sigmundgranaas.forgero.smithingrework.block.custom.BloomeryBlock;
import com.sigmundgranaas.forgero.smithingrework.item.custom.LiquidMetalCrucibleItem;
import com.sigmundgranaas.forgero.smithingrework.recipe.MetalSmeltingRecipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BloomeryBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
	private final BloomeryInventory inventory = new BloomeryInventory();
	int burnTime;
	int fuelTime;
	int cookTime;
	int cookTimeTotal;
	private MetalSmeltingRecipe currentRecipe;
	private final PropertyDelegate propertyDelegate;



	public BloomeryBlockEntity(BlockPos pos, BlockState state) {
		super(BLOOMERY_BLOCK_ENTITY, pos, state);
		this.propertyDelegate = new PropertyDelegate() {
			@Override
			public int get(int index) {
				switch (index) {
					case 0: {
						return BloomeryBlockEntity.this.burnTime;
					}
					case 1: {
						return BloomeryBlockEntity.this.fuelTime;
					}
					case 2: {
						return BloomeryBlockEntity.this.cookTime;
					}
					case 3: {
						return BloomeryBlockEntity.this.cookTimeTotal;
					}
				}
				return 0;
			}

			@Override
			public void set(int index, int value) {
				switch (index) {
					case 0: {
						BloomeryBlockEntity.this.burnTime = value;
						break;
					}
					case 1: {
						BloomeryBlockEntity.this.fuelTime = value;
						break;
					}
					case 2: {
						BloomeryBlockEntity.this.cookTime = value;
						break;
					}
					case 3: {
						BloomeryBlockEntity.this.cookTimeTotal = value;
						break;
					}
				}
			}

			@Override
			public int size() {
				return 4;
			}
		};
	}

	public BloomeryInventory getInventory() {
		return inventory;
	}

	public static void tick(World world, BlockPos pos, BlockState state, BloomeryBlockEntity be) {
		if (world.isClient) return;

		boolean wasLit = state.get(BloomeryBlock.LIT);
		boolean shouldBeLit = be.isSmelting();
		ItemStack itemStack = be.inventory.getStack(2);

		if (wasLit != shouldBeLit) {
			world.setBlockState(pos, state.with(BloomeryBlock.LIT, shouldBeLit), Block.NOTIFY_ALL);

		}

		if (be.isSmelting()) {
			--be.burnTime;
		}

		if (be.currentRecipe == null) {
			Optional<MetalSmeltingRecipe> recipe = world.getRecipeManager()
					.getFirstMatch(MetalSmeltingRecipe.TYPE, be.inventory, world);
			if (recipe.isPresent()) {
				be.currentRecipe = recipe.get();
				be.cookTime = 0;
				be.fuelTime = be.burnTime = be.getFuelTime(itemStack);
			}
		}

		if (be.currentRecipe != null && be.canSmelt()) {
			be.cookTime++;

			if (be.cookTime >= be.currentRecipe.getCookingTime()) {
				be.craftItem();
			}
		} else {
			be.cookTime = 0;
		}

		be.markDirty();
	}

	private boolean isSmelting() {
		return this.currentRecipe != null && this.canSmelt();
	}

	private boolean canSmelt() {
		if (this.currentRecipe == null) return false;
		ItemStack inputStack = inventory.getStack(BloomeryInventory.INGREDIENT_SLOT);

		return !inputStack.isEmpty() &&
				this.currentRecipe.matches(this.inventory, this.world);
	}

	private void craftItem() {
		if (this.currentRecipe != null && this.canSmelt()) {
			ItemStack ingredient = inventory.getStack(BloomeryInventory.INGREDIENT_SLOT);
			ItemStack crucibleStack = inventory.getStack(BloomeryInventory.CRUCIBLE_SLOT);
			ItemStack fuelStack = inventory.getStack(BloomeryInventory.FUEL_SLOT);

			if (crucibleStack.getItem() instanceof LiquidMetalCrucibleItem crucibleItem && crucibleItem.canAddLiquid(ingredient, currentRecipe.getLiquid(), currentRecipe.getLiquidAmount())) {
				crucibleItem.addLiquid(crucibleStack, currentRecipe.getLiquid(), currentRecipe.getLiquidAmount());
			}

			inventory.getStack(BloomeryInventory.INGREDIENT_SLOT).decrement(1);

			this.cookTime = 0;
			this.currentRecipe = null;
		}
	}

	public boolean isValid(int slot, ItemStack stack) {
		if (slot == 2) {
			return false;
		}
		if (slot == 1) {
			ItemStack itemStack = this.inventory.getStack(2);
			return AbstractFurnaceBlockEntity.canUseAsFuel(stack) || stack.isOf(Items.BUCKET) && !itemStack.isOf(Items.BUCKET);
		}
		return true;
	}


	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory.getItems());
		nbt.putInt("CookTime", cookTime);
		nbt.putInt("BurnTime", burnTime);
		nbt.putInt("CookTimeTime", cookTimeTotal);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory.getItems());
		cookTime = nbt.getInt("CookTime");
		burnTime = nbt.getInt("BurnTime");
		cookTimeTotal = nbt.getInt("CookTimeTotal");
		this.fuelTime = this.getFuelTime(this.inventory.getStack(2));
	}

	protected int getFuelTime(ItemStack fuel) {
		if (fuel.isEmpty()) {
			return 0;
		}
		Item item = fuel.getItem();
		return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
	}


	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new BloomeryScreenHandler(syncId, playerInventory, this.inventory, this.propertyDelegate);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("block.forgero.bloomery");
	}
}
