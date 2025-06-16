package com.sigmundgranaas.forgero.smithing.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sigmundgranaas.forgero.smithing.item.ModItems;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SmithingRecipeManager {
	private static final Map<Identifier, SmithingRecipe> RECIPES = new HashMap<>();

	public static void registerRecipe(SmithingRecipe recipe) {
		RECIPES.put(recipe.getId(), recipe);
	}

	public static Optional<SmithingRecipe> findRecipe(ItemStack input) {
		return RECIPES.values().stream()
				.filter(recipe -> recipe.matches(input))
				.findFirst();
	}

	public static SmithingRecipe getRecipe(Identifier id) {
		return RECIPES.get(id);
	}

	public static void initializeRecipes() {
		// Example recipe: Iron Ingot -> Custom Sword Blade
		registerRecipe(new SmithingRecipe(
				new Identifier("forgero", "clay_crucible"),
				new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_arrow_head-schematic")), 1),
				new ItemStack(ModItems.CLAY_CRUCIBLE), // Your custom item
				5, // 5 hammer strikes required
				700, // Min working heat
				850, // Max working heat
				true // Requires cooling
		));

		// Add more recipes as needed
	}
}
