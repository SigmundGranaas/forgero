package com.sigmundgranaas.forgero.common.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * Schematic-based crafting recipe.
 *
 * Simplified implementation that delegates to vanilla shapeless recipes.
 * The original implementation had special logic for schematic items,
 * but this version treats them as regular shapeless recipes.
 */
public class SchematicPartRecipe extends ShapelessRecipe {
	public static final Identifier ID = new Identifier("forgero", "schematic_part_crafting");
	public static final Serializer SERIALIZER = new Serializer();

	public SchematicPartRecipe(
			Identifier id,
			String group,
			CraftingRecipeCategory category,
			ItemStack output,
			DefaultedList<Ingredient> input
	) {
		super(id, group, category, output, input);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(RecipeInputInventory inventory, World world) {
		return super.matches(inventory, world);
	}

	@Override
	public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
		return super.craft(inventory, registryManager);
	}

	public static class Serializer implements RecipeSerializer<SchematicPartRecipe> {
		private final RecipeSerializer<ShapelessRecipe> delegate = RecipeSerializer.SHAPELESS;

		@Override
		public SchematicPartRecipe read(Identifier id, JsonObject json) {
			ShapelessRecipe base = delegate.read(id, json);
			return new SchematicPartRecipe(
					id,
					base.getGroup(),
					base.getCategory(),
					base.getOutput(null),
					base.getIngredients()
			);
		}

		@Override
		public SchematicPartRecipe read(Identifier id, PacketByteBuf buf) {
			ShapelessRecipe base = delegate.read(id, buf);
			return new SchematicPartRecipe(
					id,
					base.getGroup(),
					base.getCategory(),
					base.getOutput(null),
					base.getIngredients()
			);
		}

		@Override
		public void write(PacketByteBuf buf, SchematicPartRecipe recipe) {
			delegate.write(buf, recipe);
		}
	}
}
