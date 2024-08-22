package com.sigmundgranaas.forgero.smithing.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import lombok.Getter;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class SmithingRecipe implements Recipe<SimpleInventory> {
	private final @NotNull Identifier id;
	@Getter
	private final @NotNull Ingredient ingredient;

	public SmithingRecipe(@NotNull Identifier id, @NotNull Ingredient ingredient) {
		this.id = id;
		this.ingredient = ingredient;
	}

	@Override
	public @NotNull Identifier getId() {
		return this.id;
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		return Type.INSTANCE;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public boolean matches(@NotNull SimpleInventory inventory, @NotNull World world) {
		@NotNull var smithingAnvilItemStack = inventory.getStack(0);
		if (smithingAnvilItemStack.isEmpty() || !(smithingAnvilItemStack.getItem() instanceof StateItem)) {
			return false;
		}

		return this.getIngredient().test(inventory.getStack(0));
	}

	@Override
	public @NotNull ItemStack craft(@NotNull SimpleInventory inventory, @NotNull DynamicRegistryManager registryManager) {
		// TODO: Apply modifiers based on the temperature of the tool part
		return inventory.getStack(0);
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public @NotNull ItemStack getOutput(@NotNull DynamicRegistryManager registryManager) {
		return ItemStack.EMPTY;
	}

	public static class Type implements RecipeType<SmithingRecipe> {
		public static final Type INSTANCE = new Type();
		public static final String ID = "smithing";
	}

	public static class Serializer implements RecipeSerializer<SmithingRecipe> {
		public static final MetalSmeltingRecipe.Serializer INSTANCE = new MetalSmeltingRecipe.Serializer();

		@Override
		public SmithingRecipe read(@NotNull Identifier id, @NotNull JsonObject json) {
			Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
			return new SmithingRecipe(id, ingredient);
		}

		@Override
		public SmithingRecipe read(@NotNull Identifier id, @NotNull PacketByteBuf buf) {
			@NotNull Ingredient ingredient = Ingredient.fromPacket(buf);
			return new SmithingRecipe(id, ingredient);
		}

		@Override
		public void write(@NotNull PacketByteBuf buf, @NotNull SmithingRecipe recipe) {
			buf.writeIdentifier(recipe.getId());
			recipe.getIngredient().write(buf);
		}
	}
}
