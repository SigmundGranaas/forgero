package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

public interface UpgradeSlot<T extends ToolPartUpgrade> extends PropertyContainer {
    SlotType getType();

    int getSlot();
}
