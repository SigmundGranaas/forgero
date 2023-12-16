package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeWrapperImpl;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BasicWoodenToolRecipeGenerator implements RecipeGenerator {

	private final State material;

	private final String partName;
	private final RecipeTypes template;

	private final TemplateGenerator generator;

	public BasicWoodenToolRecipeGenerator(State material, String partName, RecipeTypes template, TemplateGenerator generator) {
		this.material = material;
		this.partName = partName;
		this.template = template;
		this.generator = generator;
	}

	@Override
	public boolean isValid() {
		if (!Registry.ITEM.containsId(new Identifier(String.format("forgero:%s-%s", material.name(), partName)))) {
			return false;
		}
		return ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier());
	}

	@Override
	public RecipeWrapper generate() {
		JsonObject template = generator.generate(this.template).orElse(new JsonObject());

		String resultName = material.name() + "-" + partName;
		template.getAsJsonObject("result").addProperty("item", "forgero:" + resultName);
		replaceKeyWithMaterialName("x", template);
		replaceKeyWithMaterialName("i", template);
		Identifier id = new Identifier(Forgero.NAMESPACE, resultName + "-basic_recipe");
		return new RecipeWrapperImpl(id, template);
	}

	private void replaceKeyWithMaterialName(String key, JsonObject template) {
		if (template.getAsJsonObject("key").has(key)) {
			String ingredient = template.getAsJsonObject("key").getAsJsonObject(key).get("item").getAsString();
			ingredient = ingredient.replace("oak", material.name());
			ingredient = ingredient.replace("minecraft", material.nameSpace());
			template.getAsJsonObject("key").getAsJsonObject(key).addProperty("item", ingredient);
		}
	}
}
