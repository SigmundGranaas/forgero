package com.sigmundgranaas.forgero.core.property;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public interface PropertyContainer extends Comparable<Object> {
	Function<PropertyContainer, Float> ATTACK_DAMAGE = (PropertyContainer container) -> Property.stream(container.getProperties()).applyAttribute(AttackDamage.KEY);
	Function<PropertyContainer, Integer> RARITY = (PropertyContainer container) -> (int) Property.stream(container.getProperties()).applyAttribute(Rarity.KEY);

	static PropertyContainer of(List<Property> properties) {
		return new SimpleContainer(properties);
	}

	@Deprecated
	@NotNull
	default List<Property> getProperties(Matchable target, MatchContext context) {
		return applyProperty(target, context);
	}

	@NotNull
	default List<Property> getProperties() {
		return getRootProperties();
	}

	@NotNull
	default PropertyStream stream() {
		return Property.stream(getRootProperties());
	}

	@NotNull
	default PropertyStream stream(Matchable target, MatchContext context) {
		return Property.stream(applyProperty(target, context));
	}


	@NotNull
	default List<Property> getRootProperties() {
		return Collections.emptyList();
	}

	@NotNull
	List<Property> getRootProperties(Matchable target, MatchContext context);

	@NotNull
	default List<Property> applyProperty(Matchable target, MatchContext context) {
		return getRootProperties().stream()
				.filter(property -> property.applyCondition(target, context))
				.toList();
	}

	@Override
	default int compareTo(@NotNull Object o) {
		if (o == this) {
			return 0;
		}
		if (o instanceof PropertyContainer container) {
			return RARITY.apply(this) - RARITY.apply(container);
		}
		return 0;
	}
}
