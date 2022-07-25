package com.sigmundgranaas.forgero.core.constructable;

import java.util.Optional;

public class EmptySlot<T extends SlotAble> implements Slot<T>, SlotValidator {
   private final SlotType type;
   private final SlotPlacement placement;

    public EmptySlot(SlotType type, SlotPlacement placement) {
        this.type = type;
        this.placement = placement;
    }

    @Override
    public boolean isFilled() {
        return false;
    }

    Optional<FilledSlot<T>> fill(T upgrade) {
        if(test(upgrade)){
            return Optional.of(new FilledSlot<T>(upgrade, type, placement));
        }else{
           return Optional.empty();
        }
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
        return Optional.empty();
    }

    @Override
    public boolean test(SlotAble element) {
        return getPlacement().test(element) && getType().test(element);
    }
}
