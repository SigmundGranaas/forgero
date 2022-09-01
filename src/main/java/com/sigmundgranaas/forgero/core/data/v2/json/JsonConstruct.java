package com.sigmundgranaas.forgero.core.data.v2.json;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class JsonConstruct {
    @NotNull
    public String target = "this";
    @Nullable
    public List<JsonSlot> slots;
    @Nullable
    public JsonRecipe recipe;
}
