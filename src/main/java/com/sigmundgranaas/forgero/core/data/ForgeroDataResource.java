package com.sigmundgranaas.forgero.core.data;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.data.v1.pojo.PropertyPojo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ForgeroDataResource {
    public static List<String> DEFAULT_DEPENDENCIES_LIST = List.of("minecraft", "forgero");
    public static Set<String> DEFAULT_DEPENDENCIES_SET = Set.of("minecraft", "forgero");
    public String name;
    @Nullable
    @SerializedName(value = "dependencies", alternate = "dependency")
    public List<String> dependencies = DEFAULT_DEPENDENCIES_LIST;
    @Nullable
    public PropertyPojo properties;
    @SerializedName(value = "resource_type", alternate = "resourceType")
    public ResourceType resourceType;
    @Nullable
    public String parent = null;
    @Nullable
    public SchemaVersion version = SchemaVersion.V1;
    @SerializedName(value = "abstract_resource", alternate = "abstractResource")
    public boolean abstractResource = false;

    @SerializedName(value = "required")
    public boolean required = false;

    public int order = 0;

    public String getName() {
        return name.toLowerCase();
    }
}
