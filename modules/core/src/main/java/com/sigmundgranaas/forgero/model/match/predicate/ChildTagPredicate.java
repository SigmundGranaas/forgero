package com.sigmundgranaas.forgero.model.match.predicate;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.match.Predicate;

/**
 * A predicate that tests if the current component being resolved ('aComponent' in the context)
 * has a specific tag. This is used in slot renderers to apply different models based on the
 * type of component being inserted into the slot.
 */
public record ChildTagPredicate(OpenIdentifier tag) implements Predicate {
	@Override
	public boolean test(ModelResolutionContext context) {
		// 'aComponent' is the component for which the model is currently being resolved.
		// In the context of a slot, this will be the child component.
		return context.aComponent().getTags().contains(tag);
	}
}
