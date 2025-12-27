package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import net.minecraft.util.Identifier;

/**
 * Implementation of SmithingRecipeBuilder.
 */
public class SmithingRecipeBuilderImpl extends AbstractRecipeBuilder<SmithingRecipeBuilder> implements SmithingRecipeBuilder {

	private IngredientBuilder template;
	private IngredientBuilder base;
	private IngredientBuilder addition;

	@Override
	public SmithingRecipeBuilder template(String templateId) {
		return template(Identifier.tryParse(templateId));
	}

	@Override
	public SmithingRecipeBuilder template(Identifier templateId) {
		this.template = IngredientBuilder.create().item(templateId);
		return this;
	}

	@Override
	public SmithingRecipeBuilder templateTag(String tagId) {
		String normalized = tagId.startsWith("#") ? tagId.substring(1) : tagId;
		this.template = IngredientBuilder.create().tag(Identifier.tryParse(normalized));
		return this;
	}

	@Override
	public SmithingRecipeBuilder base(String baseId) {
		return base(Identifier.tryParse(baseId));
	}

	@Override
	public SmithingRecipeBuilder base(Identifier baseId) {
		this.base = IngredientBuilder.create().item(baseId);
		return this;
	}

	@Override
	public SmithingRecipeBuilder baseTag(String tagId) {
		String normalized = tagId.startsWith("#") ? tagId.substring(1) : tagId;
		this.base = IngredientBuilder.create().tag(Identifier.tryParse(normalized));
		return this;
	}

	@Override
	public SmithingRecipeBuilder addition(String additionId) {
		return addition(Identifier.tryParse(additionId));
	}

	@Override
	public SmithingRecipeBuilder addition(Identifier additionId) {
		this.addition = IngredientBuilder.create().item(additionId);
		return this;
	}

	@Override
	public SmithingRecipeBuilder additionTag(String tagId) {
		String normalized = tagId.startsWith("#") ? tagId.substring(1) : tagId;
		this.addition = IngredientBuilder.create().tag(Identifier.tryParse(normalized));
		return this;
	}

	@Override
	public IngredientBuilder getTemplate() {
		return template;
	}

	@Override
	public IngredientBuilder getBase() {
		return base;
	}

	@Override
	public IngredientBuilder getAddition() {
		return addition;
	}
}
