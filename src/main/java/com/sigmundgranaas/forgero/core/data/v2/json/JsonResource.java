package com.sigmundgranaas.forgero.core.data.v2.json;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.data.SchemaVersion;

import javax.annotation.Nullable;
import java.util.List;

import static com.sigmundgranaas.forgero.core.data.v1.ForgeroDataResource.DEFAULT_DEPENDENCIES_LIST;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class JsonResource {
    @Nullable
    public String name;
    @Nullable
    public String type = EMPTY_IDENTIFIER;
    @SerializedName(value = "parent_type")
    public String parentType = EMPTY_IDENTIFIER;
    public String parent = EMPTY_IDENTIFIER;
    public SchemaVersion version = SchemaVersion.V2;
    @Nullable
    @SerializedName(value = "dependencies", alternate = "dependency")
    public List<String> dependencies = DEFAULT_DEPENDENCIES_LIST;
    @Nullable
    public JsonConstruct construct;
    @Nullable
    public JsonModel model;
    @Nullable
    @SerializedName(value = "static", alternate = "json_static")
    public JsonStatic jsonStatic;
    @Nullable
    @SerializedName(value = "host")
    public JsonHost jsonHost;
}
