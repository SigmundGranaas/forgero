package com.sigmundgranaas.forgero.generator.impl.recipe.validation;

import com.google.gson.JsonObject;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.core.Forgero.LOGGER;

public class ResultValidator {

	public boolean validateResult(JsonObject json) {
		if (!json.has("result")) {
			LOGGER.error("Missing result for recipe");
			return false;
		}

		JsonObject result = json.getAsJsonObject("result");
		if (!result.has("item")) {
			LOGGER.error("Missing item in result");
			return false;
		}

		Identifier item = new Identifier(result.get("item").getAsString());
		if (!Registries.ITEM.containsId(item)) {
			LOGGER.error("Invalid result item: {}", item);
			return false;
		}

		return true;
	}
}

