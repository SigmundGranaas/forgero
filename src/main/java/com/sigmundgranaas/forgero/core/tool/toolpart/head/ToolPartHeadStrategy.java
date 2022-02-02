package com.sigmundgranaas.forgero.core.tool.toolpart.head;


import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartPropertyStrategy;

public interface ToolPartHeadStrategy extends ToolPartPropertyStrategy {

    int getDamage();

    float getMiningSpeedMultiplier();

    int getMiningLevel();
}
