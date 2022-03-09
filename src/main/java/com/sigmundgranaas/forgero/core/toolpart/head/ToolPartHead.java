package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

public interface ToolPartHead extends ForgeroToolPart {
    ForgeroToolTypes getToolType();

    float getAttackSpeedBase();

    float getAttackDamageBase();
}
