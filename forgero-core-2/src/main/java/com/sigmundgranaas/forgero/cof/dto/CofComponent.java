package com.sigmundgranaas.forgero.cof.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * A Data Transfer Object for a fully serialized Component instance.
 * It mirrors the structure of a PropertyContainer from data loading,
 * but represents a live object's state.
 *
 * @param id The unique identifier of the component.
 * @param componentType A string identifying the concrete Component implementation (e.g., "forgero:static_component").
 * @param tags The set of tags associated with the component.
 * @param attributes A list of attribute data DTOs representing the component's attributes.
 * @param features A list of feature data DTOs representing the component's features.
 * @param structure The serialized structure of the component, if any.
 * @param upgrades The serialized upgrades of the component, if any.
 * @param cofVersion The version of the COF schema used for this component.
 */
public record CofComponent(
		OpenIdentifier id,
		String componentType,
		@Nullable Set<OpenIdentifier> tags,
		@Nullable List<AttributeData> attributes,
		@Nullable List<FeatureData> features,
		@Nullable CofStructure structure,
		@Nullable CofUpgrades upgrades,
		@Nullable Integer cofVersion
) {
}
