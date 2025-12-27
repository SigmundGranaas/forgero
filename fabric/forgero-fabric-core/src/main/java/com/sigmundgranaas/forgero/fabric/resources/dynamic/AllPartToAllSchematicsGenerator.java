package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

public class AllPartToAllSchematicsGenerator extends PartToSchematicGenerator {


	public AllPartToAllSchematicsGenerator(StateService service, RecipeCreator recipeCreator, RecipeFilter recipeFilter) {
		super(service, recipeCreator, recipeFilter);
	}

	@Override
	public void generate(DynamicResourcePack pack) {
		parts().stream()
				.map(recipeCreator::createRecipe)
				.flatMap(Optional::stream)
				.map(this::convertRecipeData)
				.forEach(recipe -> pack.addRawData(generatePath(recipe), recipe.toString().getBytes()));
	}

	@Override
	protected String generatePath(JsonObject recipe) {
		String output = recipe.getAsJsonObject("result").get("item").getAsString().split(":")[1];
		String ingredient = recipe.getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString().split(":")[1];
		return "data/forgero/recipes/" + output + ingredient + "_recipe.json";
	}
}
