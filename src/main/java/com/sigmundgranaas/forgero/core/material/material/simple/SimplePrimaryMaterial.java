package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;

public interface SimplePrimaryMaterial extends PrimaryMaterial {
    int getMiningLevel();

    float getMiningSpeed();

    int getAttackDamage();

    float getAttackSpeed();

    int getDurability();
}
