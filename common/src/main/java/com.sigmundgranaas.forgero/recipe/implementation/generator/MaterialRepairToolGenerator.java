package com.sigmundgranaas.forgero.recipe.implementation.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.utils.ItemUtils;
import com.sigmundgranaas.forgero.utils.StateUtils;

import net.minecraft.util.Identifier;

public class MaterialRepairToolGenerator implements RecipeGenerator {
	private final RecipeTypes type = RecipeTypes.REPAIR_KIT_RECIPE;
	private final State material;

	private final TemplateGenerator generator;

	public MaterialRepairToolGenerator(State material, TemplateGenerator generator) {
		this.material = material;
		this.generator = generator;
	}

	@Override
	public RecipeWrapper generate() {
		JsonObject template = generator.generate(RecipeTypes.SCHEMATIC_PART_CRAFTING).orElse(new JsonObject());
		template.addProperty("type", "forgero:repair_kit_recipe");
		JsonArray ingredients = template.getAsJsonArray("ingredients");
		JsonObject toolTag = new JsonObject();
		toolTag.addProperty("tag", "forgero:" + String.format("%s_tool", material.name()));
		JsonObject repairKit = new JsonObject();
		repairKit.addProperty("item", StateUtils.defaultedContainerMapper(material).toString());

		ingredients.add(toolTag);
		ingredients.add(repairKit);
		template.add("ingredients", ingredients);

		template.getAsJsonObject("result").addProperty("item", "forgero:" + material.name() + "_repair_kit");
		Identifier id = new Identifier(Forgero.NAMESPACE, material.name() + "_tool_basic_repair_recipe");
		return RecipeWrapper.of(id, template);
	}

	@Override
	public boolean isValid() {
		return ItemUtils.exists(material) && ForgeroConfigurationLoader.configuration.enableRepairKits;
	}
}
