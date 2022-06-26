package com.sigmundgranaas.forgero.core.toolpart.state;

public class FilledUpgradeSlot<T extends ToolPartUpgrade> implements UpgradeSlot<T> {


    @Override
    public SlotType getType() {
        return null;
    }

    @Override
    public int getSlot() {
        return 0;
    }
}
