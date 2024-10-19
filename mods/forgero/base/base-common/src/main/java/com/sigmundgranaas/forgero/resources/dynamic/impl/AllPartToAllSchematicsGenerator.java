package com.sigmundgranaas.forgero.resources.dynamic.impl;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.dynamicresourcepack.resource.DynamicResourcePackImpl;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import static com.sigmundgranaas.forgero.core.Forgero.NAMESPACE;

public class AllPartToAllSchematicsGenerator extends PartToSchematicGenerator {
	public AllPartToAllSchematicsGenerator(@NotNull StateService service, RecipeCreator recipeCreator, RecipeFilter recipeFilter) {
		super(service, recipeCreator, recipeFilter);
	}

	@Override
	public void generate(@NotNull DynamicResourcePackImpl pack) {
		parts().stream()
		       .map(recipeCreator::createRecipe)
		       .flatMap(Optional::stream)
		       .map(this::convertRecipeData)
		       .forEach(recipe -> pack.put(generateId(recipe), recipe.toString().getBytes()));
	}

	@Override
	protected @NotNull Identifier generateId(@NotNull JsonObject recipe) {
		String output = recipe.getAsJsonObject("result").get("item").getAsString().split(":")[1];
		String ingredient = recipe.getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString().split(":")[1];
		return new Identifier(NAMESPACE, "forgero:recipes/" + output + ingredient + "_recipe" + ".json");
	}
}
