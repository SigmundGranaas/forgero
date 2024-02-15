package com.sigmundgranaas.forgero.core.condition;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class ConditionContainer implements Conditional<ConditionContainer>, PropertyContainer {
	private final List<PropertyContainer> conditions;

	public ConditionContainer(List<PropertyContainer> conditions) {
		this.conditions = conditions;
	}

	public ConditionContainer() {
		this.conditions = new ArrayList<>();
	}

	@Override
	public List<PropertyContainer> localConditions() {
		return conditions;
	}


	@Override
	public ConditionContainer applyCondition(PropertyContainer container) {
		var copy = new ArrayList<>(localConditions());
		copy.add(container);
		return new ConditionContainer(copy);
	}

	@Override
	public ConditionContainer removeCondition(String identifier) {
		var copy = Conditional.removeConditions(conditions, identifier);
		return new ConditionContainer(copy);
	}

	@Override
	public @NotNull
	List<Property> getProperties() {
		return localConditions().stream()
				.map(PropertyContainer::getRootProperties)
				.flatMap(List::stream)
				.toList();
	}

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return localConditions().stream()
				.map(cond -> cond.getRootProperties(target, context))
				.flatMap(List::stream)
				.toList();
	}
}
