package com.sigmundgranaas.forgero.core.gem;

public interface AttackDamageGem extends Gem {
    float applyAttackDamage(float currentDamage);

    default GemTypes getType() {
        return GemTypes.ATTACK_DAMAGE;
    }
}
