package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;

public interface ItemRegister {
    void registerTool(ForgeroTool forgeroTool);

    void registerToolPart(ForgeroToolPart toolPart);
}
