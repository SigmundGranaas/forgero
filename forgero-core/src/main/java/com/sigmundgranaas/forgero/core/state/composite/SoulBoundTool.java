package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.soul.SoulContainer;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;

public class SoulBoundTool extends ConstructedTool implements SoulContainer {
    private final Soul soul;

    public SoulBoundTool(State head, State handle, State baseMaterial, SlotContainer slots, IdentifiableContainer id, Soul soul) {
        super(head, baseMaterial, handle, slots, id);
        this.soul = soul;
    }

    @Override
    public Soul getSoul() {
        return soul;
    }
}
