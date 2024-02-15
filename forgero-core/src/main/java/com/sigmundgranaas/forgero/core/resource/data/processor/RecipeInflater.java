package com.sigmundgranaas.forgero.core.resource.data.processor;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.core.util.Identifiers.THIS_IDENTIFIER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.identifier.Common;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ResourceType;
import com.sigmundgranaas.forgero.core.util.Identifiers;

public class RecipeInflater {
	private final DataResource resource;

	private final Function<String, List<DataResource>> typeFinder;


	public RecipeInflater(DataResource resource, Function<String, List<DataResource>> typeFinder, Function<String, Optional<DataResource>> idFinder, Function<String, Optional<DataResource>> templateProvider) {
		this.resource = resource;
		this.typeFinder = typeFinder;
	}

	public List<RecipeData> process() {
		if (invalidData()) {
			return Collections.emptyList();
		}

		return resource.construct()
				.flatMap(ConstructData::recipes)
				.orElse(Collections.emptyList())
				.stream()
				.map(this::inflateIngredients)
				.flatMap(List::stream)
				.toList();
	}

	private List<RecipeData> inflateIngredients(RecipeData recipe) {
		var templateIngredients = new ArrayList<List<IngredientData>>();
		for (IngredientData ingredient : recipe.ingredients()) {
			if (ingredient.id().equals(Identifiers.THIS_IDENTIFIER)) {
				templateIngredients.add(List.of(IngredientData.builder().id(resource.identifier()).unique(true).build()));
			} else if (!ingredient.type().equals(Identifiers.EMPTY_IDENTIFIER)) {
				if (ingredient.unique()) {
					templateIngredients.add(findUniqueIngredients(ingredient));
				} else {
					templateIngredients.add(List.of(ingredient));
				}
			}
		}

		IngredientCollection collection = new IngredientCollection(templateIngredients.size());
		for (int i = 0; i < templateIngredients.size(); i++) {
			collection.addEntries(i, templateIngredients.get(i));
		}

		var ingredients = collection.getCollection()
				.stream()
				.map(inflater -> recipe.toBuilder().ingredients(inflater.getIngredients()).build())
				.toList();

		return buildRecipes(ingredients, recipe);
	}


	private List<RecipeData> buildRecipes(List<RecipeData> inflatedIngredients, RecipeData originalRecipe) {
		var recipes = new ArrayList<RecipeData>();

		for (RecipeData inflatedIngredient : inflatedIngredients) {
			var newComponents = inflatedIngredient.ingredients();
			String name = String.join(Common.ELEMENT_SEPARATOR, newComponents.stream().map(this::ingredientToName).toList());
			recipes.add(RecipeData.builder()
					.ingredients(newComponents)
					.craftingType(originalRecipe.type())
					.target(resource.nameSpace() + ":" + name)
					.count(originalRecipe.count())
					.build());
		}

		return recipes;
	}

	private String idToName(String id) {
		String[] split = id.split(":");
		if (split.length > 1) {
			return split[1];
		}
		return id;
	}

	private String ingredientToName(IngredientData data) {
		if (data.id().equals(EMPTY_IDENTIFIER)) {
			return typeFinder.apply(data.type()).stream().filter(res -> res.resourceType() == ResourceType.DEFAULT).findFirst().map(DataResource::name).orElse(data.type().toLowerCase(Locale.ENGLISH));
		} else {
			return idToName(data.id());
		}
	}

	private boolean isTyped(IngredientData data) {
		return !data.type().equals(EMPTY_IDENTIFIER);
	}

	private boolean isId(IngredientData data) {
		return !data.id().equals(EMPTY_IDENTIFIER);
	}

	private boolean isThis(IngredientData data) {
		return isId(data) && data.id().equals(THIS_IDENTIFIER);
	}


	private boolean invalidData() {
		return resource.construct().isEmpty();
	}

	private List<IngredientData> findUniqueIngredients(IngredientData data) {
		return typeFinder.apply(data.type()).stream()
				.map(res -> IngredientData.builder().id(res.identifier()).amount(data.amount()).unique(true).build())
				.toList();
	}

	public Set<String> dependencies() {
		return resource.construct()
				.flatMap(ConstructData::recipes)
				.orElse(Collections.emptyList())
				.stream()
				.map(RecipeData::ingredients)
				.flatMap(List::stream)
				.map(ingredient -> ingredient.id().equals(EMPTY_IDENTIFIER) ? ingredient.id() : ingredient.type())
				.collect(Collectors.toSet());

	}
}
