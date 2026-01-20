package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SlotData;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeDataHelper;

import net.minecraft.util.Identifier;

public class CraftingTableUpgradeGenerator implements RecipeGenerator {

	private final RecipeTypes type = RecipeTypes.STATE_UPGRADE_CRAFTING_TABLE_RECIPE;
	private final RecipeDataHelper helper;
	private final TemplateGenerator generator;
	private final SlotData slot;
	private final String target;

	public CraftingTableUpgradeGenerator(RecipeDataHelper helper, TemplateGenerator generator, SlotData slot, String target) {
		this.helper = helper;
		this.generator = generator;
		this.slot = slot;
		this.target = target;
	}

	@Override
	public RecipeWrapper generate() {
		JsonObject template = generator.generate(type).orElse(new JsonObject());

		JsonArray ingredients = template.getAsJsonArray("ingredients");
		JsonObject tag = new JsonObject();
		tag.addProperty("tag", "forgero:" + slot.type().toLowerCase());
		JsonObject targetItem = new JsonObject();
		targetItem.addProperty("item", target);

		ingredients.add(targetItem);
		ingredients.add(tag);
		template.add("ingredients", ingredients);

		template.getAsJsonObject("result").addProperty("item", target);
		Identifier id = new Identifier(target + ELEMENT_SEPARATOR + slot.type().toLowerCase() + ELEMENT_SEPARATOR + "crafting_table");
		return RecipeWrapper.of(id, template);
	}

	@Override
	public boolean isValid() {
		return helper.stateExists(target);
	}
}
