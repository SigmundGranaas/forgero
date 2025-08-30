package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.resource.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ConditionData;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LootCondition extends NamedCondition {
	private final Set<Type> types;
	private final Set<String> ids;
	private final float chance;
	private final int priority;

	public LootCondition(String name, String nameSpace, List<Property> propertyList, Set<Type> types, Set<String> ids, float chance, int priority, Matchable target) {
		super(name, nameSpace, propertyList, target);
		this.types = types;
		this.ids = ids;
		this.chance = chance;
		this.priority = priority;
	}

	public static Optional<LootCondition> of(ConditionData data) {
		var types = data.getTarget().getTypes().stream().map(Type::of).collect(Collectors.toSet());
		var elements = data.getId().split(":");
		var name = elements[1];
		var namespace = elements[0];
		Set<String> ids = data.getTarget().getIds();
		Matchable target = (match, context) -> {
			if (match instanceof com.sigmundgranaas.forgero.core.state.State state) {
				if (ids.contains(state.identifier())) {
					return true;
				} else if (types.stream().anyMatch(type -> state.type().test(type))) {
					return true;
				}
			}
			return false;
		};
		return Optional.of(new LootCondition(name, namespace, PropertyBuilder.createPropertyListFromPOJO(data.getProperties()), types, ids, data.getChance(), data.getPriority(), target));
	}

	public boolean isApplicable(Conditional<?> conditional) {
		if (conditional instanceof State state) {
			if (ids.contains(state.identifier())) {
				return true;
			} else if (types.stream().anyMatch(type -> state.type().test(type))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean matches(State state) {
		if (ids.contains(state.identifier())) {
			return true;
		} else if (types.stream().anyMatch(type -> state.type().test(type))) {
			return true;
		}
		return false;
	}

	public int getPriority() {
		return priority;
	}

	public float getChance() {
		return chance;
	}
}
