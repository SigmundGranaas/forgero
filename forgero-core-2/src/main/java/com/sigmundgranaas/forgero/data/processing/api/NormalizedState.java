package com.sigmundgranaas.forgero.data.processing.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stage 2 Output: A container for all fully self-contained, normalized definitions.
 * All 'include' directives have been resolved.
 */
public record NormalizedState(
		Map<OpenIdentifier, NormalizedMaterial> materials,
		Map<OpenIdentifier, NormalizedShape> shapes,
		Map<OpenIdentifier, NormalizedSchematic> schematics,
		Map<OpenIdentifier, NormalizedPartTemplate> partTemplates,
		Map<OpenIdentifier, NormalizedEquipmentTemplate> equipmentTemplates,
		Map<OpenIdentifier, NormalizedStaticPart> staticParts
) {
	// Individual normalized records are defined below for clarity and encapsulation.

	public record NormalizedMaterial(OpenIdentifier id, String name, Set<OpenIdentifier> tags, List<AttributeData> attributes, List<FeatureData> features) {}

	public record NormalizedShape(OpenIdentifier id, String name, Set<OpenIdentifier> tags, List<AttributeData> attributes, List<FeatureData> features) {}

	public record NormalizedSchematic(OpenIdentifier id, String name, OpenIdentifier targetShape, String craftingMaterial, Set<OpenIdentifier> tags) {}

	public record NormalizedStaticPart(OpenIdentifier id, String name, Set<OpenIdentifier> tags, List<AttributeData> attributes, List<FeatureData> features, List<UpgradeSlotData> upgrades) {}

	public record NormalizedPartTemplate(OpenIdentifier id, String name, Set<OpenIdentifier> tags, PartTemplateStructureData structure, List<UpgradeSlotData> upgrades) {}

	public record NormalizedEquipmentTemplate(OpenIdentifier id, String name, Set<OpenIdentifier> tags, EquipmentTemplateStructureData structure, List<UpgradeSlotData> upgrades) {}
}
