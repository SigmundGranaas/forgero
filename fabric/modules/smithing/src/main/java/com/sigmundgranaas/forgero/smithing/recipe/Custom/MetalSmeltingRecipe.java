// Java
package com.sigmundgranaas.forgero.smithing.recipe.Custom;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class MetalSmeltingRecipe extends AbstractCookingRecipe {
	// Computed output from the last successful matches() call.
	private ItemStack lastOutput = ItemStack.EMPTY;

	// Configurable behavior through JSON/network
	private final int mbPerItem;
	private final String liquidNamespace;
	private final String liquidPrefix;

	// Crucible NBT keys mirrored from CrucibleItem
	private static final String STORED_ITEM_KEY = "StoredItem";
	private static final String COUNT_KEY = "Count";
	private static final String VANILLA_ITEMS_KEY = "Items";

	public static final String ID = "metal_smelting";

	public MetalSmeltingRecipe(Identifier id, int cookingTime, int mbPerItem, String liquidNamespace, String liquidPrefix) {
		super(RecipeType.CAMPFIRE_COOKING, id, "", CookingRecipeCategory.MISC, Ingredient.EMPTY, ItemStack.EMPTY, 0.0f, cookingTime);
		this.mbPerItem = mbPerItem;
		this.liquidNamespace = liquidNamespace;
		this.liquidPrefix = liquidPrefix;
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		if (inv.size() <= 0) return false;
		ItemStack in = inv.getStack(0);
		if (!(in.getItem() instanceof CrucibleItem crucibleItem)) return false;

		NbtCompound nbt = in.getNbt();
		if (nbt == null) return false;

		String storedIdStr = nbt.getString(STORED_ITEM_KEY);
		if (storedIdStr == null || storedIdStr.isEmpty()) return false;

		int count = nbt.getInt(COUNT_KEY);
		if (count <= 0) return false;

		Identifier storedItemId;
		try {
			storedItemId = new Identifier(storedIdStr);
		} catch (Exception e) {
			return false;
		}

		Identifier targetLiquid = oreToLiquid(storedItemId);
		if (targetLiquid == null) {
			return false;
		}

		int totalAmount = count * mbPerItem;

		Identifier currentLiquid = crucibleItem.getLiquidType(in);
		if (currentLiquid != null && !currentLiquid.equals(targetLiquid)) return false;

		int remainingCapacity = crucibleItem.getRemainingLiquidCapacity(in);
		if (remainingCapacity < totalAmount) return false;

		ItemStack out = in.copy();

		NbtCompound outNbt = out.getOrCreateNbt();
		outNbt.remove(STORED_ITEM_KEY);
		outNbt.remove(VANILLA_ITEMS_KEY);
		outNbt.putInt(COUNT_KEY, 0);

		boolean added = crucibleItem.addLiquid(out, targetLiquid, totalAmount);
		if (!added) return false;

		this.lastOutput = out;
		return true;
	}

	@Override
	public ItemStack craft(Inventory inv, DynamicRegistryManager registryManager) {
		return lastOutput.copy();
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return lastOutput.isEmpty() ? ItemStack.EMPTY : lastOutput.copy();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Type implements RecipeType<MetalSmeltingRecipe> {
		public static final Type INSTANCE = new Type();
		public static final String ID = MetalSmeltingRecipe.ID;
	}

	@Override
	public RecipeType<?> getType() {
		return Type.INSTANCE;
	}

	public int getMbPerItem() {
		return mbPerItem;
	}

	public static class Serializer implements RecipeSerializer<MetalSmeltingRecipe> {
		public static final Serializer INSTANCE = new Serializer();
		// Forwarding constant so 'ModRecipes' can use 'MetalSmeltingRecipe.Serializer.ID'
		public static final String ID = MetalSmeltingRecipe.ID;

		@Override
		public MetalSmeltingRecipe read(Identifier id, JsonObject json) {
			int cookingTime = JsonHelper.getInt(json, "cooking_time", 200);
			int mbPerItem = JsonHelper.getInt(json, "mb_per_item", 100);
			String liquidNamespace = JsonHelper.getString(json, "liquid_namespace", "forgero");
			String liquidPrefix = JsonHelper.getString(json, "liquid_prefix", "molten_");

			if (mbPerItem <= 0) {
				throw new JsonSyntaxException("mb_per_item must be > 0");
			}
			if (cookingTime <= 0) {
				throw new JsonSyntaxException("cooking_time must be > 0");
			}

			return new MetalSmeltingRecipe(id, cookingTime, mbPerItem, liquidNamespace, liquidPrefix);
		}

		@Override
		public MetalSmeltingRecipe read(Identifier id, PacketByteBuf buf) {
			int cookingTime = buf.readVarInt();
			int mbPerItem = buf.readVarInt();
			String liquidNamespace = buf.readString();
			String liquidPrefix = buf.readString();
			return new MetalSmeltingRecipe(id, cookingTime, mbPerItem, liquidNamespace, liquidPrefix);
		}

		@Override
		public void write(PacketByteBuf buf, MetalSmeltingRecipe recipe) {
			buf.writeVarInt(recipe.getCookTime());
			buf.writeVarInt(recipe.mbPerItem);
			buf.writeString(recipe.liquidNamespace);
			buf.writeString(recipe.liquidPrefix);
		}
	}

	private Identifier oreToLiquid(Identifier oreItemId) {
		String path = oreItemId.getPath();
		if (!path.endsWith("_ore")) {
			return null;
		}
		String base = path;
		if (base.startsWith("deepslate_")) {
			base = base.substring("deepslate_".length());
		}
		if (!base.endsWith("_ore")) {
			return null;
		}
		String metal = base.substring(0, base.length() - "_ore".length());
		if (metal.isEmpty()) {
			return null;
		}
		return new Identifier(liquidNamespace, liquidPrefix + metal);
	}
}
