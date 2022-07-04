package com.sigmundgranaas.forgero.core.constructable;

public record SlotType(String type) implements SlotValidator {
    public boolean test(SlotAble subject){
        return subject.getValidPlacements().contains(type);
    }
}
