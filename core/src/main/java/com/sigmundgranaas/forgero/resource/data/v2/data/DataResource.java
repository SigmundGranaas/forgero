package com.sigmundgranaas.forgero.resource.data.v2.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.resource.data.SchemaVersion;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.PropertyPojo;
import com.sigmundgranaas.forgero.state.Identifiable;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

@Builder(toBuilder = true)
public class DataResource implements Identifiable {

    @Builder.Default
    @Nullable
    private String name = EMPTY_IDENTIFIER;

    @Builder.Default
    @Nullable
    private String namespace = EMPTY_IDENTIFIER;

    @Builder.Default
    @Nullable
    private String type = EMPTY_IDENTIFIER;

    @SerializedName(value = "resource_type", alternate = "json_resource_type")
    @Builder.Default()
    @Nullable
    private ResourceType resourceType = ResourceType.UNDEFINED;

    @Builder.Default
    @Nullable
    private String parent = EMPTY_IDENTIFIER;

    @Builder.Default
    @Nullable
    private SchemaVersion version = SchemaVersion.V2;

    @Nullable
    @SerializedName(value = "dependencies", alternate = "dependency")
    private List<String> dependencies;

    @Nullable
    private ConstructData construct;

    @Nullable
    private ModelData model;

    @Nullable
    @SerializedName(value = "static", alternate = "json_static")
    private JsonStatic jsonStatic;

    @Builder.Default
    @Nullable
    private List<namedElement> children = Collections.emptyList();

    @Nullable
    private ContextData context;

    @Nullable
    private HostData container;

    @Nullable
    @SerializedName(value = "properties", alternate = "property")
    private PropertyPojo property;

    @Builder.Default
    private int priority = 5;

    @NotNull
    public String name() {
        return Objects.requireNonNullElse(name, EMPTY_IDENTIFIER);
    }

    @Override
    @NotNull
    public String nameSpace() {
        return Objects.requireNonNullElse(namespace, EMPTY_IDENTIFIER);
    }

    public int priority() {
        return priority;
    }

    @NotNull
    public String type() {
        return type == null ? EMPTY_IDENTIFIER : type;
    }

    @NotNull
    public ResourceType resourceType() {
        return Objects.requireNonNullElse(this.resourceType, ResourceType.UNDEFINED);
    }

    @NotNull
    public String parent() {
        return Objects.requireNonNullElse(parent, EMPTY_IDENTIFIER);
    }

    @NotNull
    public ImmutableList<String> dependencies() {
        if (dependencies == null) {
            return ImmutableList.<String>builder().build();
        }
        return ImmutableList.<String>builder().addAll(dependencies).build();
    }

    @NotNull
    public SchemaVersion version() {
        return Objects.requireNonNullElse(version, SchemaVersion.V2);
    }

    @NotNull
    public List<namedElement> children() {
        return Objects.requireNonNullElse(children, Collections.emptyList());
    }

    @NotNull
    public Optional<PropertyPojo> properties() {
        return Optional.ofNullable(property);
    }

    @NotNull
    public Optional<HostData> container() {
        return Optional.ofNullable(container);
    }

    @NotNull
    public Optional<ConstructData> construct() {
        return Optional.ofNullable(construct);
    }

    @NotNull
    public Optional<ContextData> context() {
        return Optional.ofNullable(context);
    }


}
