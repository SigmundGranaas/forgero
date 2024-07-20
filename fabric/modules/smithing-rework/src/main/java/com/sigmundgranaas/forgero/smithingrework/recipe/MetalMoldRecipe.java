package com.sigmundgranaas.forgero.smithingrework.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.smithingrework.item.custom.LiquidMetalCrucibleItem;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class MetalMoldRecipe implements Recipe<SimpleInventory> {
	public static final String ID = "metal_mold";

	private final Identifier id;
	private final Identifier liquid;
	private final int coolingTime;
	private final int liquidAmount;
	private final Ingredient mold;
	private final ItemStack result;

	public MetalMoldRecipe(Identifier id, Identifier liquid, int coolingTime, int liquidAmount, Ingredient mold, ItemStack result) {
		this.id = id;
		this.liquid = liquid;
		this.coolingTime = coolingTime;
		this.liquidAmount = liquidAmount;
		this.mold = mold;
		this.result = result;
	}

	@Override
	public boolean matches(SimpleInventory inventory, World world) {
		ItemStack crucible = inventory.getStack(1);
		return mold.test(inventory.getStack(0))
				&& crucible.getItem() instanceof LiquidMetalCrucibleItem crucibleItem
				&& crucibleItem.hasMoreOrEqualLiquid(crucible, liquidAmount)
				&& crucibleItem.getLiquidType(crucible).equals(liquid);
	}

	@Override
	public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
		return result;
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return result.copy();
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public RecipeType<?> getType() {
		return TYPE;
	}

	public static RecipeType<MetalMoldRecipe> TYPE = new RecipeType<>() {
		public String toString() {
			return "forgero:metal_mold";
		}
	};


	public Identifier getLiquid() {
		return liquid;
	}

	public int getCoolingTime() {
		return coolingTime;
	}

	public int getLiquidAmount() {
		return liquidAmount;
	}

	public static class Serializer implements RecipeSerializer<MetalMoldRecipe> {
		public static Serializer INSTANCE = new Serializer();

		@Override
		public MetalMoldRecipe read(Identifier id, JsonObject json) {
			Identifier liquid = new Identifier(JsonHelper.getString(json, "liquid"));
			int coolingTime = JsonHelper.getInt(json, "cooling_time");
			int liquidAmount = JsonHelper.getInt(json, "liquid_amount");
			Ingredient mold = Ingredient.fromJson(json.get("mold"));
			ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));

			return new MetalMoldRecipe(id, liquid, coolingTime, liquidAmount, mold, result);
		}

		@Override
		public MetalMoldRecipe read(Identifier id, PacketByteBuf buf) {
			Identifier liquid = buf.readIdentifier();
			int coolingTime = buf.readVarInt();
			int liquidAmount = buf.readVarInt();
			Ingredient mold = Ingredient.fromPacket(buf);
			ItemStack result = buf.readItemStack();

			return new MetalMoldRecipe(id, liquid, coolingTime, liquidAmount, mold, result);
		}

		@Override
		public void write(PacketByteBuf buf, MetalMoldRecipe recipe) {
			buf.writeIdentifier(recipe.liquid);
			buf.writeVarInt(recipe.coolingTime);
			buf.writeVarInt(recipe.liquidAmount);
			recipe.mold.write(buf);
			buf.writeItemStack(recipe.result);
		}
	}
}
