package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public interface ToolPartHead extends ForgeroToolPart {
    int getSharpness();

    ForgeroToolTypes getHeadType();
}
