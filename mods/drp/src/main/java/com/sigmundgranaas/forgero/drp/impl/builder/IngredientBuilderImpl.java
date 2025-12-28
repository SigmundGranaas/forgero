package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of IngredientBuilder.
 */
public class IngredientBuilderImpl implements IngredientBuilder {

	private final List<IngredientEntry> entries = new ArrayList<>();

	@Override
	public IngredientBuilder item(String itemId) {
		return item(Identifier.tryParse(itemId));
	}

	@Override
	public IngredientBuilder item(Identifier itemId) {
		entries.add(IngredientEntryImpl.item(itemId));
		return this;
	}

	@Override
	public IngredientBuilder tag(String tagId) {
		String normalized = tagId.startsWith("#") ? tagId.substring(1) : tagId;
		return tag(Identifier.tryParse(normalized));
	}

	@Override
	public IngredientBuilder tag(Identifier tagId) {
		entries.add(IngredientEntryImpl.tag(tagId));
		return this;
	}

	@Override
	public IngredientBuilder or() {
		// In Minecraft's ingredient format, multiple entries in a list are OR'd together
		// So this is a no-op - just continue adding entries
		return this;
	}

	@Override
	public List<IngredientEntry> getEntries() {
		return List.copyOf(entries);
	}
}
