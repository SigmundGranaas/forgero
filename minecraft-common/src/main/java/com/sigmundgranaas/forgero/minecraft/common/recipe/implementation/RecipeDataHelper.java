package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ResourceType;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.type.Type;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class RecipeDataHelper {

    public Optional<DataResource> findDefaultedResource(String id) {
        var resource = ForgeroStateRegistry.CONSTRUCTS.stream()
                .filter(res -> res.identifier().equals(id))
                .findAny();
        if (resource.isPresent() && resource.get().resourceType() != ResourceType.DEFAULT) {
            return ForgeroStateRegistry.CONSTRUCTS.stream()
                    .filter(res -> res.type().equals(resource.get().type()))
                    .filter(res -> res.resourceType() == ResourceType.DEFAULT)
                    .filter(res -> sameMaterial(res, resource.get()))
                    .findFirst();
        }
        return resource;
    }

    public boolean stateExists(String id) {
        return ForgeroStateRegistry.STATES.contains(id);
    }

    public String ingredientToDefaultedId(IngredientData data) {
        if (data.type().equals(EMPTY_IDENTIFIER)) {
            var resource = findDefaultedResource(data.id());
            if (resource.isPresent()) {
                return resource.get().identifier();
            }
            return data.id();
        } else {
            return ForgeroStateRegistry.CONSTRUCTS.stream()
                    .filter(res -> res.type().equals(data.type()))
                    .filter(res -> res.resourceType() == ResourceType.DEFAULT)
                    .map(DataResource::identifier)
                    .findFirst()
                    .orElse(data.id());
        }
    }

    private boolean sameMaterial(DataResource res, DataResource compare) {
        var test1 = getMaterial(res);
        var test2 = getMaterial(compare);
        return test1.isPresent() && test2.isPresent() && test2.get().equals(test1.get());
    }

    public Optional<String> getMaterial(DataResource res) {
        return Arrays.stream(res.name().split(ELEMENT_SEPARATOR))
                .map(element -> ForgeroStateRegistry.STATES.find(Type.MATERIAL).stream().filter(state -> state.get().name().equals(element)).findAny())
                .flatMap(Optional::stream)
                .map(Supplier::get)
                .filter(state -> state.test(Type.MATERIAL))
                .findFirst()
                .map(Identifiable::name);
    }
}
