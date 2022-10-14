package com.sigmundgranaas.forgero.resource.data.v2.data;


import lombok.Builder;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;


@Builder(toBuilder = true)
public class IngredientData {
    @Builder.Default
    private final boolean unique = false;

    @Builder.Default
    private final int amount = 1;
    @Builder.Default
    @Nullable
    private final String type = EMPTY_IDENTIFIER;
    @Builder.Default
    @Nullable
    private final String id = EMPTY_IDENTIFIER;

    public boolean unique() {
        return unique;
    }

    public String type() {
        return Objects.requireNonNullElse(type, EMPTY_IDENTIFIER);
    }

    public String id() {
        return Objects.requireNonNullElse(id, EMPTY_IDENTIFIER);
    }

}
