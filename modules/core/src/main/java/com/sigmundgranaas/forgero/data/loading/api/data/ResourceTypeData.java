package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Common interface for all resource types (Shape, Material, Schematic, Cast, etc.).
 *
 * <p>This interface provides a unified view of resource properties and supports
 * the include-based inheritance system with local vs inherited properties.</p>
 *
 * <p><strong>Key Concepts:</strong></p>
 * <ul>
 *   <li><strong>Inherited Properties:</strong> {@code tags}, {@code attributes} -
 *       These are inherited from the include chain</li>
 *   <li><strong>Local Properties:</strong> {@code localTags}, {@code localAttributes} -
 *       These are NOT inherited via include, only from the resource itself</li>
 *   <li><strong>Type Field:</strong> Never inherited, defines the resource type</li>
 *   <li><strong>Shape Name:</strong> Derived from include chain for ID templates</li>
 * </ul>
 */
public interface ResourceTypeData {

	/**
	 * The resource type identifier (e.g., "forgero:shape", "forgero:material").
	 * This field is NEVER inherited via include chain.
	 */
	OpenIdentifier type();

	/**
	 * The name of this resource.
	 */
	String name();

	/**
	 * List of resource identifiers to include (inherit from).
	 * Properties from included resources are merged into this resource.
	 */
	@Nullable
	List<OpenIdentifier> include();

	/**
	 * Tags that are inherited via include chain.
	 * Tags from included resources are merged with this resource's tags.
	 */
	@Nullable
	List<OpenIdentifier> tags();

	/**
	 * Tags that are LOCAL to this resource and NOT inherited via include.
	 * These tags are only present on this specific resource.
	 *
	 * <p>Use case: Marking specific resources for generation filtering without
	 * affecting resources that extend them via include.</p>
	 */
	@Nullable
	List<OpenIdentifier> localTags();

	/**
	 * Host data mapping this resource to physical items.
	 */
	@Nullable
	HostData host();

	/**
	 * Attributes that are inherited via include chain.
	 * Attributes from included resources are merged with this resource's attributes.
	 */
	@Nullable
	List<AttributeData> attributes();

	/**
	 * Attributes that are LOCAL to this resource and NOT inherited via include.
	 * These attributes are only present on this specific resource.
	 *
	 * <p>Use case: Adding bonuses specific to a schematic/cast variant without
	 * affecting the base shape or other variants.</p>
	 */
	@Nullable
	List<AttributeData> localAttributes();

	/**
	 * Additional properties as raw JSON elements.
	 */
	@Nullable
	Map<String, JsonElement> properties();

	/**
	 * Derives the shape name for use in ID templates.
	 *
	 * <p>The shape name is derived as follows:</p>
	 * <ul>
	 *   <li>If this resource has no includes, returns the resource's own name</li>
	 *   <li>If this resource has includes, returns the name portion of the first include</li>
	 * </ul>
	 *
	 * <p>Example:</p>
	 * <pre>
	 * // pickaxe_head_schematic includes "forgero:shapes/pickaxe_head"
	 * // shapeName() returns "pickaxe_head" (from first include)
	 *
	 * // Template: "forgero:{material.name}-{shape.shape_name}"
	 * // Result: "forgero:iron-pickaxe_head" (NOT "iron-pickaxe_head_schematic")
	 * </pre>
	 */
	default String shapeName() {
		if (include() == null || include().isEmpty()) {
			return name();
		}
		// Extract the name from the first include's identifier
		// E.g., "forgero:shapes/pickaxe_head" -> "pickaxe_head"
		return include().get(0).name();
	}
}
