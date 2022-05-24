package com.sigmundgranaas.forgero.core.data;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

import javax.annotation.Nullable;
import java.util.List;

public class ForgeroDataResource {
    public static List<String> DEFAULT_DEPENDENCIES = List.of("minecraft", "forgero");
    public String name;
    @Nullable
    public List<String> dependencies = DEFAULT_DEPENDENCIES;
    @Nullable
    public PropertyPOJO properties;
    @SerializedName(value = "resource_type", alternate = "resourceType")
    public ResourceType resourceType;
    @Nullable
    public String parent = null;
    @Nullable
    public SchemaVersion version = SchemaVersion.V1;
    @SerializedName(value = "abstract_resource", alternate = "abstractResource")
    public boolean abstractResource = false;
}
