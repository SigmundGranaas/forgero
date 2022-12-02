package com.sigmundgranaas.forgero.resource.data.v2.data;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

@SuppressWarnings("ClassCanBeRecord")
@Builder(toBuilder = true)
public class RecipeData {
    private final List<IngredientData> ingredients;
    @SerializedName("crafting_type")
    private final String craftingType;

    private String target;

    public List<IngredientData> ingredients() {
        return Objects.requireNonNullElse(ingredients, Collections.emptyList());
    }

    public String type() {
        return Objects.requireNonNullElse(craftingType, EMPTY_IDENTIFIER);
    }

    public String target() {
        return Objects.requireNonNullElse(target, EMPTY_IDENTIFIER);
    }
}
