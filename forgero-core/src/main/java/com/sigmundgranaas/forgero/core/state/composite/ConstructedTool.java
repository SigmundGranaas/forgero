package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;

import java.util.List;

public class ConstructedTool extends ConstructedComposite implements MaterialBased {
    private final State head;

    private final State handle;
    private final State baseMaterial;

    public ConstructedTool(State head, State handle, State baseMaterial, SlotContainer slots, IdentifiableContainer id) {
        super(slots, id, List.of(head, handle));
        this.head = head;
        this.baseMaterial = baseMaterial;
        this.handle = handle;
    }

    @Override
    public State baseMaterial() {
        return baseMaterial;
    }

    @Override
    public ConstructedTool upgrade(State upgrade) {
        return null;
    }

    @Override
    public ConstructedTool removeUpgrade(String id) {
        return null;
    }

    public State getHead() {
        return head;
    }

    public State getHandle() {
        return handle;
    }
}
