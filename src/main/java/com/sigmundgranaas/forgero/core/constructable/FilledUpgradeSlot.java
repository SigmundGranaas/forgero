package com.sigmundgranaas.forgero.core.constructable;

import java.util.Optional;

public class FilledUpgradeSlot<T extends Upgrade> implements UpgradeSlot<T> {
    private final T upgrade;
    private final SlotPlacement placement;
    private final SlotType type;

    public FilledUpgradeSlot(T upgrade, SlotPlacement placement, SlotType type) {
        this.upgrade = upgrade;
        this.placement = placement;
        this.type = type;
    }

    T get(){
        return this.upgrade;
    }

    @Override
    public boolean isFilled() {
        return true;
    }

    @Override
    public SlotType getType() {
        return type;
    }

    @Override
    public SlotPlacement getPlacement() {
        return placement;
    }

    @Override
    public Optional<T> getIfPresent() {
        return Optional.of(upgrade);
    }
}
