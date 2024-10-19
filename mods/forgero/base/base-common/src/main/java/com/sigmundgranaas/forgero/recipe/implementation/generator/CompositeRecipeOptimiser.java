package com.sigmundgranaas.forgero.recipe.implementation.generator;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.recipe.implementation.RecipeUtils.ingredientsToRecipeId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.service.StateService;

/**
 * CompositeRecipeOptimiser class is used for optimizing recipes.
 * It groups common recipes and converts their ingredients using different converters based on the recipe and ingredient types.
 */
public class CompositeRecipeOptimiser {
	private final Map<String, List<RecipeData>> targetGroups = new HashMap<>();
	private final Map<String, List<String>> idTagEntries = new HashMap<>();
	private final Map<RecipeTypes, RecipeDataConverter> recipeDataConverters = new HashMap<>();
	private final Map<RecipeTypes, IngredientDataConverter> ingredientDataConverters = new HashMap<>();

	public CompositeRecipeOptimiser() {
		// Initialize converters
		recipeDataConverters.put(RecipeTypes.STATE_CRAFTING_RECIPE, new StateCraftingRecipeDataConverter());
		recipeDataConverters.put(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE, new ToolpartSchematicRecipeDataConverter());
		ingredientDataConverters.put(RecipeTypes.STATE_CRAFTING_RECIPE, new CommonIngredientDataConverter());
		ingredientDataConverters.put(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE, new MaterialSpecificIngredientDataConverter());
	}

	/**
	 * Process method processes a collection of RecipeData resources.
	 * It groups common recipes and converts ingredients.
	 *
	 * @param resources collection of RecipeData resources
	 * @return collection of RecipeData with optimized groups and converted ingredients
	 */
	public Collection<RecipeData> process(Collection<RecipeData> resources) {
		resources.forEach(this::findCommonGroup);
		return resolveGroups();
	}

	/**
	 * findCommonGroup method groups the RecipeData resources by their target.
	 *
	 * @param data RecipeData resource
	 */
	private void findCommonGroup(RecipeData data) {
		targetGroups.computeIfAbsent(data.target(), k -> new ArrayList<>()).add(data);
	}

	/**
	 * resolveGroups method resolves the groups of recipes.
	 * It converts common recipes to tags.
	 *
	 * @return list of RecipeData with resolved groups and converted ingredients
	 */
	private List<RecipeData> resolveGroups() {
		return targetGroups.values().stream()
				.map(this::convertCommonsToTag)
				.flatMap(Collection::stream)
				.toList();
	}

	/**
	 * convertCommonsToTag method converts a list of RecipeData entries.
	 * It applies the appropriate converter based on the recipe type.
	 *
	 * @param entries list of RecipeData entries
	 * @return collection of RecipeData with converted ingredients
	 */
	private Collection<RecipeData> convertCommonsToTag(List<RecipeData> entries) {
		Map<String, RecipeData> recipeMap = new HashMap<>();
		for (RecipeData entry : entries) {
			RecipeTypes recipeType = RecipeTypes.of(entry.type());
			RecipeDataConverter recipeDataConverter = recipeDataConverters.get(recipeType);
			IngredientDataConverter ingredientDataConverter = ingredientDataConverters.get(recipeType);
			if (recipeDataConverter != null && ingredientDataConverter != null) {
				List<IngredientData> convertedIngredients = entry.ingredients().stream()
						.map(ingredientDataConverter::convert)
						.toList();
				RecipeData newRecipe = recipeDataConverter.convert(entry, convertedIngredients);
				recipeMap.put(ingredientsToRecipeId(convertedIngredients), newRecipe);
			} else {
				recipeMap.put(ingredientsToRecipeId(entry.ingredients()), entry);
			}
		}
		return recipeMap.values();
	}

	/**
	 * placeEntryIntoTagMap method places an entry into the tag map.
	 *
	 * @param tag a string representing the tag
	 * @param id  a string representing the id
	 */
	private void placeEntryIntoTagMap(String tag, String id) {
		idTagEntries.computeIfAbsent(tag, k -> new ArrayList<>()).add(id);
	}

	/**
	 * RecipeDataConverter interface is used for converting RecipeData.
	 */
	private IngredientData convertIngredientData(IngredientData data, Function<IngredientData, Optional<String>> converter) {
		Optional<String> converted = converter.apply(data);
		if (converted.isPresent()) {
			placeEntryIntoTagMap(converted.get(), data.id());
			return data.toBuilder().type(converted.get()).id(EMPTY_IDENTIFIER).build();
		} else {
			return data;
		}
	}

	interface RecipeDataConverter {
		RecipeData convert(RecipeData original, List<IngredientData> convertedIngredients);
	}

	/**
	 * IngredientDataConverter interface is used for converting IngredientData.
	 */
	interface IngredientDataConverter {
		IngredientData convert(IngredientData original);
	}

	/**
	 * StateCraftingRecipeDataConverter class implements the RecipeDataConverter interface for the state crafting recipe type.
	 */
	static class StateCraftingRecipeDataConverter implements RecipeDataConverter {
		@Override
		public RecipeData convert(RecipeData original, List<IngredientData> convertedIngredients) {
			return original.toBuilder().ingredients(convertedIngredients).build();
		}
	}

	/**
	 * ToolpartSchematicRecipeDataConverter class implements the RecipeDataConverter interface for the toolpart schematic recipe type.
	 */
	static class ToolpartSchematicRecipeDataConverter implements RecipeDataConverter {
		@Override
		public RecipeData convert(RecipeData original, List<IngredientData> convertedIngredients) {
			return original.toBuilder().ingredients(convertedIngredients).build();
		}
	}

	/**
	 * CommonIngredientDataConverter class implements the IngredientDataConverter interface for common ingredient data.
	 */
	class CommonIngredientDataConverter implements IngredientDataConverter {
		@Override
		public IngredientData convert(IngredientData original) {
			return convertIngredientData(original, this::materialTypeTag);
		}

		private Optional<String> materialTypeTag(IngredientData data) {
			if (data.id().equals(EMPTY_IDENTIFIER) || !data.type().equals(EMPTY_IDENTIFIER)) {
				return Optional.empty();
			}
			var elements = data.id().split(ELEMENT_SEPARATOR);
			Optional<String> type = StateService.INSTANCE.find(data.id()).map(State::type).map(Type::typeName).map(name -> name.toLowerCase(Locale.ENGLISH));
			if (elements.length > 1 && type.isPresent()) {
				return Optional.of(String.format("%s-%s", elements[0].split(":")[1], type.get()));
			}
			return Optional.empty();
		}
	}


	/**
	 * MaterialSpecificIngredientDataConverter class implements the IngredientDataConverter interface for material specific ingredient data.
	 */
	class MaterialSpecificIngredientDataConverter implements IngredientDataConverter {
		@Override
		public IngredientData convert(IngredientData original) {
			return convertIngredientData(original, this::removeMaterialFromId);
		}

		private Optional<String> removeMaterialFromId(IngredientData data) {
			if (!data.id().equals(EMPTY_IDENTIFIER)) {
				var elements = data.id().split(ELEMENT_SEPARATOR);
				if (elements.length > 1) {
					return Optional.of(elements[1]);
				}
			}
			return Optional.empty();
		}
	}
}
