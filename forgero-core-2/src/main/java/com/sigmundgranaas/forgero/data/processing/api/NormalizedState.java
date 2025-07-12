package com.sigmundgranaas.forgero.data.processing.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.template.HostTemplateData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stage 2 Output: A container for all fully self-contained, normalized definitions.
 * All 'include' directives have been resolved and all property sources have been
 * consolidated into a single properties map. These DTOs do not implement PropertyContainer.
 */
public record NormalizedState(
		Map<OpenIdentifier, NormalizedMaterial> materials,
		Map<OpenIdentifier, NormalizedShape> shapes,
		Map<OpenIdentifier, NormalizedSchematic> schematics,
		Map<OpenIdentifier, NormalizedPartTemplate> partTemplates,
		Map<OpenIdentifier, NormalizedEquipmentTemplate> equipmentTemplates,
		Map<OpenIdentifier, NormalizedStaticPart> staticParts
) {

	public record NormalizedMaterial(OpenIdentifier id, String name, Set<OpenIdentifier> tags,
									 @Nullable HostData host,
									 @Nullable Map<String, List<PropertyData>> properties) {
	}

	public record NormalizedShape(OpenIdentifier id, String name, Set<OpenIdentifier> tags,
								  @Nullable HostData host,
								  @Nullable Map<String, List<PropertyData>> properties) {
	}

	public record NormalizedSchematic(OpenIdentifier id, String name, OpenIdentifier target,
									  String craftingMaterial, Set<OpenIdentifier> tags,
									  @Nullable HostData host,
									  @Nullable Map<String, List<PropertyData>> properties) {
	}

	public record NormalizedStaticPart(OpenIdentifier id, String name, Set<OpenIdentifier> tags,
									   @Nullable HostData host,
									   @Nullable List<UpgradeSlotData> upgrades,
									   @Nullable Map<String, List<PropertyData>> properties) {
	}

	public record NormalizedPartTemplate(OpenIdentifier id, String name, Set<OpenIdentifier> tags,
										 PartTemplateStructureData structure,
										 @Nullable HostTemplateData host_template,
										 @Nullable List<UpgradeSlotData> upgrades,
										 @Nullable Map<String, List<PropertyData>> properties) {
	}

	public record NormalizedEquipmentTemplate(OpenIdentifier id, String name, Set<OpenIdentifier> tags,
											  EquipmentTemplateStructureData structure,
											  @Nullable HostTemplateData host_template,
											  @Nullable List<UpgradeSlotData> upgrades,
											  @Nullable Map<String, List<PropertyData>> properties) {
	}
}
