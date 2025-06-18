package com.sigmundgranaas.forgero.smithing.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.smithing.block.inventory.BloomeryInventory;
import com.sigmundgranaas.forgero.smithing.item.custom.LiquidMetalCrucibleItem;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class MetalSmeltingRecipe implements Recipe<BloomeryInventory> {
	private final Identifier id;
	private final Ingredient ingredient;
	private final Identifier liquid;
	private final int cookingTime;
	private final int liquidAmount;

	public MetalSmeltingRecipe(Identifier id, Ingredient ingredient, Identifier liquid, int cookingTime, int liquidAmount) {
		this.id = id;
		this.ingredient = ingredient;
		this.liquid = liquid;
		this.cookingTime = cookingTime;
		this.liquidAmount = liquidAmount;
	}

	@Override
	public boolean matches(BloomeryInventory inventory, World world) {
		ItemStack crucible = inventory.getCrucible();
		ItemStack ingredient = inventory.getIngredient();

		if (!(crucible.getItem() instanceof LiquidMetalCrucibleItem)) {
			return false;
		}

		LiquidMetalCrucibleItem crucibleItem = (LiquidMetalCrucibleItem) crucible.getItem();
		Identifier currentLiquid = crucibleItem.getLiquidType(crucible);
		boolean ingredientMatches = this.ingredient.test(ingredient);
		boolean liquidMatches = currentLiquid == null || currentLiquid.equals(this.liquid);
		boolean canAddLiquid = crucibleItem.canAddLiquid(crucible, this.liquid, this.liquidAmount);

		return ingredientMatches && liquidMatches && canAddLiquid;
	}

	@Override
	public ItemStack craft(BloomeryInventory inventory, DynamicRegistryManager registryManager) {
		ItemStack crucible = inventory.getCrucible().copy();
		LiquidMetalCrucibleItem crucibleItem = (LiquidMetalCrucibleItem) crucible.getItem();
		crucibleItem.addLiquid(crucible, this.liquid, this.liquidAmount);
		return crucible;
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return ItemStack.EMPTY;
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<MetalSmeltingRecipe> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public RecipeType<?> getType() {
		return Type.INSTANCE;
	}

	public Ingredient getIngredient() {
		return this.ingredient;
	}

	public Identifier getLiquid() {
		return this.liquid;
	}

	public int getCookingTime() {
		System.out.println("Getting cooking time: " + this.cookingTime + " for recipe " + this.id);
		return this.cookingTime;
	}

	public int getLiquidAmount() {
		return this.liquidAmount;
	}


	public static class Type implements RecipeType<MetalSmeltingRecipe> {
		public static final Type INSTANCE = new Type();
		public static final String ID = "metal_smelting";
	}

	public static class Serializer implements RecipeSerializer<MetalSmeltingRecipe> {
		public static final Serializer INSTANCE = new Serializer();
		public static final String ID = "metal_smelting";

		@Override
		public MetalSmeltingRecipe read(Identifier id, JsonObject json) {
			Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
			Identifier liquid = new Identifier(JsonHelper.getString(json, "liquid"));
			int cookingTime = JsonHelper.getInt(json, "cooking_time", 200);
			int liquidAmount = JsonHelper.getInt(json, "liquid_amount");

			System.out.println("Loading recipe " + id + " with cookingTime: " + cookingTime + ", liquid: " + liquid + ", amount: " + liquidAmount);
			return new MetalSmeltingRecipe(id, ingredient, liquid, cookingTime, liquidAmount);
		}

		@Override
		public MetalSmeltingRecipe read(Identifier id, PacketByteBuf buf) {
			Ingredient ingredient = Ingredient.fromPacket(buf);
			Identifier liquid = buf.readIdentifier();
			int cookingTime = buf.readVarInt();
			int liquidAmount = buf.readVarInt();
			return new MetalSmeltingRecipe(id, ingredient, liquid, cookingTime, liquidAmount);
		}


		@Override
		public void write(PacketByteBuf buf, MetalSmeltingRecipe recipe) {
			recipe.getIngredient().write(buf);
			buf.writeIdentifier(recipe.getLiquid());
			buf.writeVarInt(recipe.getCookingTime());
			buf.writeVarInt(recipe.getLiquidAmount());
		}
	}
}
