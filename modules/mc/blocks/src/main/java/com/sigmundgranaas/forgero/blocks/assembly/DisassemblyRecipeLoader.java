package com.sigmundgranaas.forgero.blocks.assembly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Loads JSON-based disassembly recipes for non-Forgero items.
 * <p>
 * Recipe files are located in data/&#42;/disassembly/&#42;.json and define how vanilla
 * or modded items can be broken down into parts.
 * <p>
 * Example recipe format:
 * <pre>{@code
 * {
 *   "input": "minecraft:diamond_pickaxe",
 *   "results": [
 *     "minecraft:diamond",
 *     "minecraft:diamond",
 *     "minecraft:diamond",
 *     "minecraft:stick",
 *     "minecraft:stick"
 *   ]
 * }
 * }</pre>
 */
public class DisassemblyRecipeLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisassemblyRecipeLoader.class);
	private static List<DisassemblyRecipe> recipes = new ArrayList<>();

	/**
	 * Gets all loaded disassembly recipes.
	 */
	public static List<DisassemblyRecipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

	/**
	 * Reloads disassembly recipes from the resource manager.
	 * <p>
	 * This is called during data pack reload.
	 */
	public static void reload(ResourceManager manager) {
		Gson gson = new Gson();
		List<DisassemblyRecipe> loadedRecipes = new ArrayList<>();

		for (Resource resource : manager.findResources("disassembly", path -> path.getPath().endsWith(".json")).values()) {
			try (InputStream stream = resource.getInputStream()) {
				JsonReader reader = new JsonReader(new InputStreamReader(stream));
				DisassemblyData data = gson.fromJson(reader, DisassemblyData.class);

				DisassemblyRecipe.fromData(data).ifPresent(loadedRecipes::add);
			} catch (Exception e) {
				LOGGER.error("Failed to load disassembly recipe: {}", e.getMessage());
			}
		}

		recipes = loadedRecipes;
	}

	/**
	 * Clears all loaded recipes.
	 * <p>
	 * Useful for testing.
	 */
	public static void clear() {
		recipes.clear();
	}

	/**
	 * JSON data structure for disassembly recipes.
	 */
	public static class DisassemblyData {
		public String input;
		public List<String> results;
	}

	/**
	 * A disassembly recipe that breaks down an item into parts.
	 */
	public static class DisassemblyRecipe {
		private final Ingredient input;
		private final List<Item> results;

		public DisassemblyRecipe(Ingredient input, List<Item> results) {
			this.input = input;
			this.results = results;
		}

		/**
		 * Creates a recipe from JSON data.
		 */
		public static Optional<DisassemblyRecipe> fromData(DisassemblyData data) {
			if (data.input == null || data.results == null || data.results.isEmpty()) {
				return Optional.empty();
			}

			// Parse input item
			Optional<Item> inputItem = parseItem(data.input);
			if (inputItem.isEmpty()) {
				return Optional.empty();
			}

			// Parse result items
			List<Item> resultItems = new ArrayList<>();
			for (String resultId : data.results) {
				Optional<Item> item = parseItem(resultId);
				if (item.isEmpty()) {
					LOGGER.warn("Invalid item in disassembly recipe results: {}", resultId);
					return Optional.empty();
				}
				resultItems.add(item.get());
			}

			if (resultItems.isEmpty()) {
				return Optional.empty();
			}

			Ingredient ingredient = Ingredient.ofItems(inputItem.get());
			return Optional.of(new DisassemblyRecipe(ingredient, resultItems));
		}

		/**
		 * Parses an item identifier string.
		 */
		private static Optional<Item> parseItem(String itemId) {
			try {
				Identifier id = new Identifier(itemId);
				if (Registries.ITEM.containsId(id)) {
					return Optional.of(Registries.ITEM.get(id));
				}
			} catch (Exception e) {
				// Invalid identifier format
			}
			return Optional.empty();
		}

		/**
		 * Gets the input ingredient.
		 */
		public Ingredient getInput() {
			return input;
		}

		/**
		 * Gets the result items.
		 */
		public List<Item> getResults() {
			return Collections.unmodifiableList(results);
		}
	}
}
