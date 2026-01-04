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
 * DTO for `forgero:extension` type data files.
 * Extensions contribute properties to existing definitions without modifying the base file.
 *
 * <p>Extensions are merged into their target definition during the loading pipeline,
 * before include resolution. Multiple extensions can target the same definition
 * and are applied in priority order (lowest first).</p>
 *
 * <h3>Merge Semantics:</h3>
 * <ul>
 *   <li><strong>Tags:</strong> Union (extension tags added to target)</li>
 *   <li><strong>Attributes:</strong> Concatenate (extension attributes appended)</li>
 *   <li><strong>Properties:</strong> Deep merge (objects merged recursively, arrays concatenated)</li>
 *   <li><strong>Upgrades:</strong> Concatenate with override (duplicate IDs: extension wins with warning)</li>
 * </ul>
 *
 * <h3>Example Use Case:</h3>
 * <p>A mod can add tool properties to iron material without touching the base iron.json:</p>
 * <pre>{@code
 * // iron-tool-extension.json
 * {
 *   "type": "forgero:extension",
 *   "target": "forgero:materials/iron",
 *   "priority": 0,
 *   "tags": ["forgero:tool_material"],
 *   "attributes": [...]
 * }
 * }</pre>
 *
 * @param type       The type identifier, always "forgero:extension".
 * @param target     The identifier of the definition to extend.
 * @param priority   The merge priority. Lower values are applied first (default: 0).
 * @param tags       Optional list of tags to add to the target.
 * @param attributes Optional list of attributes to add to the target.
 * @param properties Optional map of properties to merge into the target.
 * @param upgrades   Optional list of upgrade slots to add to the target (for templates).
 */
public record ExtensionData(
		OpenIdentifier type,
		OpenIdentifier target,
		int priority,
		@Nullable List<OpenIdentifier> tags,
		@Nullable List<AttributeData> attributes,
		@Nullable Map<String, JsonElement> properties,
		@Nullable List<UpgradeSlotData> upgrades
) implements DefinitionData, ResourceTypeData {

	/**
	 * The type identifier for extension resources.
	 */
	public static final String TYPE_ID = "extension";

	/**
	 * Default priority for extensions when not specified.
	 */
	public static final int DEFAULT_PRIORITY = 0;

	/**
	 * Creates an ExtensionData with default priority and no upgrades.
	 */
	public ExtensionData(
			OpenIdentifier type,
			OpenIdentifier target,
			@Nullable List<OpenIdentifier> tags,
			@Nullable List<AttributeData> attributes,
			@Nullable Map<String, JsonElement> properties
	) {
		this(type, target, DEFAULT_PRIORITY, tags, attributes, properties, null);
	}

	/**
	 * Creates an ExtensionData with specified priority but no upgrades.
	 * Backward compatibility constructor for existing tests and usage.
	 */
	public ExtensionData(
			OpenIdentifier type,
			OpenIdentifier target,
			int priority,
			@Nullable List<OpenIdentifier> tags,
			@Nullable List<AttributeData> attributes,
			@Nullable Map<String, JsonElement> properties
	) {
		this(type, target, priority, tags, attributes, properties, null);
	}

	/**
	 * Creates an ExtensionData with default priority.
	 */
	public ExtensionData(
			OpenIdentifier type,
			OpenIdentifier target,
			@Nullable List<OpenIdentifier> tags,
			@Nullable List<AttributeData> attributes,
			@Nullable Map<String, JsonElement> properties,
			@Nullable List<UpgradeSlotData> upgrades
	) {
		this(type, target, DEFAULT_PRIORITY, tags, attributes, properties, upgrades);
	}

	// ResourceTypeData implementation
	// Extensions use target as the name - they contribute to the target's definition

	@Override
	public String name() {
		return target.name();
	}

	/**
	 * Extensions do not use the include mechanism.
	 * They contribute directly to their target.
	 */
	@Override
	public List<OpenIdentifier> include() {
		return List.of();
	}

	/**
	 * Extensions do not have local tags.
	 * All extension tags are merged into the target.
	 */
	@Override
	public List<OpenIdentifier> localTags() {
		return List.of();
	}

	/**
	 * Extensions do not have host mappings.
	 * The target definition defines the host.
	 */
	@Override
	public HostData host() {
		return null;
	}

	/**
	 * Extensions do not have local attributes.
	 * All extension attributes are merged into the target.
	 */
	@Override
	public List<AttributeData> localAttributes() {
		return List.of();
	}
}
