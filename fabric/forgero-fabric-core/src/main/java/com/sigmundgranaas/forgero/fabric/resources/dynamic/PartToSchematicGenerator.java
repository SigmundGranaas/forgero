package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeUtils;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator.CompositeRecipeOptimiser;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import net.devtech.arrp.api.RuntimeResourcePack;

import net.minecraft.util.Identifier;


/**
 * A class that generates schematic recipes.
 */
public class PartToSchematicGenerator implements DynamicResourceGenerator {
	protected final StateService service;
	protected final RecipeCreator recipeCreator;
	protected final RecipeFilter recipeFilter;

	/**
	 * Constructor.
	 *
	 * @param service       The StateService object.
	 * @param recipeCreator The RecipeCreator object.
	 * @param recipeFilter  The RecipeFilter object.
	 */
	public PartToSchematicGenerator(StateService service, RecipeCreator recipeCreator, RecipeFilter recipeFilter) {
		this.service = service;
		this.recipeCreator = recipeCreator;
		this.recipeFilter = recipeFilter;
	}

	/**
	 * Generates schematic recipes and adds them to a RuntimeResourcePack object.
	 *
	 * @param pack The RuntimeResourcePack object.
	 */
	@Override
	public void generate(RuntimeResourcePack pack) {
		var recipes = parts().stream()
				.map(recipeCreator::createRecipe)
				.flatMap(Optional::stream)
				.toList();

		var optimiser = new CompositeRecipeOptimiser();
		optimiser.process(recipes).stream()
				.map(this::convertRecipeData)
				.forEach(recipe -> pack.addData(generateId(recipe), recipe.toString().getBytes()));
	}

	/**
	 * Gets a list of parts from the StateService object.
	 *
	 * @return The list of parts.
	 */
	protected List<ConstructedState> parts() {
		return service.all().stream()
				.map(Supplier::get)
				.filter(ConstructedState.class::isInstance)
				.map(ConstructedState.class::cast)
				.filter(comp -> comp.parts().stream().anyMatch(ingredient -> ingredient.name().contains("schematic")))
				.filter(recipeFilter::shouldKeep)
				.toList();
	}

	protected JsonObject convertRecipeData(RecipeData construct) {
		var json = new JsonObject();
		json.addProperty("type", "minecraft:crafting_shapeless");
		var ingredients = new JsonArray();
		construct.ingredients().stream().map(RecipeUtils::ingredientsToJsonEntry).forEach(ingredients::add);
		json.add("ingredients", ingredients);
		var result = new JsonObject();
		result.addProperty("item", construct.target());
		json.add("result", result);
		return json;
	}

	protected Identifier generateId(JsonObject recipe) {
		String output = recipe.getAsJsonObject("result").get("item").getAsString().split(":")[1];
		return new Identifier("forgero:recipes/" + output + "_recipe" + ".json");
	}

	/**
	 * Interface for classes that can create a RecipeData object from a ConstructedState object.
	 */
	public interface RecipeCreator {
		/**
		 * Creates a recipe from a ConstructedState object.
		 *
		 * @param construct The ConstructedState object.
		 * @return The created RecipeData object, or an empty Optional if a recipe cannot be created.
		 */
		Optional<RecipeData> createRecipe(ConstructedState construct);
	}

	/**
	 * Interface for classes that can decide whether to keep a ConstructedState object based on certain conditions.
	 */
	public interface RecipeFilter {
		/**
		 * Checks whether a ConstructedState object should be kept.
		 *
		 * @param state The ConstructedState object.
		 * @return true if the object should be kept, false otherwise.
		 */
		boolean shouldKeep(ConstructedState state);
	}

	/**
	 * RecipeFilter implementation that only keeps base variants of schematic recipes.
	 */
	public static class BaseVariantFilter implements RecipeFilter {
		/**
		 * Checks whether a ConstructedState object should be kept.
		 * Only base variants of schematic recipes are kept.
		 *
		 * @param state The ConstructedState object.
		 * @return true if the object is a base variant of a schematic recipe, false otherwise.
		 */
		@Override
		public boolean shouldKeep(ConstructedState state) {
			String name = state.name().toLowerCase();
			String type = state.type().typeName().toLowerCase();

			String[] nameComponents = name.split(ELEMENT_SEPARATOR);

			// Check that name has at least two components
			if (nameComponents.length < 2) {
				return false;
			}

			// Compare the second component of the name with the type
			return nameComponents[1].equals(type) || nameComponents[1].equals("binding");
		}
	}

	/**
	 * RecipeFilter implementation that keeps all variants of schematic recipes.
	 */
	public static class AllVariantFilter implements RecipeFilter {

		/**
		 * Checks whether a ConstructedState object should be kept.
		 * All variants of schematic recipes are kept.
		 *
		 * @param state The ConstructedState object.
		 * @return true, as all objects should be kept.
		 */
		@Override
		public boolean shouldKeep(ConstructedState state) {
			return true;
		}
	}

	/**
	 * RecipeCreator implementation that creates schematic recipes.
	 */
	public static class SchematicRecipeCreator implements RecipeCreator {
		/**
		 * Creates a schematic recipe from a ConstructedState object.
		 *
		 * @param construct The ConstructedState object.
		 * @return The created RecipeData object, or an empty Optional if a recipe cannot be created.
		 */
		@Override
		public Optional<RecipeData> createRecipe(ConstructedState construct) {
			var schematic = construct.parts().stream()
					.filter(ingredient -> ingredient.name().contains("schematic"))
					.findFirst();

			if (schematic.isPresent()) {
				var paper = IngredientData.builder().id("minecraft:paper").build();
				var constructIngredient = IngredientData.builder().id(construct.identifier()).build();
				var recipe = RecipeData.builder()
						.ingredients(List.of(paper, constructIngredient))
						.target(schematic.get().identifier())
						.craftingType(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE.toString())
						.build();
				return Optional.of(recipe);
			}

			return Optional.empty();
		}
	}
}
