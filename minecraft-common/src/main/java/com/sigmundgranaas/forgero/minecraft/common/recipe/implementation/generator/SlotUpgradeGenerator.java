package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SlotData;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeDataHelper;

import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public class SlotUpgradeGenerator implements RecipeGenerator {

	private final RecipeTypes type = RecipeTypes.STATE_UPGRADE_RECIPE;
	private final RecipeDataHelper helper;
	private final TemplateGenerator generator;

	private final SlotData slot;

	private final String target;

	public SlotUpgradeGenerator(RecipeDataHelper helper, TemplateGenerator generator, SlotData slot, String target) {
		this.helper = helper;
		this.generator = generator;
		this.slot = slot;
		this.target = target;
	}


	@Override
	public RecipeWrapper generate() {
		JsonObject jsonTemplate = generator.generate(type).orElse(new JsonObject());
		jsonTemplate.getAsJsonObject("base").addProperty("item", target);
		jsonTemplate.getAsJsonObject("addition").addProperty("tag", "forgero:" + slot.type().toLowerCase());
		jsonTemplate.getAsJsonObject("result").addProperty("item", target);
		Identifier id = new Identifier(target + ELEMENT_SEPARATOR + slot.type().toLowerCase());
		return RecipeWrapper.of(id, jsonTemplate, type);
	}

	@Override
	public boolean isValid() {
		if (!helper.stateExists(target)) {
			return false;
		}
		return true;
	}
}
