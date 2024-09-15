package com.sigmundgranaas.forgero.core.property;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public interface PropertyContainer extends Comparable<Object> {
	PropertyContainer EMPTY = new SimpleContainer(Collections.emptyList());
	Function<PropertyContainer, Float> ATTACK_DAMAGE = (PropertyContainer container) -> Property.stream(container.getProperties()).applyAttribute(AttackDamage.KEY);
	Function<PropertyContainer, Integer> RARITY = (PropertyContainer container) -> (int) Property.stream(container.getProperties()).applyAttribute(Rarity.KEY);

	static PropertyContainer of(List<Property> properties) {
		return new SimpleContainer(properties);
	}
	
	static PropertyContainer of(Property properties) {
		return new SingleContainer(properties);
	}

	default PropertyContainer with(PropertyContainer container) {
		return new CompositeContainer(List.of(this, container));
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
		return Property.stream(getRootProperties(), Matchable.DEFAULT_TRUE, MatchContext.of());
	}

	@NotNull
	default PropertyStream stream(Matchable target, MatchContext context) {
		return Property.stream(getRootProperties(target, context), target, context);
	}

	@NotNull
	default PropertyStream stream(Matchable target) {
		return Property.stream(getRootProperties(target, MatchContext.of()));
	}


	default Property applySource(Property property) {
		if (property instanceof Attribute attribute) {
			return attribute.setSource(this);
		} else {
			return property;
		}
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
				.filter(property -> {
					if (property instanceof Feature && property.isDynamic()) {
							return true;
					}
					return property.applyCondition(target, context);
				})
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
