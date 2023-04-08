package com.sigmundgranaas.forgero.minecraft.common.dynamic.resource.data;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.dynamic.resource.JsonData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(fluent = true)
@SuperBuilder
public class ShapelessRecipe extends Recipe {
	protected List<Ingredient> ingredients;

	public static ShapelessRecipe.ShapelessRecipeBuilder<?, ?> defaultedBuilder() {
		return ShapelessRecipe.builder()
				.type("minecraft:crafting_shapeless")
				.group("crafting");
	}

	@Override
	public JsonObject asJson() {
		JsonObject recipe = super.asJson();
		JsonArray ingredientsJson = new JsonArray();
		ingredients.stream().map(JsonData::asJson).forEach(ingredientsJson::add);
		recipe.add("ingredients", ingredientsJson);
		return recipe;
	}
}
