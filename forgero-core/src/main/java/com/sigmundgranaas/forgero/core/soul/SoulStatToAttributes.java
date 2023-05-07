package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.property.*;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
				.applyCondition((target) ->
						target.isApplicable(Set.of(targetBlock), TargetTypes.BLOCK))
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
				.applyCondition((target) ->
						target.isApplicable(Set.of(targetBlock), TargetTypes.ENTITY))
				.build());
	}

	private List<Property> mobAttributes() {
		return tracker.mobs().toMap().entrySet().stream()
				.map(entry -> mobPercentage(entry.getValue(), entry.getKey()))
				.flatMap(Optional::stream)
				.toList();
	}
}
