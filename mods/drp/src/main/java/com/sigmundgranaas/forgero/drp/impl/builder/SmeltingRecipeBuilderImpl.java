package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmeltingRecipeBuilder;
import net.minecraft.util.Identifier;

/**
 * Implementation of SmeltingRecipeBuilder.
 */
public class SmeltingRecipeBuilderImpl implements SmeltingRecipeBuilder {

	private final SmeltingType smeltingType;
	private IngredientBuilder input;
	private Identifier result;
	private int resultCount = 1;
	private String group;
	private float experience = 0.1f;
	private int cookingTime;

	public SmeltingRecipeBuilderImpl(SmeltingType smeltingType) {
		this.smeltingType = smeltingType;
		this.cookingTime = smeltingType.getDefaultCookingTime();
	}

	@Override
	public SmeltingRecipeBuilder input(String itemId) {
		return input(Identifier.tryParse(itemId));
	}

	@Override
	public SmeltingRecipeBuilder input(Identifier itemId) {
		this.input = IngredientBuilder.create().item(itemId);
		return this;
	}

	@Override
	public SmeltingRecipeBuilder inputTag(String tagId) {
		return inputTag(Identifier.tryParse(tagId));
	}

	@Override
	public SmeltingRecipeBuilder inputTag(Identifier tagId) {
		this.input = IngredientBuilder.create().tag(tagId);
		return this;
	}

	@Override
	public SmeltingRecipeBuilder experience(float experience) {
		this.experience = experience;
		return this;
	}

	@Override
	public SmeltingRecipeBuilder cookingTime(int ticks) {
		this.cookingTime = ticks;
		return this;
	}

	@Override
	public SmeltingRecipeBuilder group(String group) {
		this.group = group;
		return this;
	}

	@Override
	public SmeltingRecipeBuilder result(Identifier result) {
		this.result = result;
		return this;
	}

	@Override
	public SmeltingRecipeBuilder result(Identifier result, int count) {
		this.result = result;
		this.resultCount = count;
		return this;
	}

	@Override
	public IngredientBuilder getInput() {
		return input;
	}

	@Override
	public float getExperience() {
		return experience;
	}

	@Override
	public int getCookingTime() {
		return cookingTime;
	}

	@Override
	public SmeltingType getSmeltingType() {
		return smeltingType;
	}

	@Override
	public Identifier getResult() {
		return result;
	}

	@Override
	public int getResultCount() {
		return resultCount;
	}

	@Override
	public String getGroup() {
		return group;
	}
}
