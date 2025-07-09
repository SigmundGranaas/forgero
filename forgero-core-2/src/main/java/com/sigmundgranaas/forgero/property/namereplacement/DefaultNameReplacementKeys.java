package com.sigmundgranaas.forgero.property.namereplacement;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;

import java.util.Optional;

/**
 * Central registry for keys related to Name Replacement properties.
 * This class provides the type-safe {@link ResolutionKey} for requesting Name Replacement data
 * from the {@link com.sigmundgranaas.forgero.core.property.api.Resolver}.
 */
public class DefaultNameReplacementKeys {
	public static final OpenIdentifier NAME_REPLACEMENT_IDENTIFIER = new OpenIdentifier("forgero", "name_replacement");
	public static final ResolutionKey<Optional<String>> NAME_REPLACEMENT = new ResolutionKey<>(NAME_REPLACEMENT_IDENTIFIER);
}
