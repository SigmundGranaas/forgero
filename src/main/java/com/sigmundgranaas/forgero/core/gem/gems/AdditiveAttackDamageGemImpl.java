package com.sigmundgranaas.forgero.core.gem.gems;

import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.HeadGem;

public class AdditiveAttackDamageGemImpl extends ForgeroGem implements AdditiveAttackDamageGem, HeadGem {
    public AdditiveAttackDamageGemImpl(int gemLevel, String identifier) {
        super(gemLevel, identifier);
    }

    @Override
    public ForgeroGem createNewGem(int level, String Identifier) {
        return new AdditiveAttackDamageGemImpl(level, getIdentifier());
    }

    @Override
    public boolean equals(Gem newGem) {
        return (newGem instanceof AdditiveAttackDamageGemImpl && newGem.getLevel() == this.getLevel());
    }

    @Override
    public float applyAttackDamage(float currentDamage) {
        return AdditiveAttackDamageGem.super.applyAttackDamage(currentDamage);
    }

    @Override
    public float applyMiningSpeedMultiplier(float currentMiningSpeed) {
        return currentMiningSpeed;
    }


    @Override
    public Gem createGem(int level) {
        return createNewGem(level, this.getIdentifier());
    }
}
