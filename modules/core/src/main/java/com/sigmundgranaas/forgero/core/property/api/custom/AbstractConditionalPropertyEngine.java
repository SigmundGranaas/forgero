package com.sigmundgranaas.forgero.core.property.api.custom;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.property.api.CompilerPass;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Base {@link CompilerPass} for a {@link ConditionalProperty}. It compiles the list of
 * statically-valid properties of its type: gathered across the tree, slot-filtered, and
 * filtered by static (structural) conditions. Dynamic conditions are NOT evaluated here —
 * properties carrying them are included as data for the game layer to filter at runtime.
 *
 * @param <P> The concrete ConditionalProperty type this pass handles.
 */
public abstract class AbstractConditionalPropertyEngine<P extends ConditionalProperty> implements CompilerPass<List<P>> {

	private final ResolutionKey<List<P>> key;
	private final PropertyKey<P> propertyKey;

	protected AbstractConditionalPropertyEngine(ResolutionKey<List<P>> key, PropertyKey<P> propertyKey) {
		this.key = key;
		this.propertyKey = propertyKey;
	}

	@Override
	public ResolutionKey<List<P>> key() {
		return key;
	}

	@Override
	public List<P> compile(Stream<Component> components) {
		List<Component> componentList = components.toList();
		Component root = componentList.isEmpty() ? null : componentList.get(0);

		return componentList.stream()
				.flatMap(component -> {
					ResolutionContext resCtx = new ResolutionContext(component, root);

					Stream<P> componentProps = component.properties(propertyKey).stream();

					// Slots may control which properties contribute to the parent.
					Optional<Slot> slot = resCtx.getSlot();
					Stream<P> filteredProps = slot
							.map(s -> s.filterProperties(propertyKey, componentProps))
							.orElse(componentProps);

					// Static (structural) conditions are resolved now; dynamic ones ride through.
					return filteredProps.filter(prop -> prop.getCondition()
							.map(Condition::staticConditions)
							.map(resCtx::test)
							.orElse(true));
				})
				.toList();
	}
}
