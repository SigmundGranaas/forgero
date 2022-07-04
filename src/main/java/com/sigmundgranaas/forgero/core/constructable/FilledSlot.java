package com.sigmundgranaas.forgero.core.constructable;

import java.util.Optional;

public record FilledSlot<T extends SlotAble>(T element, SlotType type, SlotPlacement placement) implements Slot<T>{
    @Override
    public boolean isFilled() {
        return false;
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
        return Optional.of(element);
    }

    T get(){
        return element;
    }
}
