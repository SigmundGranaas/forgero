package com.sigmundgranaas.forgero.minecraft.common.recipe;

public interface RecipeGenerator {
	default boolean isValid() {
		return true;
	}

	RecipeWrapper generate();
}
