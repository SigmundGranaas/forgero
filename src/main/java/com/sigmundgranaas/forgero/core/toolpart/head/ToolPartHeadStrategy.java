package com.sigmundgranaas.forgero.core.toolpart.head;


import com.sigmundgranaas.forgero.core.toolpart.ToolPartPropertyStrategy;

public interface ToolPartHeadStrategy extends ToolPartPropertyStrategy {

    int getDamage();

    float getMiningSpeedMultiplier();

    int getMiningLevel();
}
