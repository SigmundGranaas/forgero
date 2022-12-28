package com.sigmundgranaas.forgero.resource.data.v2.data;

import com.sigmundgranaas.forgero.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final String target = Identifiers.THIS_IDENTIFIER;

    @Nullable
    @Builder.Default
    private final String type = Identifiers.EMPTY_IDENTIFIER;

    @Nullable
    @Builder.Default
    private final List<SlotData> slots = Collections.emptyList();

    public boolean container() {
        return container;
    }

    @NotNull
    public String target() {
        return Objects.requireNonNullElse(target, Identifiers.EMPTY_IDENTIFIER);
    }

    @NotNull
    public String type() {
        return Objects.requireNonNullElse(type, Identifiers.EMPTY_IDENTIFIER);
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
