package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.tool.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.ToolPartHead;

public interface ToolDescriptionWriter {

    void addHead(ToolPartHead head);

    void addHandle(ToolPartHandle handle);

    void addBinding(ToolPartBinding binding);
}
