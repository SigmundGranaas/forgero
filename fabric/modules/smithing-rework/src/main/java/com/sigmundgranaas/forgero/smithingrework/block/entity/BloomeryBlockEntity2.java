package com.sigmundgranaas.forgero.smithingrework.block.entity;

import static com.sigmundgranaas.forgero.smithingrework.ForgeroSmithingInitializer.BLOOMERY_BLOCK_ENTITY;

import java.util.Optional;

import com.sigmundgranaas.forgero.smithingrework.block.custom.BloomeryBlock2;
import com.sigmundgranaas.forgero.smithingrework.item.custom.LiquidMetalCrucibleItem;
import com.sigmundgranaas.forgero.smithingrework.recipe.MetalSmeltingRecipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BloomeryBlockEntity2 extends BlockEntity implements NamedScreenHandlerFactory {
	private final BloomeryInventory inventory = new BloomeryInventory();
	private int cookTime = 0;
	private MetalSmeltingRecipe currentRecipe;
	private final PropertyDelegate propertyDelegate;


	public BloomeryBlockEntity2(BlockPos pos, BlockState state) {
		super(BLOOMERY_BLOCK_ENTITY, pos, state);
		this.propertyDelegate = new PropertyDelegate() {
			@Override
			public int get(int index) {
				if (index == 0) {
					return cookTime;
				}
				return 0;
			}

			@Override
			public void set(int index, int value) {
				if (index == 0) {
					cookTime = value;
				}
			}

			@Override
			public int size() {
				return 1;
			}
		};
	}

	public BloomeryInventory getInventory() {
		return inventory;
	}

	public static void tick(World world, BlockPos pos, BlockState state, BloomeryBlockEntity2 be) {
		if (world.isClient) return;

		boolean wasLit = state.get(BloomeryBlock2.LIT);
		boolean shouldBeLit = be.isSmelting();

		if (wasLit != shouldBeLit) {
			world.setBlockState(pos, state.with(BloomeryBlock2.LIT, shouldBeLit), Block.NOTIFY_ALL);
		}

		if (be.currentRecipe == null) {
			Optional<MetalSmeltingRecipe> recipe = world.getRecipeManager()
					.getFirstMatch(MetalSmeltingRecipe.TYPE, be.inventory, world);
			if (recipe.isPresent()) {
				be.currentRecipe = recipe.get();
				be.cookTime = 0;
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

			if (crucibleStack.getItem() instanceof LiquidMetalCrucibleItem crucibleItem && crucibleItem.canAddLiquid(ingredient, currentRecipe.getLiquid(), currentRecipe.getLiquidAmount())) {
				crucibleItem.addLiquid(crucibleStack, currentRecipe.getLiquid(), currentRecipe.getLiquidAmount());
			}

			inventory.getStack(BloomeryInventory.INGREDIENT_SLOT).decrement(1);

			this.cookTime = 0;
			this.currentRecipe = null;
		}
	}


	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory.getItems());
		nbt.putInt("CookTime", cookTime);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, inventory.getItems());
		cookTime = nbt.getInt("CookTime");
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
