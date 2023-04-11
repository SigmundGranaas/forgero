package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;

import java.util.*;
import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeUtils.ingredientsToRecipeId;

public class CompositeRecipeOptimiser {
	private final Map<String, List<RecipeData>> targetGroups = new HashMap<>();

	private final Map<String, List<String>> idTagEntries = new HashMap<>();

	public Collection<RecipeData> process(Collection<RecipeData> resources) {
		resources.forEach(this::findCommonGroup);
		return resolveGroups();
	}

	private void findCommonGroup(RecipeData data) {
		if (targetGroups.containsKey(data.target())) {
			targetGroups.get(data.target()).add(data);
		} else {
			targetGroups.put(data.target(), new ArrayList<>(List.of(data)));
		}
	}

	private List<RecipeData> resolveGroups() {
		return targetGroups.values().stream()
				.map(this::convertCommonsToTag)
				.flatMap(Collection::stream)
				.toList();
	}

	private Collection<RecipeData> convertCommonsToTag(List<RecipeData> entries) {
		var recipeMap = new HashMap<String, RecipeData>();
		for (RecipeData entry : entries) {
			if (RecipeTypes.of(entry.type()) == RecipeTypes.STATE_CRAFTING_RECIPE) {
				var converted = entry.ingredients().stream().map(this::convertIngredient).toList();
				var newRecipe = entry.toBuilder().ingredients(converted).build();
				recipeMap.put(ingredientsToRecipeId(converted), newRecipe);
			} else if (RecipeTypes.of(entry.type()) == RecipeTypes.TOOLPART_SCHEMATIC_RECIPE) {
				var converted = entry.ingredients().stream().map(this::convertMaterialSpecificIngredient).toList();
				var newRecipe = entry.toBuilder().ingredients(converted).build();
				recipeMap.put(ingredientsToRecipeId(converted), newRecipe);
			} else {
				recipeMap.put(ingredientsToRecipeId(entry.ingredients()), entry);
			}

		}
		return recipeMap.values();
	}

	private IngredientData convertIngredient(IngredientData data) {
		var converted = materialTypeTag(data);
		if (converted.isPresent()) {
			placeEntryIntoTagMap(converted.get(), data.id());
			return data.toBuilder().type(converted.get()).id(EMPTY_IDENTIFIER).build();
		} else {
			return data;
		}
	}

	private IngredientData convertMaterialSpecificIngredient(IngredientData data) {
		var converted = removeMaterialFromId(data);
		if (converted.isPresent()) {
			placeEntryIntoTagMap(converted.get(), data.id());
			return data.toBuilder().type(converted.get()).id(EMPTY_IDENTIFIER).build();
		} else {
			return data;
		}
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

	private void placeEntryIntoTagMap(String tag, String id) {
		if (idTagEntries.containsKey(tag)) {
			idTagEntries.get(tag).add(id);
		} else {
			idTagEntries.put(tag, new ArrayList<>(List.of(id)));
		}
	}

	private Optional<String> materialTypeTag(IngredientData data) {
		if (data.id().equals(EMPTY_IDENTIFIER) || !data.type().equals(EMPTY_IDENTIFIER)) {
			return Optional.empty();
		}
		var elements = data.id().split(ELEMENT_SEPARATOR);
		Optional<String> type = ForgeroStateRegistry.STATES.find(data.id()).map(Supplier::get).map(State::type).map(Type::typeName).map(String::toLowerCase);
		if (elements.length > 1 && type.isPresent()) {
			return Optional.of(String.format("%s-%s", elements[0].split(":")[1], type.get()));
		}
		return Optional.empty();
	}
}
