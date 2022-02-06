package com.sigmundgranaas.forgero.core.gem.gems;

import com.sigmundgranaas.forgero.core.gem.*;

public class AdditiveDurabilityGem extends ForgeroGem implements DurabilityGem, HeadGem, BindingGem, HandleGem {
    public AdditiveDurabilityGem(int gemLevel, String identifier) {
        super(gemLevel, identifier);
    }

    @Override
    public int applyDurability(int currentDurability) {
        return currentDurability + 100 * getLevel();
    }

    @Override
    public AdditiveDurabilityGem createNewGem(int level, String Identifier) {
        return new AdditiveDurabilityGem(level, Identifier);
    }

    @Override
    public boolean equals(Gem newGem) {
        return newGem instanceof AdditiveDurabilityGem && newGem.getLevel() == getLevel();
    }

    @Override
    public Gem createGem(int level) {
        return createNewGem(level, this.getIdentifier());
    }
}
