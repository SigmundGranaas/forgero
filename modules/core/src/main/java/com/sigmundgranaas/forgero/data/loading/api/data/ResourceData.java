package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Unified DTO for all resource types: materials, shapes, schematics, casts, and static parts.
 *
 * <p>This record replaces the separate MaterialData, ShapeData, SchematicData, CastData, and StaticData
 * records. The semantic type is determined by the {@link #type()} field value:</p>
 * <ul>
 *   <li>{@code "forgero:material"} - Base material like iron or diamond</li>
 *   <li>{@code "forgero:shape"} - Tool/weapon shape like pickaxe_head or blade</li>
 *   <li>{@code "forgero:schematic"} - Shape variant with crafting bonuses</li>
 *   <li>{@code "forgero:cast"} - Another shape variant mechanism</li>
 *   <li>{@code "forgero:static_part"} - Single, non-generated component</li>
 * </ul>
 *
 * <h3>Inheritance Semantics:</h3>
 * <ul>
 *   <li><strong>Inherited Properties:</strong> {@code tags}, {@code attributes} - merged via include chain</li>
 *   <li><strong>Local Properties:</strong> {@code localTags}, {@code localAttributes} - NOT inherited</li>
 * </ul>
 *
 * @param type            The resource type identifier (determines semantic meaning)
 * @param name            The unique name of this resource
 * @param include         Optional list of IDs of other definitions to include (inherit from)
 * @param tags            Optional tags associated with this resource (inherited via include)
 * @param localTags       Optional tags LOCAL to this resource (NOT inherited via include)
 * @param host            Optional data for mapping to a platform-specific item
 * @param attributes      Optional attributes provided by this resource (inherited via include)
 * @param localAttributes Optional attributes LOCAL to this resource (NOT inherited via include)
 * @param properties      Optional map for custom, extensible properties
 * @param upgrades        Optional upgrade slots (for static_part type)
 * @param target          (DEPRECATED) For schematics, the part_template this crafts. Kept for backward compatibility.
 */
public record ResourceData(
		OpenIdentifier type,
		String name,
		@Nullable List<OpenIdentifier> include,
		@Nullable List<OpenIdentifier> tags,
		@Nullable List<OpenIdentifier> localTags,
		@Nullable HostData host,
		@Nullable List<AttributeData> attributes,
		@Nullable List<AttributeData> localAttributes,
		@Nullable Map<String, JsonElement> properties,
		@Nullable List<UpgradeSlotData> upgrades,
		@Deprecated @Nullable OpenIdentifier target
) implements DefinitionData, ResourceTypeData {

	/**
	 * Compact constructor for resources without upgrades or deprecated target field.
	 * This is the most common case for materials, shapes, casts.
	 */
	public ResourceData(
			OpenIdentifier type,
			String name,
			@Nullable List<OpenIdentifier> include,
			@Nullable List<OpenIdentifier> tags,
			@Nullable List<OpenIdentifier> localTags,
			@Nullable HostData host,
			@Nullable List<AttributeData> attributes,
			@Nullable List<AttributeData> localAttributes,
			@Nullable Map<String, JsonElement> properties
	) {
		this(type, name, include, tags, localTags, host, attributes, localAttributes, properties, null, null);
	}

	/**
	 * Derives the shape name for use in ID templates.
	 *
	 * <p>If this resource has no includes, returns the resource's own name.
	 * If it has includes, returns the name portion of the first include.</p>
	 *
	 * @see ResourceTypeData#shapeName()
	 */
	@Override
	public String shapeName() {
		if (include == null || include.isEmpty()) {
			return name;
		}
		return include.get(0).name();
	}

	@Override
	public DefinitionData withMergedExtension(
			List<OpenIdentifier> mergedTags,
			List<AttributeData> mergedAttributes,
			Map<String, JsonElement> mergedProperties
	) {
		return withMergedExtension(mergedTags, mergedAttributes, mergedProperties, upgrades);
	}

	@Override
	public DefinitionData withMergedExtension(
			List<OpenIdentifier> mergedTags,
			List<AttributeData> mergedAttributes,
			Map<String, JsonElement> mergedProperties,
			List<UpgradeSlotData> mergedUpgrades
	) {
		return new ResourceData(
				type,
				name,
				include,
				mergedTags,
				localTags,
				host,
				mergedAttributes,
				localAttributes,
				mergedProperties,
				mergedUpgrades,
				target
		);
	}

	// ===== Factory methods for creating from deprecated types =====

	/**
	 * Creates a ResourceData from a MaterialData.
	 */
	public static ResourceData from(MaterialData data) {
		return new ResourceData(
				data.type(),
				data.name(),
				data.include(),
				data.tags(),
				data.localTags(),
				data.host(),
				data.attributes(),
				data.localAttributes(),
				data.properties()
		);
	}

	/**
	 * Creates a ResourceData from a ShapeData.
	 */
	public static ResourceData from(ShapeData data) {
		return new ResourceData(
				data.type(),
				data.name(),
				data.include(),
				data.tags(),
				data.localTags(),
				data.host(),
				data.attributes(),
				data.localAttributes(),
				data.properties()
		);
	}

	/**
	 * Creates a ResourceData from a SchematicData.
	 */
	public static ResourceData from(SchematicData data) {
		return new ResourceData(
				data.type(),
				data.name(),
				data.include(),
				data.tags(),
				data.localTags(),
				data.host(),
				data.attributes(),
				data.localAttributes(),
				data.properties(),
				null,
				data.target()
		);
	}

	/**
	 * Creates a ResourceData from a CastData.
	 */
	public static ResourceData from(CastData data) {
		return new ResourceData(
				data.type(),
				data.name(),
				data.include(),
				data.tags(),
				data.localTags(),
				data.host(),
				data.attributes(),
				data.localAttributes(),
				data.properties()
		);
	}

	/**
	 * Creates a ResourceData from a StaticData.
	 */
	public static ResourceData from(StaticData data) {
		return new ResourceData(
				data.type(),
				data.name(),
				data.include(),
				data.tags(),
				null, // StaticData doesn't have localTags
				data.host(),
				data.attributes(),
				null, // StaticData doesn't have localAttributes
				data.properties(),
				data.upgrades(),
				null
		);
	}
}
