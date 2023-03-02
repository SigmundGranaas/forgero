package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeDataHelper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeWrapperImpl;

import net.minecraft.util.Identifier;

import java.util.stream.IntStream;

import static com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeUtils.ingredientsToJsonEntry;

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
		return new RecipeWrapperImpl(new Identifier(data.target()), template, RecipeTypes.SCHEMATIC_PART_CRAFTING);
	}

	@Override
	public boolean isValid() {
		if (generator.generate(type).isEmpty()) {
			return false;
		} else if (!helper.stateExists(data.target())) {
			return false;
		}
		return true;
	}
}
