package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;

import java.util.Map;
import java.util.Optional;

public class TemplateGenerator {
    private final Map<RecipeTypes, JsonObject> typeMap;

    public TemplateGenerator(Map<RecipeTypes, JsonObject> typeMap) {
        this.typeMap = typeMap;
    }

    public Optional<JsonObject> generate(RecipeTypes type) {
        return Optional.ofNullable(typeMap.get(type)).map(this::copyJson);
    }

    private JsonObject copyJson(JsonObject object) {
        return JsonParser.parseString(object.toString()).getAsJsonObject();
    }
}
