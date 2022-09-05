package com.sigmundgranaas.forgero.core.state;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.type.Type.*;

public class NameCompositor {
    public String compositeName(List<Ingredient> ingredients) {
        return ingredients.stream()
                .sorted(Comparator.comparingInt(this::sorter))
                .map(this::mapper)
                .flatMap(Optional::stream)
                .collect(Collectors.joining(ELEMENT_SEPARATOR));
    }

    private int sorter(Ingredient ingredient) {
        if (ingredient.test(MATERIAL)) {
            return 1;
        } else if (ingredient.test(SCHEMATIC)) {
            return 2;
        }
        return 0;
    }

    private Optional<String> mapper(Ingredient ingredient) {
        if (ingredient.test(TOOL_PART_HEAD)) {
            return Optional.of(ingredient.name().replace("_head", ""));

        } else if (ingredient.test(HANDLE)) {
            return Optional.empty();

        } else if (ingredient.test(SCHEMATIC)) {
            var elements = ingredient.name().split(ELEMENT_SEPARATOR);
            if (elements.length == 2) {
                return Optional.of(elements[0]);
            }
        }
        return Optional.of(ingredient.name());
    }
}
