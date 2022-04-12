package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.property.PropertyStream;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;

public interface ToolDescriptionWriter {

    void addHead(ToolPartHead head);

    void addHandle(ToolPartHandle handle);

    void addBinding(ToolPartBinding binding);

    void addToolProperties(PropertyStream stream);
}
