package com.sigmundgranaas.forgero.data.loading.api.data.attribute;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Optional;

/**
 * Data transfer object for attribute definitions in JSON.
 *
 * <p>Attributes can specify a {@link #context()} to indicate how they participate
 * in composition (e.g., part-composite for shape+material composition).</p>
 */
public interface AttributeData {
	Optional<OpenIdentifier> id();
	OpenIdentifier type();
	ComputationData computation();

	/**
	 * The composition context for this attribute.
	 *
	 * <p>Empty means default behavior (no special composition handling).</p>
	 *
	 * @return The context identifier, or empty for default
	 */
	Optional<OpenIdentifier> context();

	Optional<Condition> condition();
}
