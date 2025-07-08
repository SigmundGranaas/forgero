package com.sigmundgranaas.forgero.core.data.definition;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.UpgradeSlotData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stage 3 Output: A container for definitions of generated, composite items.
 */
public record GeneratedState(
		Map<OpenIdentifier, GeneratedPart> parts,
		Map<OpenIdentifier, GeneratedEquipment> equipment
) {
	// Individual generated records are defined below for clarity and encapsulation.

	/**
	 * A GeneratedPart is a concrete combination of one Material and one Shape (via a PartTemplate).
	 */
	public record GeneratedPart(
			OpenIdentifier id,
			String name,
			Set<OpenIdentifier> tags,
			OpenIdentifier materialId,
			OpenIdentifier shapeId, // Assuming shape is derived from PartTemplate for now
			List<UpgradeSlotData> upgrades,
			List<AttributeData> attributes,
			List<FeatureData> features
	) {}

	/**
	 * A GeneratedEquipment is a concrete combination of one EquipmentTemplate and specific Parts.
	 */
	public record GeneratedEquipment(
			OpenIdentifier id,
			String name,
			Set<OpenIdentifier> tags,
			Map<String, OpenIdentifier> structure,
			List<UpgradeSlotData> upgrades
	) {}
}
