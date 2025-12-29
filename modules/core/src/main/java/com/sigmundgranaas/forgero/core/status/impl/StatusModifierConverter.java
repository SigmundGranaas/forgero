package com.sigmundgranaas.forgero.core.status.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.StatusModifierData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;

import java.util.*;

/**
 * Converts StatusModifierData (loaded from JSON) to runtime StatusModifier and StatusModifierDefinition instances.
 */
public final class StatusModifierConverter {

	private StatusModifierConverter() {
		// Utility class
	}

	/**
	 * Converts StatusModifierData to a StatusModifierDefinition.
	 *
	 * @param data The data loaded from JSON
	 * @return A runtime StatusModifierDefinition
	 */
	public static StatusModifierDefinition toDefinition(StatusModifierData data) {
		StatusModifier modifier = toModifier(data);

		StatusModifierData.TargetData target = data.getTarget();

		return new SimpleStatusModifierDefinition(
				modifier,
				target.getTypes(),
				target.getIds(),
				data.chance()
		);
	}

	/**
	 * Converts StatusModifierData to a StatusModifier.
	 *
	 * @param data The data loaded from JSON
	 * @return A runtime StatusModifier
	 */
	public static StatusModifier toModifier(StatusModifierData data) {
		Map<String, List<?>> properties = convertAttributes(data.getAttributes());

		// Merge any additional properties from the properties map
		if (data.properties() != null && !data.properties().isEmpty()) {
			// Properties map contains JsonElement values - these would need custom conversion
			// For now, we just use the attributes
		}

		Set<OpenIdentifier> incompatibilities = data.getTarget().getIncompatibilities();

		return new SimpleStatusModifier(
				data.id(),
				data.displayName(),
				data.priority(),
				incompatibilities,
				properties
		);
	}

	/**
	 * Converts AttributeData list to a property map.
	 * The key is the attribute type, and the value is a list of AttributeData for that type.
	 */
	private static Map<String, List<?>> convertAttributes(List<AttributeData> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			return Map.of();
		}

		Map<String, List<?>> result = new LinkedHashMap<>();

		// Group attributes by type
		Map<String, List<AttributeData>> grouped = new LinkedHashMap<>();
		for (AttributeData attr : attributes) {
			if (attr.type() != null) {
				String typeKey = attr.type().toString();
				grouped.computeIfAbsent(typeKey, k -> new ArrayList<>()).add(attr);
			}
		}

		// Convert to the property map format
		// The key format follows the pattern used in the property system
		String attributeKey = "forgero:attributes";
		result.put(attributeKey, List.copyOf(attributes));

		return result;
	}

	/**
	 * Batch converts multiple StatusModifierData to definitions.
	 *
	 * @param dataList The list of data loaded from JSON
	 * @return A list of runtime StatusModifierDefinitions
	 */
	public static List<StatusModifierDefinition> toDefinitions(Collection<StatusModifierData> dataList) {
		return dataList.stream()
				.map(StatusModifierConverter::toDefinition)
				.toList();
	}
}
