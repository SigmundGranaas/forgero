package com.sigmundgranaas.forgero.core.toolpart.head;


import com.sigmundgranaas.forgero.core.toolpart.ToolPartPropertyStrategy;

public interface ToolPartHeadStrategy extends ToolPartPropertyStrategy {

    float getAttackDamage();

    float getMiningSpeedMultiplier();

    int getMiningLevel();

    float getAttackSpeed();

    double getAttackDamageAddition();
}
