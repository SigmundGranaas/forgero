package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.ArrayList;
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
	private DependencyData dependencies;

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

	/**
	 * Merges two lists of {@link PropertyPojo.Attribute}, considering both id and order.
	 * If two attributes have the same id but different priority, the one with the higher priority is kept.
	 *
	 * @param first the first list of attributes to merge.
	 * @param second the second list of attributes to merge.
	 * @return a merged list of attributes, with duplicates removed based on id and higher order preference.
	 */
	public static List<PropertyPojo.Attribute> mergeAttributes(List<PropertyPojo.Attribute> first,
	                                                           List<PropertyPojo.Attribute> second) {
		if (areAllListsNull(first, second)) {
			return Collections.emptyList();
		} else if (areNoListsNull(first, second)) {
			return mergeNonNullAttributeLists(first, second);
		} else {
			return Objects.requireNonNullElse(first, second);
		}
	}

	private static List<PropertyPojo.Attribute> mergeNonNullAttributeLists(List<PropertyPojo.Attribute> firstList,
	                                                                       List<PropertyPojo.Attribute> secondList) {
		var combinedAttributes = new HashMap<String, PropertyPojo.Attribute>();

		for (PropertyPojo.Attribute attr : firstList) {
			combinedAttributes.put(attr.id, attr);
		}

		for (PropertyPojo.Attribute attr : secondList) {
			combinedAttributes.merge(attr.id, attr, DataResource::attributeMergeRule);
		}

		return new ArrayList<>(combinedAttributes.values());
	}

	/**
	 * Merges two attributes, considering both id and order.
	 * @param existingAttr the attribute that already exists.
	 * @param newAttr the attribute that is being merged.
	 * @return the attribute with the highest priority.
	 */
	private static PropertyPojo.Attribute attributeMergeRule(PropertyPojo.Attribute existingAttr, PropertyPojo.Attribute newAttr) {
		if (newAttr.priority > existingAttr.priority && newAttr.id.equals(existingAttr.id)) {
			return newAttr;
		} else {
			return existingAttr;
		}
	}

	/**
	 * Checks if all provided lists are null.
	 *
	 * @param lists an array of lists to check for null.
	 * @return true if all lists are null, false otherwise.
	 */
	private static boolean areAllListsNull(List<?>... lists) {
		for (List<?> list : lists) {
			if (list != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if none of the provided lists are null.
	 *
	 * @param lists an array of lists to check for non-null.
	 * @return true if none of the lists are null, false otherwise.
	 */
	private static boolean areNoListsNull(List<?>... lists) {
		for (List<?> list : lists) {
			if (list == null) {
				return false;
			}
		}
		return true;
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
	public DependencyData dependencies() {
		if (dependencies == null) {
			return DependencyData.empty();
		}
		return dependencies;
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
		
		newProps.setAttributes(mergeAttributes(properties.getAttributes(), mergeProperties.getAttributes()).stream().distinct().toList());
		newProps.features = mergeProperty(mergeProperties.features, properties.features).stream().distinct().toList();

		return builder.property(newProps).build();
	}

	public DataContainer getCustomData() {
		return CustomJsonDataContainer.of(customData);
	}
}
