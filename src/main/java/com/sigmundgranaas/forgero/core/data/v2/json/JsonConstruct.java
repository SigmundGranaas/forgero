package com.sigmundgranaas.forgero.core.data.v2.json;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class JsonConstruct {
    @NotNull
    public String target = "this";
    @NotNull
    public String type = EMPTY_IDENTIFIER;
    @Nullable
    public List<JsonSlot> slots;
    @Nullable
    public JsonRecipe recipe;
}
