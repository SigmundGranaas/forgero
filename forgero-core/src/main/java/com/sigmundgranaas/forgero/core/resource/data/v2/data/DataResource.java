package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.customdata.CustomJsonDataContainer;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import com.sigmundgranaas.forgero.core.resource.data.SchemaVersion;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

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
	private List<ModelData> models;

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
	private PaletteData palette;

	@Nullable
	@SerializedName(value = "properties", alternate = "property")
	private PropertyPojo property;

	@Builder.Default
	private int priority = 5;

	@Builder.Default
	@SerializedName("custom_data")
	private Map<String, JsonElement> customData = new HashMap<>();

	public static <T> List<T> mergeProperty(List<T> attribute1, List<T> attribute2) {
		if (attribute1 == null && attribute2 == null)
			return Collections.emptyList();
		else if (attribute1 != null && attribute2 != null) {
			return Stream.of(attribute1, attribute2).flatMap(List::stream).toList();
		} else return Objects.requireNonNullElse(attribute1, attribute2);
	}

	public static List<PropertyPojo.Attribute> mergeAttributes(List<PropertyPojo.Attribute> attribute1, List<PropertyPojo.Attribute> attribute2) {
		if (attribute1 == null && attribute2 == null)
			return Collections.emptyList();
		else if (attribute1 != null && attribute2 != null) {
			var filteredMergedAttributes = attribute2.stream().filter(attr -> !attr.id.equals(EMPTY_IDENTIFIER) && attribute1.stream().noneMatch(attribute -> attribute.id.equals(attr.id))).toList();
			return Stream.of(attribute1, filteredMergedAttributes).flatMap(List::stream).toList();
		} else return Objects.requireNonNullElse(attribute1, attribute2);
	}

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
	public ImmutableList<ModelData> models() {
		if (models == null) {
			return ImmutableList.<ModelData>builder().build();
		}
		return ImmutableList.<ModelData>builder().addAll(models).build();
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

	@NotNull
	public Optional<PaletteData> palette() {
		return Optional.ofNullable(palette);
	}

	public DataResource mergeResource(DataResource resource) {
		var builder = this.toBuilder();

		var properties = properties().orElse(new PropertyPojo());
		var mergeProperties = resource.properties().orElse(new PropertyPojo());
		var newProps = new PropertyPojo();

		newProps.active = mergeProperty(mergeProperties.active, properties.active).stream().distinct().toList();
		newProps.passiveProperties = mergeProperty(mergeProperties.passiveProperties, properties.passiveProperties).stream().distinct().toList();
		newProps.attributes = mergeAttributes(properties.attributes, mergeProperties.attributes).stream().distinct().toList();
		newProps.features = mergeProperty(mergeProperties.features, properties.features).stream().distinct().toList();

		return builder.property(newProps).build();
	}

	public DataContainer getCustomData() {
		return CustomJsonDataContainer.of(customData);
	}
}
