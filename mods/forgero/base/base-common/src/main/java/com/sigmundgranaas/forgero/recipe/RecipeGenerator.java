package com.sigmundgranaas.forgero.recipe;

public interface RecipeGenerator {
	default boolean isValid() {
		return true;
	}

	RecipeWrapper generate();
}
