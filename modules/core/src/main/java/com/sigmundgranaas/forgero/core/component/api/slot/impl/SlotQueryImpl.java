package com.sigmundgranaas.forgero.core.component.api.slot.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotQuery;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Implementation of {@link SlotQuery} that builds and executes slot filters.
 * <p>
 * This implementation uses a list of predicates that are combined with AND logic.
 * Each filter method adds a new predicate to the list. When {@code execute()} is
 * called, all predicates are applied sequentially.
 *
 * @param <S> The type of slot being queried
 */
public class SlotQueryImpl<S extends Slot> implements SlotQuery<S> {

	private final List<S> baseSlots;
	private final List<Predicate<S>> filters;

	/**
	 * Creates a new slot query for the given slots.
	 *
	 * @param slots The base slots to query
	 */
	public SlotQueryImpl(List<S> slots) {
		this.baseSlots = List.copyOf(slots);
		this.filters = new ArrayList<>();
	}

	/**
	 * Private constructor for creating query instances with filters.
	 *
	 * @param baseSlots The base slots
	 * @param filters   The filters to apply
	 */
	private SlotQueryImpl(List<S> baseSlots, List<Predicate<S>> filters) {
		this.baseSlots = baseSlots;
		this.filters = new ArrayList<>(filters);
	}

	@Override
	public SlotQuery<S> ofType(OpenIdentifier type) {
		return matching(slot -> slot.type().equals(type));
	}

	@Override
	public SlotQuery<S> ofAnyType(OpenIdentifier... types) {
		Set<OpenIdentifier> typeSet = Set.of(types);
		return matching(slot -> typeSet.contains(slot.type()));
	}

	@Override
	public SlotQuery<S> matching(Predicate<S> predicate) {
		filters.add(predicate);
		return this;
	}

	@Override
	public SlotQuery<S> onlyEmpty() {
		return matching(slot -> {
			if (slot instanceof UpgradeSlot upgradeSlot) {
				return upgradeSlot.isEmpty();
			}
			return false; // Structure slots are always filled
		});
	}

	@Override
	public SlotQuery<S> onlyFilled() {
		return matching(slot -> {
			if (slot instanceof UpgradeSlot upgradeSlot) {
				return upgradeSlot.isFilled();
			}
			return true; // Structure slots are always filled
		});
	}

	@Override
	public SlotQuery<S> compatibleWith(Component component) {
		return matching(slot -> {
			if (slot instanceof UpgradeSlot upgradeSlot) {
				return upgradeSlot.validator().test(component);
			}
			return false;
		});
	}

	@Override
	public SlotQuery<S> descriptionMatches(String descriptionPattern) {
		return matching(slot -> slot.description().matches(descriptionPattern));
	}

	@Override
	public List<S> execute() {
		Stream<S> stream = baseSlots.stream();
		for (Predicate<S> filter : filters) {
			stream = stream.filter(filter);
		}
		return stream.toList();
	}

	@Override
	public Optional<S> first() {
		Stream<S> stream = baseSlots.stream();
		for (Predicate<S> filter : filters) {
			stream = stream.filter(filter);
		}
		return stream.findFirst();
	}

	@Override
	public boolean exists() {
		return first().isPresent();
	}

	@Override
	public int count() {
		return execute().size();
	}
}
