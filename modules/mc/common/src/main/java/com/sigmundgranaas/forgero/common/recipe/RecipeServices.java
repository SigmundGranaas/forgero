package com.sigmundgranaas.forgero.common.recipe;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;

/**
 * Immutable service container for recipe crafting operations.
 * <p>
 * Injected into recipes via the serializer, replacing the static service locator pattern.
 * This enables proper dependency injection and testability.
 *
 * @param registry  The component registry for looking up base components
 * @param converter The converter for ItemStack ↔ Component transformations
 * @param mutater   The mutater for applying structural changes to components
 */
public record RecipeServices(
		ComponentRegistry registry,
		ComponentConverter converter,
		ComponentMutater mutater
) {
}
