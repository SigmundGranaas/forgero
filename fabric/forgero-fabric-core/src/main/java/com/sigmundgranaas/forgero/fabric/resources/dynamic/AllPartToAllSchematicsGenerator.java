package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import net.devtech.arrp.api.RuntimeResourcePack;

import net.minecraft.util.Identifier;

public class AllPartToAllSchematicsGenerator extends PartToSchematicGenerator {


	public AllPartToAllSchematicsGenerator(StateService service) {
		super(service);
	}

	@Override
	public void generate(RuntimeResourcePack pack) {
		parts().stream()
				.map(this::createRecipe)
				.flatMap(Optional::stream)
				.map(this::convertRecipeData)
				.forEach(recipe -> pack.addData(generateId(recipe), recipe.toString().getBytes()));
	}

	@Override
	protected Identifier generateId(JsonObject recipe) {
		String output = recipe.getAsJsonObject("result").get("item").getAsString().split(":")[1];
		String ingredient = recipe.getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString().split(":")[1];
		return new Identifier("forgero:recipes/" + output + ingredient + "_recipe" + ".json");
	}
}
