package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeWrapperImpl;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PartSmeltingRecipeGenerator implements RecipeGenerator {

	private final State material;

	private final State part;
	private final RecipeTypes template;

	private final TemplateGenerator generator;

	public PartSmeltingRecipeGenerator(State material, State part, RecipeTypes template, TemplateGenerator generator) {
		this.material = material;
		this.part = part;
		this.template = template;
		this.generator = generator;
	}

	@Override
	public boolean isValid() {
		if (!Registry.ITEM.containsId(new Identifier(part.identifier()))) {
			return false;
		}
		return ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier());
	}

	@Override
	public RecipeWrapper generate() {
		JsonObject template = generator.generate(this.template).orElse(new JsonObject());
		template.getAsJsonObject("ingredient").addProperty("item", part.identifier());
		String result = Optional.ofNullable(ForgeroStateRegistry.STATE_TO_CONTAINER.get(material.identifier())).orElse(material.identifier());
		template.addProperty("result", result);
		template.addProperty("group", material.name());
		Identifier id = new Identifier(part.identifier() + "-" + this.template.getName());
		return new RecipeWrapperImpl(id, template);
	}
}
