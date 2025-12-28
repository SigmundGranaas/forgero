package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementation of ShapedRecipeBuilder.
 */
public class ShapedRecipeBuilderImpl extends AbstractRecipeBuilder<ShapedRecipeBuilder> implements ShapedRecipeBuilder {

	private final List<String> pattern = new ArrayList<>();
	private final Map<Character, IngredientBuilder> keys = new HashMap<>();

	@Override
	public ShapedRecipeBuilder pattern(String row) {
		if (pattern.size() >= 3) {
			throw new IllegalArgumentException("Pattern cannot have more than 3 rows");
		}
		if (row.length() > 3) {
			throw new IllegalArgumentException("Pattern row cannot be longer than 3 characters");
		}
		pattern.add(row);
		return this;
	}

	@Override
	public ShapedRecipeBuilder key(char key, String itemId) {
		return key(key, Identifier.tryParse(itemId));
	}

	@Override
	public ShapedRecipeBuilder key(char key, Identifier itemId) {
		IngredientBuilder ingredient = IngredientBuilder.create().item(itemId);
		keys.put(key, ingredient);
		return this;
	}

	@Override
	public ShapedRecipeBuilder keyTag(char key, String tagId) {
		String normalized = tagId.startsWith("#") ? tagId.substring(1) : tagId;
		return keyTag(key, Identifier.tryParse(normalized));
	}

	@Override
	public ShapedRecipeBuilder keyTag(char key, Identifier tagId) {
		IngredientBuilder ingredient = IngredientBuilder.create().tag(tagId);
		keys.put(key, ingredient);
		return this;
	}

	@Override
	public ShapedRecipeBuilder key(char key, Consumer<IngredientBuilder> configurator) {
		IngredientBuilder ingredient = IngredientBuilder.create();
		configurator.accept(ingredient);
		keys.put(key, ingredient);
		return this;
	}

	@Override
	public List<String> getPattern() {
		return List.copyOf(pattern);
	}

	@Override
	public Map<Character, IngredientBuilder> getKeys() {
		return Map.copyOf(keys);
	}
}
