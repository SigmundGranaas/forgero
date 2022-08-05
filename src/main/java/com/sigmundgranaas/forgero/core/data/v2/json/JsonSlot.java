package com.sigmundgranaas.forgero.core.data.v2.json;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class JsonSlot {
    @Nullable
    @SerializedName(value = "upgrade_type", alternate = "upgradeType")
    public String upgradeType;

    @NotNull
    public List<String> category = Collections.emptyList();

    @NotNull
    public String type = "EMPTY";

    public int tier = 1;
}
