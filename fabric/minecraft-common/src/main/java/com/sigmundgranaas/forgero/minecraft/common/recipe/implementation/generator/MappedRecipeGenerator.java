package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeWrapperImpl;

import net.minecraft.util.Identifier;

public class MappedRecipeGenerator implements RecipeGenerator {
	public static final String identifier = "forgero:mapped_recipe_generator";
	private final StringReplacer replacer;
	private final Map<String, State> stateMap;
	private JsonObject template;

	public MappedRecipeGenerator(StringReplacer replacer, JsonObject template, Map<String, State> stateMap) {
		this.replacer = replacer;
		this.template = template;
		this.stateMap = stateMap;
	}

	@Override
	public RecipeWrapper generate() {
		Identifier id = new Identifier(replacer.applyReplacements(template.get("identifier").getAsString(), stateMap));
		template.remove("identifier");
		template.remove("generator_type");
		template.remove("state_map");

		template = new Gson().fromJson(replacer.applyReplacements(this.template.toString(), stateMap), JsonObject.class);
		return new RecipeWrapperImpl(id, template);
	}
}
