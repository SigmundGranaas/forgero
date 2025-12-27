package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Common interface for all definition types in the data loading pipeline.
 *
 * <p>This interface provides type-safe handling of definitions throughout the pipeline,
 * eliminating the need for instanceof checks and type erasure.</p>
 *
 * <h3>Implementing Types:</h3>
 * <ul>
 *   <li>{@link ResourceData} - Unified resource type (materials, shapes, schematics, casts, static parts)</li>
 *   <li>{@link PartTemplateData} - Part generation templates</li>
 *   <li>{@link EquipmentTemplateData} - Equipment generation templates</li>
 *   <li>{@link ExtensionData} - Extension contributions to existing definitions</li>
 * </ul>
 *
 * <h3>Common Operations:</h3>
 * <ul>
 *   <li>{@link #type()} and {@link #name()} - Identity information</li>
 *   <li>{@link #include()}, {@link #tags()}, {@link #attributes()}, {@link #properties()} - Mergeable fields</li>
 *   <li>{@link #withMergedExtension} - Creates a copy with merged extension fields</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This interface is intentionally not sealed to support cross-package
 * implementations in non-JPMS environments. All known implementations are in this package
 * or the {@code template} subpackage.</p>
 */
public interface DefinitionData {

	/**
	 * The definition type identifier (e.g., "forgero:material", "forgero:part_template").
	 */
	OpenIdentifier type();

	/**
	 * The unique name of this definition.
	 */
	String name();

	/**
	 * List of definition identifiers to include (inherit from).
	 * May be null if no includes are specified.
	 */
	@Nullable
	List<OpenIdentifier> include();

	/**
	 * Tags associated with this definition.
	 * These are inherited via the include chain.
	 */
	@Nullable
	List<OpenIdentifier> tags();

	/**
	 * Attributes associated with this definition.
	 * These are inherited via the include chain.
	 */
	@Nullable
	List<AttributeData> attributes();

	/**
	 * Additional properties as raw JSON elements.
	 */
	@Nullable
	Map<String, JsonElement> properties();

	/**
	 * Tags that are LOCAL to this definition (NOT inherited via include chain).
	 *
	 * <p>Only applicable to resource types (materials, shapes, etc.).
	 * Templates return null by default.</p>
	 */
	@Nullable
	default List<OpenIdentifier> localTags() {
		return null;
	}

	/**
	 * Attributes that are LOCAL to this definition (NOT inherited via include chain).
	 *
	 * <p>Only applicable to resource types (materials, shapes, etc.).
	 * Templates return null by default.</p>
	 */
	@Nullable
	default List<AttributeData> localAttributes() {
		return null;
	}

	/**
	 * Host data for mapping to a platform-specific item.
	 *
	 * <p>Only applicable to resource types (materials, shapes, etc.).
	 * Templates use host_template instead and return null here.</p>
	 */
	@Nullable
	default HostData host() {
		return null;
	}

	/**
	 * Creates a copy of this definition with merged extension fields.
	 *
	 * <p>This method is used by the extension merging process to apply
	 * extensions to their target definitions polymorphically, without
	 * requiring instanceof checks.</p>
	 *
	 * <p>Default implementation returns {@code this}, meaning types that
	 * don't support extension merging (like templates) are unaffected.</p>
	 *
	 * @param mergedTags       The combined tags after merging
	 * @param mergedAttributes The combined attributes after merging
	 * @param mergedProperties The combined properties after merging
	 * @return A new definition with merged fields, or {@code this} if merging is not supported
	 */
	default DefinitionData withMergedExtension(
			List<OpenIdentifier> mergedTags,
			List<AttributeData> mergedAttributes,
			Map<String, JsonElement> mergedProperties
	) {
		return this;
	}
}
