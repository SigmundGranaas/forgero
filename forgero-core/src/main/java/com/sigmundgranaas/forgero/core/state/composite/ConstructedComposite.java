package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;

import java.util.List;
import java.util.stream.Stream;

public class ConstructedComposite extends BaseComposite implements Constructed {
    private final List<State> parts;

    protected ConstructedComposite(SlotContainer slotContainer, IdentifiableContainer id, List<State> parts) {
        super(slotContainer, id);
        this.parts = parts;
    }

    @Override
    public List<State> components() {
        return Stream.of(parts(), upgrades()).flatMap(List::stream).toList();
    }

    @Override
    public Composite upgrade(State upgrade) {
        return null;
    }

    @Override
    public Composite removeUpgrade(String id) {
        return null;
    }

    @Override
    public List<State> parts() {
        return parts;
    }
}
