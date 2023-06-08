package com.sigmundgranaas.forgero.core.state.upgrade.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.state.CopyAble;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.Context;

public class SlotContainer implements CopyAble<SlotContainer> {
	private final List<Slot> slots;

	public SlotContainer(List<Slot> slots) {
		this.slots = slots;
	}

	public static SlotContainer of(List<? extends Slot> slots) {
		return new SlotContainer(new ArrayList<>(slots));
	}

	public Slot set(Slot entry) {
		if (slots.size() - 1 >= entry.index()) {
			var lastSlot = slots.get(entry.index());
			slots.set(entry.index(), entry);
			return lastSlot;
		} else {
			slots.add(entry);
			return entry;
		}
	}

	public Optional<Slot> set(State entry) {
		for (Slot slot : slots) {
			if (slot.test(entry, Context.of())) {
				Optional<Slot> mappedSlot = slot.fill(entry, slot.category());
				if (mappedSlot.isPresent()) {
					return Optional.of(this.set(mappedSlot.get()));
				}
			}
		}
		return Optional.empty();
	}

	public Slot set(State entry, Slot slot) {
		for (Slot slot1 : slots) {
			if (slot == slot1 && slot.test(entry, Context.of())) {
				Optional<Slot> mappedSlot = slot.fill(entry, slot.category());
				if (mappedSlot.isPresent()) {
					this.slots.set(this.slots.indexOf(slot1), mappedSlot.get());
					return mappedSlot.get();
				}
			}
		}
		return slot;
	}

	public Optional<Slot> set(State entry, int index) {
		return slots.get(index).fill(entry, slots.get(index).category());
	}

	public ImmutableList<State> entries() {
		return slots.stream().map(Slot::get).flatMap(Optional::stream).collect(ImmutableList.toImmutableList());
	}

	public List<Slot> slots() {
		return slots;
	}

	public boolean canUpgrade(State state) {
		return slots.stream()
				.filter(slot -> !slot.filled())
				.anyMatch(slot -> slot.test(state, Context.of()));
	}

	@Override
	public SlotContainer copy() {
		return new SlotContainer(slots());
	}

	public SlotContainer remove(String id) {
		var originalSlots = slots().stream().filter(slot -> !slot.filled() || (slot.get().isPresent() && !slot.get().get().identifier().contains(id))).toList();
		var emptySlots = slots().stream().filter(slot -> (slot.get().isPresent() && slot.get().get().identifier().contains(id))).map(Slot::empty).toList();
		if (emptySlots.isEmpty()) {
			return this;
		}
		return new SlotContainer(Stream.of(originalSlots, emptySlots).flatMap(List::stream).toList());
	}

	public Slot empty(Slot slot) {
		if (this.slots.remove(slot)) {
			var empty = slot.empty();
			this.slots.add(empty);
			return empty;
		}
		return slot;
	}
}
