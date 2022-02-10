package com.sigmundgranaas.forgero.core.gem.gems;

import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.HeadGem;
import com.sigmundgranaas.forgero.core.gem.MiningSpeedGem;

public class AdditiveMiningSpeedGem extends ForgeroGem implements MiningSpeedGem, HeadGem {
    public AdditiveMiningSpeedGem(int gemLevel, String identifier) {
        super(gemLevel, identifier);
    }

    @Override
    public ForgeroGem createNewGem(int level, String Identifier) {
        return new AdditiveMiningSpeedGem(level, Identifier);
    }

    @Override
    public boolean equals(Gem newGem) {
        return newGem instanceof AdditiveMiningSpeedGem && newGem.getLevel() == getLevel();
    }


    @Override
    public Gem createGem(int level) {
        return createNewGem(level, this.getIdentifier());
    }

    @Override
    public float applyAttackDamage(float currentDamage) {
        return currentDamage;
    }

    @Override
    public float applyMiningSpeedMultiplier(float currentMiningSpeed) {
        return currentMiningSpeed + 3.0f * getLevel();
    }
}
