package com.sigmundgranaas.forgero.core.constructable;

public record SlotPlacement(String placement, int index) implements SlotValidator {
    public boolean test(SlotAble subject) {
        return subject.getValidPlacements().contains(placement);
    }
}
