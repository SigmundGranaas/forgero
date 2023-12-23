package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import static com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeUtils.ingredientsToJsonEntry;
import static com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeUtils.ingredientsToRecipeId;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeDataHelper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeWrapperImpl;

import net.minecraft.util.Identifier;

public class ToolRecipeCreator implements RecipeGenerator {

	private final RecipeTypes type = RecipeTypes.STATE_CRAFTING_RECIPE;
	private final RecipeDataHelper helper;
	private final RecipeData data;
	private final TemplateGenerator generator;

	public ToolRecipeCreator(RecipeData data, RecipeDataHelper helper, TemplateGenerator generator) {
		this.data = data;
		this.helper = helper;
		this.generator = generator;
	}

	public RecipeWrapper generate() {
		JsonObject jsonTemplate = generator.generate(type).orElse(new JsonObject());

		jsonTemplate.getAsJsonObject("key").add("H", ingredientsToJsonEntry(data.ingredients().get(0)));
		jsonTemplate.getAsJsonObject("key").add("I", ingredientsToJsonEntry(data.ingredients().get(1)));
		if (data.ingredients().size() > 2) {
			jsonTemplate.getAsJsonObject("key").add("G", ingredientsToJsonEntry(data.ingredients().get(2)));
			jsonTemplate.getAsJsonArray("pattern").set(0, new JsonPrimitive("  H"));
			jsonTemplate.getAsJsonArray("pattern").set(1, new JsonPrimitive(" I "));
			jsonTemplate.getAsJsonArray("pattern").add(new JsonPrimitive("G  "));
		}
		jsonTemplate.getAsJsonObject("result").addProperty("item", data.target());
		return new RecipeWrapperImpl(new Identifier(Forgero.NAMESPACE, ingredientsToRecipeId(data.ingredients())), jsonTemplate);
	}

	@Override
	public boolean isValid() {
		if (data.ingredients().size() > 3) {
			return false;
		} else if (!helper.stateExists(data.target())) {
			return false;
		} else return generator.generate(type).isPresent();
	}
}
