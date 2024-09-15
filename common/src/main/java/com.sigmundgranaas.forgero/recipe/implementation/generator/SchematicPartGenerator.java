package com.sigmundgranaas.forgero.recipe.implementation.generator;

import static com.sigmundgranaas.forgero.recipe.implementation.RecipeUtils.ingredientsToJsonEntry;

import java.util.stream.IntStream;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.recipe.implementation.RecipeDataHelper;
import com.sigmundgranaas.forgero.recipe.implementation.RecipeWrapperImpl;

import net.minecraft.util.Identifier;

public class SchematicPartGenerator implements RecipeGenerator {

	private final RecipeTypes type = RecipeTypes.SCHEMATIC_PART_CRAFTING;
	private final RecipeDataHelper helper;
	private final RecipeData data;
	private final TemplateGenerator generator;

	public SchematicPartGenerator(RecipeDataHelper helper, RecipeData data, TemplateGenerator generator) {
		this.helper = helper;
		this.data = data;
		this.generator = generator;
	}

	@Override
	public RecipeWrapper generate() {
		JsonObject template = generator.generate(type).orElse(new JsonObject());
		for (IngredientData ingredient : data.ingredients()) {
			IntStream.range(0, ingredient.amount()).forEach(i -> template.getAsJsonArray("ingredients").add(ingredientsToJsonEntry(ingredient)));
		}
		template.getAsJsonObject("result").addProperty("item", data.target());
		return new RecipeWrapperImpl(new Identifier(data.target()), template);
	}

	@Override
	public boolean isValid() {
		if (generator.generate(type).isEmpty()) {
			return false;
		} else return helper.stateExists(data.target());
	}
}
