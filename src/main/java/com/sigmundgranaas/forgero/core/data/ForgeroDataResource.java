package com.sigmundgranaas.forgero.core.data;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

public class ForgeroDataResource {
    public String[] dependencies;
    public String name;
    public PropertyPOJO properties;
    @SerializedName("resource_type")
    public ResourceType resourceType;
    public String parent;
    public SchemaVersion version = SchemaVersion.V1;
}
