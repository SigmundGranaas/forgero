package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.RecipeBuilder;
import net.minecraft.util.Identifier;

/**
 * Abstract base implementation for recipe builders.
 */
public abstract class AbstractRecipeBuilder<T extends RecipeBuilder<T>> implements RecipeBuilder<T> {

	protected String group;
	protected Identifier result;
	protected int resultCount = 1;

	@Override
	@SuppressWarnings("unchecked")
	public T group(String group) {
		this.group = group;
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T result(Identifier result) {
		this.result = result;
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T result(Identifier result, int count) {
		this.result = result;
		this.resultCount = count;
		return (T) this;
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
