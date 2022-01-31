package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;

public interface ToolDescriptionWriter {

    void addHead(ToolPartHead head);

    void addHandle(ToolPartHandle handle);

    void addBinding(ToolPartBinding binding);
}
