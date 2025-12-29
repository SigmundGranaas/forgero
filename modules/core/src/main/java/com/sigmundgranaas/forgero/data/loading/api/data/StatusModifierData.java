package com.sigmundgranaas.forgero.data.loading.api.data;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Transfer Object for status modifier definitions loaded from JSON.
 * Status modifiers are enchantment-like properties that can be applied to components
 * (e.g., "sharp", "durable", "broken", "unbreakable").
 *
 * <h3>JSON Format:</h3>
 * <pre>{@code
 * {
 *   "id": "forgero:sharp",
 *   "display_name": "Sharp",
 *   "priority": 5,
 *   "target": {
 *     "types": ["forgero:tool", "forgero:weapon"],
 *     "ids": [],
 *     "incompatibilities": ["forgero:blunt"]
 *   },
 *   "chance": 0.04,
 *   "attributes": [
 *     {"id": "forgero:sharp-damage", "type": "forgero:attack_damage", "computation": {"add": 3}}
 *   ],
 *   "properties": {
 *     "forgero:tooltip": [{"text": "Sharper edge"}]
 *   }
 * }
 * }</pre>
 *
 * @param id               Unique identifier for this modifier (e.g., "forgero:sharp")
 * @param displayName      Human-readable name for UI display
 * @param priority         Priority for resolution ordering (higher = applied first)
 * @param target           Targeting rules (types, ids, incompatibilities)
 * @param chance           Probability of natural occurrence (0.0-1.0)
 * @param attributes       Attributes this modifier contributes
 * @param properties       Additional extensible properties
 */
public record StatusModifierData(
		OpenIdentifier id,
		String displayName,
		int priority,
		@Nullable TargetData target,
		float chance,
		@Nullable List<AttributeData> attributes,
		@Nullable Map<String, JsonElement> properties
) {

	/**
	 * Targeting rules for status modifier applicability.
	 *
	 * @param types            Component types this modifier can apply to
	 * @param ids              Specific component IDs this modifier can apply to
	 * @param incompatibilities Modifier IDs that cannot coexist with this modifier
	 */
	public record TargetData(
			@Nullable Set<OpenIdentifier> types,
			@Nullable Set<OpenIdentifier> ids,
			@Nullable Set<OpenIdentifier> incompatibilities
	) {
		public static TargetData empty() {
			return new TargetData(Set.of(), Set.of(), Set.of());
		}

		/**
		 * @return Non-null set of target types
		 */
		public Set<OpenIdentifier> getTypes() {
			return types != null ? types : Set.of();
		}

		/**
		 * @return Non-null set of target IDs
		 */
		public Set<OpenIdentifier> getIds() {
			return ids != null ? ids : Set.of();
		}

		/**
		 * @return Non-null set of incompatibilities
		 */
		public Set<OpenIdentifier> getIncompatibilities() {
			return incompatibilities != null ? incompatibilities : Set.of();
		}
	}

	/**
	 * Compact constructor with validation.
	 */
	public StatusModifierData {
		if (id == null) {
			throw new IllegalArgumentException("id cannot be null");
		}
		if (displayName == null || displayName.isBlank()) {
			displayName = id.name();
		}
		if (chance < 0 || chance > 1) {
			throw new IllegalArgumentException("chance must be between 0.0 and 1.0, got: " + chance);
		}
	}

	/**
	 * Creates a minimal StatusModifierData with just an ID.
	 */
	public static StatusModifierData minimal(OpenIdentifier id) {
		return new StatusModifierData(id, id.name(), 0, null, 0f, null, null);
	}

	/**
	 * Creates a StatusModifierData with common fields.
	 */
	public static StatusModifierData of(
			OpenIdentifier id,
			String displayName,
			int priority,
			Set<OpenIdentifier> targetTypes,
			Set<OpenIdentifier> incompatibilities,
			float chance,
			List<AttributeData> attributes
	) {
		return new StatusModifierData(
				id,
				displayName,
				priority,
				new TargetData(targetTypes, Set.of(), incompatibilities),
				chance,
				attributes,
				null
		);
	}

	/**
	 * @return Non-null target data
	 */
	public TargetData getTarget() {
		return target != null ? target : TargetData.empty();
	}

	/**
	 * @return Non-null list of attributes
	 */
	public List<AttributeData> getAttributes() {
		return attributes != null ? attributes : List.of();
	}

	/**
	 * @return Non-null properties map
	 */
	public Map<String, JsonElement> getProperties() {
		return properties != null ? properties : Map.of();
	}
}
