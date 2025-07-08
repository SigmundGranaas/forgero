package com.sigmundgranaas.forgero.data.dto.attribute;

import com.sigmundgranaas.forgero.data.dto.condition.ConditionData;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

/**
 * An interface representing an intermediate representation of an Attribute property from a JSON file.
 * This contract defines the common fields for all attribute data DTOs.
 */
public interface AttributeData {
	/**
	 * @return A unique identifier for this attribute instance, used for overriding.
	 */
	OpenIdentifier id();

	/**
	 * @return The type of attribute (e.g., "forgero:durability").
	 */
	OpenIdentifier type();

	/**
	 * @return The computation logic for this attribute.
	 */
	ComputationData computation();

	/**
	 * @return An optional condition for when this attribute is active.
	 */
	@Nullable
	ConditionData condition();

	/**
	 * @return An optional key for pairing with other attributes to form a composite property.
	 */
	@Nullable
	OpenIdentifier composite();
}
