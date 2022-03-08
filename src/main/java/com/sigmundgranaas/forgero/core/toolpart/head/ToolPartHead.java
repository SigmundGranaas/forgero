package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

public interface ToolPartHead extends ForgeroToolPart {

    int getMiningLevel();

    float getAttackDamage();

    float getMiningSpeedMultiplier();

    float getAttackSpeed();

    ForgeroToolTypes getToolType();

    double getAttackDamageAddition();

    float getAttackSpeedBase();

    float getAttackDamageBase();
}
