package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Builder(toBuilder = true)
public class SlotData {
    @Builder.Default
    @Nullable
    private final List<Category> category = Collections.emptyList();

    @Builder.Default
    @Nullable
    private final String type = Identifiers.EMPTY_IDENTIFIER;

    @Builder.Default
    @Nullable
    private final String description = Identifiers.EMPTY_IDENTIFIER;

    @SerializedName(value = "upgrade_type", alternate = "upgradeType")
    @Builder.Default
    @Nullable
    private String upgradeType = Identifiers.EMPTY_IDENTIFIER;


    @Builder.Default
    private int tier = 1;

    @NotNull
    public String upgradeType() {
        return Objects.requireNonNullElse(upgradeType, Identifiers.EMPTY_IDENTIFIER);
    }

    @NotNull
    public List<Category> category() {
        return Objects.requireNonNullElse(category, Collections.emptyList());
    }

    @NotNull
    public String type() {
        return Objects.requireNonNullElse(type, Identifiers.EMPTY_IDENTIFIER);
    }

    @NotNull
    public String description() {
        return Objects.requireNonNullElse(description, Identifiers.EMPTY_IDENTIFIER);
    }


    public int tier() {
        return tier;
    }
}
