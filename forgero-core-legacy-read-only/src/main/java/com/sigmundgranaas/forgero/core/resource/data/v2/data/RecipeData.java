package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;

@SuppressWarnings("ClassCanBeRecord")
@Builder(toBuilder = true)
public class RecipeData {
	private final List<IngredientData> ingredients;
	@SerializedName("crafting_type")
	private final String craftingType;

	private final int count;

	private String target;

	public List<IngredientData> ingredients() {
		return Objects.requireNonNullElse(ingredients, Collections.emptyList());
	}

	public String type() {
		return Objects.requireNonNullElse(craftingType, Identifiers.EMPTY_IDENTIFIER);
	}

	public String target() {
		return Objects.requireNonNullElse(target, Identifiers.EMPTY_IDENTIFIER);
	}

	public int count() {
		if (count == 0) {
			return 1;
		}
		return count;
	}
}
