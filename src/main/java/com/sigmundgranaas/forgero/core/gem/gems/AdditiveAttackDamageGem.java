package com.sigmundgranaas.forgero.core.gem.gems;

import com.sigmundgranaas.forgero.core.gem.AttackDamageGem;

public interface AdditiveAttackDamageGem extends AttackDamageGem {

    @Override
    default float applyAttackDamage(float currentDamage) {
        return currentDamage + getLevel();
    }
}
