package com.sigmundgranaas.forgero.property.namereplacement;

import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.Optional;

import static com.sigmundgranaas.forgero.property.namereplacement.DefaultNameReplacementKeys.NAME_REPLACEMENT;

/**
 * The expert engine for resolving Name Replacement properties.
 * <p>
 * Intermediate Baked Type {@code <B>}: {@link OptimizedBakedResult}
 * Final Result Type {@code <R>}: {@code Optional<String>}
 */
public class NameReplacementEngine extends AbstractConditionalPropertyEngine<NameReplacementProperty, Optional<String>> {
	public static final ResolutionKey<Optional<String>> KEY = NAME_REPLACEMENT;

	public NameReplacementEngine() {
		super(KEY, NameReplacementProperty.class);
	}

	@Override
	public Optional<String> apply(OptimizedBakedResult<NameReplacementProperty> baked, DynamicContext context) {
		return baked.stream(context)
				.map(NameReplacementProperty::to)
				.findFirst(); // Only the first valid replacement wins
	}
}
