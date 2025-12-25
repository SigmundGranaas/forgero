package com.sigmundgranaas.forgero.data.loading.api.data.template;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Common interface for template DTOs (PartTemplateData, EquipmentTemplateData).
 *
 * <p>Templates are different from regular resource types like MaterialData or ShapeData.
 * They define how to generate components by combining other resources, rather than
 * representing a single resource with inheritable properties.</p>
 *
 * <p>Key differences from ResourceTypeData:</p>
 * <ul>
 *   <li>Templates have host_template (HostTemplateData) instead of host (HostData)</li>
 *   <li>Templates don't support localTags or localAttributes (no inheritance semantics)</li>
 *   <li>Templates define structure and upgrade slots for generation</li>
 * </ul>
 */
public interface TemplateData {

	/**
	 * The template type identifier (e.g., "forgero:part_template", "forgero:equipment_template").
	 */
	OpenIdentifier type();

	/**
	 * The unique name of this template.
	 */
	String name();

	/**
	 * Optional list of template IDs to include (for template inheritance).
	 */
	@Nullable
	List<OpenIdentifier> include();

	/**
	 * Tags associated with generated components.
	 */
	@Nullable
	List<OpenIdentifier> tags();

	/**
	 * Template for mapping generated components to host platform items.
	 */
	@Nullable
	HostTemplateData host_template();

	/**
	 * Upgrade slots available on generated components.
	 */
	@Nullable
	List<UpgradeSlotData> upgrades();

	/**
	 * Attributes inherent to this template.
	 */
	@Nullable
	List<AttributeData> attributes();

	/**
	 * Additional properties as raw JSON elements.
	 */
	@Nullable
	Map<String, JsonElement> properties();
}
