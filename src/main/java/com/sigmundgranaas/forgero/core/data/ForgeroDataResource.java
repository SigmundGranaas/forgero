package com.sigmundgranaas.forgero.core.data;

import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

public class ForgeroDataResource {
    public String[] dependencies;
    public String name;
    public PropertyPOJO properties;
    public ResourceType resourceType;
    public String parent;
    public SchemaVersion version = SchemaVersion.V1;
}
