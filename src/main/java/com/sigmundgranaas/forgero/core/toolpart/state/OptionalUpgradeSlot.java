package com.sigmundgranaas.forgero.core.toolpart.state;

import java.util.Optional;

public class OptionalUpgradeSlot<T extends ToolPartUpgrade> implements UpgradeSlot<T> {
    boolean isFilled() {
        return !isEmpty();
    }

    boolean isEmpty() {
        return true;
    }

    Optional<T> getResource() {
        return Optional.empty();
    }

    Optional<FilledUpgradeSlot<T>> fill(T upgrade) {
        return Optional.empty();
    }

    @Override
    public SlotType getType() {
        return null;
    }

    @Override
    public int getSlot() {
        return 0;
    }
}
