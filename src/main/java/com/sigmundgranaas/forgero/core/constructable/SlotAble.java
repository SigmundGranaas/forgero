package com.sigmundgranaas.forgero.core.constructable;

import java.util.List;
import java.util.Set;

public interface SlotAble {
    Set<String> getValidPlacements();

    List<Slot<?>> getSlots();
}
