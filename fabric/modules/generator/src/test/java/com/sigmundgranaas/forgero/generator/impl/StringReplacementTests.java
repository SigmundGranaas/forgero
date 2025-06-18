package com.sigmundgranaas.forgero.generator.impl;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StringReplacementTests {
	@Test
	void entriesAreReplacedUsingFunctions() {
		Wood woodVariable = new Wood("oak", "minecraft", "item");

		StringReplacer converter = new StringReplacer(this::woodStringMapper);

		Map<String, Object> variable = new HashMap<>();

		variable.put("wood", woodVariable);

		String testRecipe = """
				 {
				  "type": "minecraft:crafting_shaped",
				  "pattern": [
				    "xx",
				    "x "
				  ],
				  "key": {
				    "x": {
				      "${wood.tagOrItem}": "${wood.container_id}"
				    }
				  },
				  "result": {
				    "item": "forgero:${wood.name}-axe_head"
				  }
				 }
				""";
		String result = converter.applyReplacements(testRecipe, variable);

		JsonObject convertedRecipe = new Gson().fromJson(result, JsonObject.class);
		String item = convertedRecipe.getAsJsonObject("result").get("item").getAsString();
		String itemX = convertedRecipe.getAsJsonObject("key").getAsJsonObject("x").get("item").getAsString();

		Assertions.assertEquals("forgero:oak-axe_head", item);
		Assertions.assertEquals("minecraft:oak", itemX);
	}

	@Test
	void multipleRecipesAreGeneratedFromVariables() {
		Wood oak = new Wood("oak", "minecraft", "item");
		Wood birch = new Wood("birch", "minecraft", "item");
		Wood acacia = new Wood("acacia", "minecraft", "item");

		StringReplacer converter = new StringReplacer(this::woodStringMapper);

		Map<String, Object> variable = new HashMap<>();

		variable.put("oak", oak);
		variable.put("birch", birch);
		variable.put("acacia", acacia);

		Function<JsonElement, Collection<?>> variableTransformer = (element) -> element.getAsJsonArray()
				.asList().stream()
				.map(JsonElement::getAsString)
				.map(variable::get)
				.toList();

		VariableToMapTransformer transformer = new VariableToMapTransformer(variableTransformer);

		String template = """
				 {
				  "identifier": "minecraft:${wood.name}-axe_head",
				  "variables": {
				  	"wood": ["oak", "birch", "acacia"]
				  },
				  "type": "minecraft:crafting_shaped",
				  "pattern": [
				    "xx",
				    "x "
				  ],
				  "key": {
				    "x": {
				      "${wood.tagOrItem}": "${wood.container_id}"
				    }
				  },
				  "result": {
				    "item": "forgero:${wood.name}-axe_head"
				  }
				 }
				""";
		JsonObject jsonTemplate = new Gson().fromJson(template, JsonObject.class);

		List<IdentifiedJson> result = new RecipeGenerator(converter, transformer).generateRecipeFrom(jsonTemplate);

		Assertions.assertEquals(3, result.size());
	}

	private String woodStringMapper(String function, Object object){
		if(object instanceof Wood wood){
			switch (function) {
				case "tagOrItem" -> {
					return wood.tagOrItem;
				}
				case "name" -> {
					return wood.name;
				}
				case "container_id" -> {
					return wood.containerId();
				}
			}
		}
		return "Something did not go as expected";
	}

	private record Wood(String name, String namespace, String tagOrItem){
		public String containerId(){
			return namespace + ":" + name;
		}
	}
}


