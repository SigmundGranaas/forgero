package com.sigmundgranaas.forgero.core.soul;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public class SoulStatToAttributes implements PropertyContainer {

	private final StatTracker tracker;

	public SoulStatToAttributes(StatTracker tracker) {
		this.tracker = tracker;
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return Stream.of(mobAttributes(), blockAttributes()).flatMap(List::stream).toList();
	}

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return getRootProperties();
	}

	private List<Property> blockAttributes() {
		return tracker.blocks().toMap().entrySet().stream()
				.map(entry -> blockPercentage(entry.getValue(), entry.getKey()))
				.flatMap(Optional::stream)
				.toList();
	}

	private Optional<Property> blockPercentage(int amount, String targetBlock) {
		int percent = amount / 1000;
		if (percent == 0) {
			return Optional.empty();
		}
		return Optional.of(new AttributeBuilder("MINING_SPEED")
				.applyOrder(CalculationOrder.MIDDLE)
				.applyOperation(NumericOperation.MULTIPLICATION)
				.applyValue(1f + (float) percent / 100)
				.applyPredicate(Matchable.DEFAULT_TRUE)
				// Todo! Implement proper predicates here
				//target.isApplicable(Set.of(targetBlock), TargetTypes.BLOCK))
				.build());
	}

	private Optional<Property> mobPercentage(int amount, String targetBlock) {
		int percent = amount / 100;
		if (percent == 0) {
			return Optional.empty();
		}
		return Optional.of(new AttributeBuilder("ATTACK_DAMAGE")
				.applyOrder(CalculationOrder.MIDDLE)
				.applyOperation(NumericOperation.MULTIPLICATION)
				.applyValue(1f + (float) percent / 100)
				.applyPredicate(Matchable.DEFAULT_TRUE)
				// Todo! Implement proper predicates here
				//(target) ->
				//target.isApplicable(Set.of(targetBlock), TargetTypes.ENTITY))
				.build());
	}

	private List<Property> mobAttributes() {
		return tracker.mobs().toMap().entrySet().stream()
				.map(entry -> mobPercentage(entry.getValue(), entry.getKey()))
				.flatMap(Optional::stream)
				.toList();
	}
}
