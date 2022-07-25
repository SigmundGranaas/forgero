package com.sigmundgranaas.forgero.core.constructable;

@FunctionalInterface
public interface SlotValidator {
     boolean test(SlotAble slot);
}
