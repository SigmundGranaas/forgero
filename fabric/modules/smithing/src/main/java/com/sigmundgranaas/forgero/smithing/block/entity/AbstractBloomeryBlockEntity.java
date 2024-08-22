package com.sigmundgranaas.forgero.smithing.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sigmundgranaas.forgero.smithing.block.custom.AbstractBloomeryBlock;
import com.sigmundgranaas.forgero.smithing.block.inventory.BloomeryInventory;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBloomeryBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeInputProvider, RecipeUnlocker
	{
		protected static final int INPUT_SLOT_INDEX = 0;
		protected static final int FUEL_SLOT_INDEX = 1;
		protected static final int OUTPUT_SLOT_INDEX = 2;
		public static final int BURN_TIME_PROPERTY_INDEX = 0;
		private static final int[] TOP_SLOTS = new int[]{0};
		private static final int[] BOTTOM_SLOTS = new int[]{2, 1};
		private static final int[] SIDE_SLOTS = new int[]{1};
		public static final int FUEL_TIME_PROPERTY_INDEX = 1;
		public static final int COOK_TIME_PROPERTY_INDEX = 2;
		public static final int COOK_TIME_TOTAL_PROPERTY_INDEX = 3;
		public static final int PROPERTY_COUNT = 4;
		public static final int DEFAULT_COOK_TIME = 200;
		public static final int field_31295 = 2;
		private final BloomeryInventory inventory = new BloomeryInventory();
		int burnTime;
		int fuelTime;
		int cookTime;
		int cookTimeTotal;
		protected final PropertyDelegate propertyDelegate = new PropertyDelegate(){

			@Override
			public int get(int index) {
				switch (index) {
					case 0: {
						return AbstractBloomeryBlockEntity.this.burnTime;
					}
					case 1: {
						return AbstractBloomeryBlockEntity.this.fuelTime;
					}
					case 2: {
						return AbstractBloomeryBlockEntity.this.cookTime;
					}
					case 3: {
						return AbstractBloomeryBlockEntity.this.cookTimeTotal;
					}
				}
				return 0;
			}

			@Override
			public void set(int index, int value) {
				switch (index) {
					case 0: {
						AbstractBloomeryBlockEntity.this.burnTime = value;
						break;
					}
					case 1: {
						AbstractBloomeryBlockEntity.this.fuelTime = value;
						break;
					}
					case 2: {
						AbstractBloomeryBlockEntity.this.cookTime = value;
						break;
					}
					case 3: {
						AbstractBloomeryBlockEntity.this.cookTimeTotal = value;
						break;
					}
				}
			}

			@Override
			public int size() {
				return 4;
			}
		};
		private final Object2IntOpenHashMap<Identifier> recipesUsed = new Object2IntOpenHashMap();
		private final RecipeManager.MatchGetter<Inventory, ? extends AbstractCookingRecipe> matchGetter;

		protected AbstractBloomeryBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType) {
			super(blockEntityType, pos, state);
			this.matchGetter = RecipeManager.createCachedMatchGetter(recipeType);
		}

		public static Map<Item, Integer> createFuelTimeMap() {
			LinkedHashMap<Item, Integer> map = Maps.newLinkedHashMap();
			AbstractBloomeryBlockEntity.addFuel(map, Items.LAVA_BUCKET, 20000);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.COAL_BLOCK, 16000);
			AbstractBloomeryBlockEntity.addFuel(map, Items.BLAZE_ROD, 2400);
			AbstractBloomeryBlockEntity.addFuel(map, Items.COAL, 1600);
			AbstractBloomeryBlockEntity.addFuel(map, Items.CHARCOAL, 1600);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.LOGS, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.BAMBOO_BLOCKS, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.PLANKS, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.BAMBOO_MOSAIC, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOODEN_STAIRS, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOODEN_SLABS, 150);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.BAMBOO_MOSAIC_SLAB, 150);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOODEN_TRAPDOORS, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOODEN_FENCES, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.FENCE_GATES, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.NOTE_BLOCK, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.BOOKSHELF, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.CHISELED_BOOKSHELF, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.LECTERN, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.JUKEBOX, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.CHEST, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.TRAPPED_CHEST, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.CRAFTING_TABLE, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.DAYLIGHT_DETECTOR, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.BANNERS, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Items.BOW, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Items.FISHING_ROD, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.LADDER, 300);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.SIGNS, 200);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.HANGING_SIGNS, 800);
			AbstractBloomeryBlockEntity.addFuel(map, Items.WOODEN_SHOVEL, 200);
			AbstractBloomeryBlockEntity.addFuel(map, Items.WOODEN_SWORD, 200);
			AbstractBloomeryBlockEntity.addFuel(map, Items.WOODEN_HOE, 200);
			AbstractBloomeryBlockEntity.addFuel(map, Items.WOODEN_AXE, 200);
			AbstractBloomeryBlockEntity.addFuel(map, Items.WOODEN_PICKAXE, 200);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOODEN_DOORS, 200);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.BOATS, 1200);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOOL, 100);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOODEN_BUTTONS, 100);
			AbstractBloomeryBlockEntity.addFuel(map, Items.STICK, 100);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.SAPLINGS, 100);
			AbstractBloomeryBlockEntity.addFuel(map, Items.BOWL, 100);
			AbstractBloomeryBlockEntity.addFuel(map, ItemTags.WOOL_CARPETS, 67);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.DRIED_KELP_BLOCK, 4001);
			AbstractBloomeryBlockEntity.addFuel(map, Items.CROSSBOW, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.BAMBOO, 50);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.DEAD_BUSH, 100);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.SCAFFOLDING, 50);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.LOOM, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.BARREL, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.CARTOGRAPHY_TABLE, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.FLETCHING_TABLE, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.SMITHING_TABLE, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.COMPOSTER, 300);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.AZALEA, 100);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.FLOWERING_AZALEA, 100);
			AbstractBloomeryBlockEntity.addFuel(map, Blocks.MANGROVE_ROOTS, 300);
			return map;
		}

		/**
		 * {@return whether the provided {@code item} is in the {@link
		 * net.minecraft.registry.tag.ItemTags#NON_FLAMMABLE_WOOD non_flammable_wood} tag}
		 */
		private static boolean isNonFlammableWood(Item item) {
			return item.getRegistryEntry().isIn(ItemTags.NON_FLAMMABLE_WOOD);
		}

		private static void addFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime) {
			for (RegistryEntry<Item> registryEntry : Registries.ITEM.iterateEntries(tag)) {
				if (AbstractBloomeryBlockEntity.isNonFlammableWood(registryEntry.value())) continue;
				fuelTimes.put(registryEntry.value(), fuelTime);
			}
		}

		private static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible item, int fuelTime) {
			Item item2 = item.asItem();
			if (AbstractBloomeryBlockEntity.isNonFlammableWood(item2)) {
				if (SharedConstants.isDevelopment) {
					throw Util.throwOrPause(new IllegalStateException("A developer tried to explicitly make fire resistant item " + item2.getName(null).getString() + " a furnace fuel. That will not work!"));
				}
				return;
			}
			fuelTimes.put(item2, fuelTime);
		}

		private boolean isBurning() {
			return this.burnTime > 0;
		}

		@Override
		public void readNbt(NbtCompound nbt) {
			super.readNbt(nbt);
			Inventories.readNbt(nbt, inventory.getItems());
			this.burnTime = nbt.getShort("BurnTime");
			this.cookTime = nbt.getShort("CookTime");
			this.cookTimeTotal = nbt.getShort("CookTimeTotal");
			this.fuelTime = this.getFuelTime(this.inventory.getFuel());
			NbtCompound nbtCompound = nbt.getCompound("RecipesUsed");
			for (String string : nbtCompound.getKeys()) {
				this.recipesUsed.put(new Identifier(string), nbtCompound.getInt(string));
			}
		}

		@Override
		protected void writeNbt(NbtCompound nbt) {
			super.writeNbt(nbt);
			nbt.putShort("BurnTime", (short)this.burnTime);
			nbt.putShort("CookTime", (short)this.cookTime);
			nbt.putShort("CookTimeTotal", (short)this.cookTimeTotal);
			Inventories.writeNbt(nbt, inventory.getItems());
			NbtCompound nbtCompound = new NbtCompound();
			this.recipesUsed.forEach((identifier, count) -> nbtCompound.putInt(identifier.toString(), (int)count));
			nbt.put("RecipesUsed", nbtCompound);
		}

		public static void tick(World world, BlockPos pos, BlockState state, AbstractBloomeryBlockEntity blockEntity) {
			boolean bl4;
			boolean bl = blockEntity.isBurning();
			boolean bl2 = false;
			if (blockEntity.isBurning()) {
				--blockEntity.burnTime;
			}
			ItemStack itemStack = blockEntity.inventory.getFuel();
			boolean bl3 = !blockEntity.inventory.getIngredient().isEmpty();
			boolean bl5 = bl4 = !itemStack.isEmpty();
			if (blockEntity.isBurning() || bl4 && bl3) {
				Recipe recipe = bl3 ? (Recipe)blockEntity.matchGetter.getFirstMatch(blockEntity, world).orElse(null) : null;
				int i = blockEntity.getMaxCountPerStack();
				if (!blockEntity.isBurning() && AbstractBloomeryBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), recipe, blockEntity.inventory.getItems(), i)) {
					blockEntity.fuelTime = blockEntity.burnTime = blockEntity.getFuelTime(itemStack);
					if (blockEntity.isBurning()) {
						bl2 = true;
						if (bl4) {
							Item item = itemStack.getItem();
							itemStack.decrement(1);
							if (itemStack.isEmpty()) {
								Item item2 = item.getRecipeRemainder();
								blockEntity.inventory.setStack(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
							}
						}
					}
				}
				if (blockEntity.isBurning() && AbstractBloomeryBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), recipe, blockEntity.inventory.getItems(), i)) {
					++blockEntity.cookTime;
					if (blockEntity.cookTime == blockEntity.cookTimeTotal) {
						blockEntity.cookTime = 0;
						blockEntity.cookTimeTotal = AbstractBloomeryBlockEntity.getCookTime(world, blockEntity);
						if (AbstractBloomeryBlockEntity.craftRecipe(world.getRegistryManager(), recipe, blockEntity.inventory.getItems(), i)) {
							blockEntity.setLastRecipe(recipe);
						}
						bl2 = true;
					}
				} else {
					blockEntity.cookTime = 0;
				}
			} else if (!blockEntity.isBurning() && blockEntity.cookTime > 0) {
				blockEntity.cookTime = MathHelper.clamp(blockEntity.cookTime - 2, 0, blockEntity.cookTimeTotal);
			}
			if (bl != blockEntity.isBurning()) {
				bl2 = true;
				state = (BlockState)state.with(AbstractBloomeryBlock.LIT, blockEntity.isBurning());
				world.setBlockState(pos, state, Block.NOTIFY_ALL);
			}
			if (bl2) {
				AbstractBloomeryBlockEntity.markDirty(world, pos, state);
			}
		}

		private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @org.jetbrains.annotations.Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count) {
			if (slots.get(0).isEmpty() || recipe == null) {
				return false;
			}
			ItemStack itemStack = recipe.getOutput(registryManager);
			if (itemStack.isEmpty()) {
				return false;
			}
			ItemStack itemStack2 = slots.get(2);
			if (itemStack2.isEmpty()) {
				return true;
			}
			if (!ItemStack.areItemsEqual(itemStack2, itemStack)) {
				return false;
			}
			if (itemStack2.getCount() < count && itemStack2.getCount() < itemStack2.getMaxCount()) {
				return true;
			}
			return itemStack2.getCount() < itemStack.getMaxCount();
		}

		private static boolean craftRecipe(DynamicRegistryManager registryManager, @org.jetbrains.annotations.Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count) {
			if (recipe == null || !AbstractBloomeryBlockEntity.canAcceptRecipeOutput(registryManager, recipe, slots, count)) {
				return false;
			}
			ItemStack itemStack = slots.get(0);
			ItemStack itemStack2 = recipe.getOutput(registryManager);
			ItemStack itemStack3 = slots.get(2);
			if (itemStack3.isEmpty()) {
				slots.set(2, itemStack2.copy());
			} else if (itemStack3.isOf(itemStack2.getItem())) {
				itemStack3.increment(1);
			}
			if (itemStack.isOf(Blocks.WET_SPONGE.asItem()) && !slots.get(1).isEmpty() && slots.get(1).isOf(Items.BUCKET)) {
				slots.set(1, new ItemStack(Items.WATER_BUCKET));
			}
			itemStack.decrement(1);
			return true;
		}

		protected int getFuelTime(ItemStack fuel) {
			if (fuel.isEmpty()) {
				return 0;
			}
			Item item = fuel.getItem();
			return AbstractBloomeryBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
		}

		private static int getCookTime(World world, AbstractBloomeryBlockEntity furnace) {
			return furnace.matchGetter.getFirstMatch(furnace, world).map(AbstractCookingRecipe::getCookTime).orElse(200);
		}

		public static boolean canUseAsFuel(ItemStack stack) {
			return AbstractBloomeryBlockEntity.createFuelTimeMap().containsKey(stack.getItem());
		}

		@Override
		public int[] getAvailableSlots(Direction side) {
			if (side == Direction.DOWN) {
				return BOTTOM_SLOTS;
			}
			if (side == Direction.UP) {
				return TOP_SLOTS;
			}
			return SIDE_SLOTS;
		}

		@Override
		public boolean canInsert(int slot, ItemStack stack, @org.jetbrains.annotations.Nullable Direction dir) {
			return this.isValid(slot, stack);
		}

		@Override
		public boolean canExtract(int slot, ItemStack stack, Direction dir) {
			if (dir == Direction.DOWN && slot == 1) {
				return stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET);
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack itemStack : this.inventory.getItems()) {
				if (itemStack.isEmpty()) {
					continue;
				}

				return false;
			}
			return true;
		}

		@Override
		public ItemStack getStack(int slot) {
			return this.inventory.getStack(slot);
		}

		@Override
		public ItemStack removeStack(int slot, int amount) {
			return Inventories.splitStack(this.inventory.getItems(), slot, amount);
		}

		@Override
		public ItemStack removeStack(int slot) {
			return Inventories.removeStack(this.inventory.getItems(), slot);
		}

		@Override
		public void setStack(int slot, ItemStack stack) {
			ItemStack itemStack = this.inventory.getStack(slot);
			boolean bl = !stack.isEmpty() && ItemStack.canCombine(itemStack, stack);
			this.inventory.setStack(slot, stack);
			if (stack.getCount() > this.getMaxCountPerStack()) {
				stack.setCount(this.getMaxCountPerStack());
			}
			if (slot == 0 && !bl) {
				this.cookTimeTotal = AbstractBloomeryBlockEntity.getCookTime(this.world, this);
				this.cookTime = 0;
				this.markDirty();
			}
		}

		@Override
		public boolean canPlayerUse(PlayerEntity player) {
			return Inventory.canPlayerUse(this, player);
		}

		@Override
		public boolean isValid(int slot, ItemStack stack) {
			if (slot == 2) {
				return false;
			}
			if (slot == 1) {
				ItemStack itemStack = this.inventory.getFuel();
				return AbstractBloomeryBlockEntity.canUseAsFuel(stack) || stack.isOf(Items.BUCKET) && !itemStack.isOf(Items.BUCKET);
			}
			return true;
		}

		@Override
		public void clear() {
			this.inventory.clear();
		}

		@Override
		public void setLastRecipe(@org.jetbrains.annotations.Nullable Recipe<?> recipe) {
			if (recipe != null) {
				Identifier identifier = recipe.getId();
				this.recipesUsed.addTo(identifier, 1);
			}
		}

		@Override
		@Nullable
		public Recipe<?> getLastRecipe() {
			return null;
		}

		@Override
		public void unlockLastRecipe(PlayerEntity player, List<ItemStack> ingredients) {
		}

		public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
			List<Recipe<?>> list = this.getRecipesUsedAndDropExperience(player.getServerWorld(), player.getPos());
			player.unlockRecipes(list);
			for (Recipe<?> recipe : list) {
				if (recipe == null) continue;
				player.onRecipeCrafted(recipe, this.inventory.getItems());
			}
			this.recipesUsed.clear();
		}

		public List<Recipe<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
			ArrayList<Recipe<?>> list = Lists.newArrayList();
			for (Object2IntMap.Entry entry : this.recipesUsed.object2IntEntrySet()) {
				world.getRecipeManager().get((Identifier)entry.getKey()).ifPresent(recipe -> {
					list.add((Recipe<?>)recipe);
					AbstractBloomeryBlockEntity.dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe)recipe).getExperience());
				});
			}
			return list;
		}

		private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
			int i = MathHelper.floor((float)multiplier * experience);
			float f = MathHelper.fractionalPart((float)multiplier * experience);
			if (f != 0.0f && Math.random() < (double)f) {
				++i;
			}
			ExperienceOrbEntity.spawn(world, pos, i);
		}

		@Override
		public void provideRecipeInputs(RecipeMatcher finder) {
			for (ItemStack itemStack : this.inventory.getItems()) {
				finder.addInput(itemStack);
			}
		}
	}

