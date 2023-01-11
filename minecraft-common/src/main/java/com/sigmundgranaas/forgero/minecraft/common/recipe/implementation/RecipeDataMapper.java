package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.state.composite.NameCompositor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class RecipeDataMapper implements Function<RecipeData, RecipeData> {
    private final RecipeDataHelper helper;

    public RecipeDataMapper(RecipeDataHelper helper) {
        this.helper = helper;
    }

    @Override
    public RecipeData apply(RecipeData data) {
        var ingredients = data.ingredients().stream().map(this::mapIngredient).toList();
        var target = resolveTarget(data);
        return data.toBuilder().ingredients(ingredients).target(target).build();
    }

    private IngredientData mapIngredient(IngredientData data) {
        var builder = data.toBuilder();
        if (data.unique() && !data.id().equals(EMPTY_IDENTIFIER)) {
            String id;
            if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(ForgeroStateRegistry.ID_MAPPER.get(data.id()))) {
                id = ForgeroStateRegistry.STATE_TO_CONTAINER.get(ForgeroStateRegistry.ID_MAPPER.get(data.id()));
            } else if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsValue(data.id())) {
                id = ForgeroStateRegistry.STATE_TO_CONTAINER.entrySet().stream().filter(entry -> entry.getValue().equals(data.id())).map(Map.Entry::getKey).findFirst().orElse(data.id());
            } else {
                id = data.id();
            }
            builder.id(id);
        } else if (!data.id().equals(EMPTY_IDENTIFIER)) {
            builder.id(ForgeroStateRegistry.stateFinder().find(ForgeroStateRegistry.ID_MAPPER.get(data.id())).get().type().typeName().toLowerCase());

        } else {
            builder.type(data.type().toLowerCase());
        }
        return builder.build();
    }

    private String resolveTarget(RecipeData data) {
        var idMapper = ForgeroStateRegistry.ID_MAPPER;
        if (idMapper.containsKey(data.target())) {
            return idMapper.get(data.target());
        } else {
            var states = data.ingredients().stream()
                    .map(helper::ingredientToDefaultedId)
                    .map(id -> Optional.ofNullable(idMapper.get(id)).orElse(id))
                    .map(id -> ForgeroStateRegistry.STATES.find(id))
                    .flatMap(Optional::stream)
                    .map(Supplier::get)
                    .toList();

            return String.format("%s:%s", Forgero.NAMESPACE, new NameCompositor().compositeName(states));
        }
    }
}
