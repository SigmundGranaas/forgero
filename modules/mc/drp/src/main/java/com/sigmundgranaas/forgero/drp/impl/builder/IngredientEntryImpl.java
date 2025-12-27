package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import net.minecraft.util.Identifier;

/**
 * Implementation of IngredientBuilder.IngredientEntry.
 */
public record IngredientEntryImpl(Identifier id, boolean isTag) implements IngredientBuilder.IngredientEntry {

	public static IngredientEntryImpl item(Identifier id) {
		return new IngredientEntryImpl(id, false);
	}

	public static IngredientEntryImpl tag(Identifier id) {
		return new IngredientEntryImpl(id, true);
	}
}
