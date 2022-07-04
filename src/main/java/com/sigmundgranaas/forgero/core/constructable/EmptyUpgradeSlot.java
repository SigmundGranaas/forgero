package com.sigmundgranaas.forgero.core.constructable;

public class EmptyUpgradeSlot<T extends Upgrade> extends EmptySlot<T> implements UpgradeSlot<T>{
    public EmptyUpgradeSlot(SlotType type, SlotPlacement placement) {
        super(type, placement);
    }
}
