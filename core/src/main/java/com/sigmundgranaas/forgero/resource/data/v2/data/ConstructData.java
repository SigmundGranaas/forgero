package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.util.Identifiers.THIS_IDENTIFIER;

@Builder(toBuilder = true)
public class ConstructData {
    @Nullable
    private final List<RecipeData> recipes;

    @Nullable
    @Builder.Default
    private final boolean container = false;

    @Nullable
    @Builder.Default
    private final List<IngredientData> components = Collections.emptyList();

    @Nullable
    @Builder.Default
    private final String target = THIS_IDENTIFIER;

    @Nullable
    @Builder.Default
    private final String type = EMPTY_IDENTIFIER;

    @Nullable
    @Builder.Default
    private final List<SlotData> slots = Collections.emptyList();

    public boolean container() {
        return container;
    }

    @NotNull
    public String target() {
        return Objects.requireNonNullElse(target, EMPTY_IDENTIFIER);
    }

    @NotNull
    public String type() {
        return Objects.requireNonNullElse(type, EMPTY_IDENTIFIER);
    }

    @NotNull
    public List<SlotData> slots() {
        return Objects.requireNonNullElse(slots, Collections.emptyList());
    }

    @NotNull
    public List<IngredientData> components() {
        return Objects.requireNonNullElse(components, Collections.emptyList());
    }

    public Optional<List<RecipeData>> recipes() {
        return Optional.ofNullable(recipes);
    }
}
