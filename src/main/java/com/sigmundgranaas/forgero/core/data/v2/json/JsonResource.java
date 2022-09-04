package com.sigmundgranaas.forgero.core.data.v2.json;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.data.SchemaVersion;
import com.sigmundgranaas.forgero.core.data.v1.pojo.PropertyPojo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.core.data.v1.ForgeroDataResource.DEFAULT_DEPENDENCIES_LIST;
import static com.sigmundgranaas.forgero.core.data.v2.json.JsonResourceType.UNDEFINED;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public class JsonResource {
    @Nullable
    public String name = EMPTY_IDENTIFIER;

    @Nullable
    public String type = EMPTY_IDENTIFIER;

    @Nullable
    @SerializedName(value = "resource_type", alternate = "json_resource_type")
    public JsonResourceType resourceType = UNDEFINED;

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

    public List<JsonChild> children = Collections.emptyList();

    @Nullable
    public JsonContext context;

    @Nullable
    @SerializedName(value = "properties", alternate = "property")
    public PropertyPojo property;

    public JsonResource applyDefaults() {
        var res = this.copy();
        if (res.context == null || res.context.defaults == null) {
            return res;
        }
        var defaultResource = res.context.defaults.applyDefaults();
        if (res.name == EMPTY_IDENTIFIER) {
            res.name = defaultResource.name;
        }
        if (res.dependencies == DEFAULT_DEPENDENCIES_LIST) {
            res.dependencies = defaultResource.dependencies;
        }
        if (res.parent == EMPTY_IDENTIFIER) {
            res.parent = defaultResource.parent;
        }
        if (res.resourceType == UNDEFINED) {
            res.resourceType = defaultResource.resourceType;
        }


        return res;
    }

    public JsonResource copy() {
        JsonResource res = new JsonResource();
        res.name = name;
        res.type = type;
        res.context = context.copy();
        res.resourceType = resourceType;
        res.children = children;
        res.parent = parent;
        res.model = model;
        res.jsonStatic = jsonStatic;
        res.jsonHost = jsonHost;
        res.construct = construct;
        res.property = property;
        return res;
    }
}
