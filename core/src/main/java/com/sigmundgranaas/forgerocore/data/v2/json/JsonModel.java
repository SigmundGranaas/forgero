package com.sigmundgranaas.forgerocore.data.v2.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsonModel {
    @NotNull
    public String modelType = "COMPOSITE";
    @Nullable
    public List<JsonModelEntry> models;
}
