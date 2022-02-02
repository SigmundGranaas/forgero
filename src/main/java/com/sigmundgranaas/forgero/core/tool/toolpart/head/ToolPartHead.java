package com.sigmundgranaas.forgero.core.tool.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;

public interface ToolPartHead extends ForgeroToolPart {

    int getMiningLevel();


    ForgeroToolTypes getToolType();
}
