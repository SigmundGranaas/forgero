package com.sigmundgranaas.forgero.state.slot;

import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.util.match.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotContainer {
    private final ArrayList<Slot> slots;

    public SlotContainer(ArrayList<Slot> slots) {
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
        return slots.stream()
                .filter(slot -> slot.test(entry, Context.of()))
                .map(slot -> slot.fill(entry, slot.category()))
                .flatMap(Optional::stream)
                .findFirst()
                .map(this::set);
    }

    public Optional<Slot> set(State entry, int index) {
        return slots.get(index).fill(entry, slots.get(index).category());
    }

    public List<State> entries() {
        return slots.stream().map(Slot::get).flatMap(Optional::stream).toList();
    }

    public List<Slot> slots() {
        return slots;
    }

    public boolean canUpgrade(State state) {
        return slots.stream()
                .filter(slot -> !slot.filled())
                .anyMatch(slot -> slot.test(state, Context.of()));
    }
}
