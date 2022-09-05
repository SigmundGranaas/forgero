package com.sigmundgranaas.forgerocore.tool;

import com.sigmundgranaas.forgerocore.property.PropertyStream;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;

public interface ToolDescriptionWriter {

    void addHead(ToolPartHead head);

    void addHandle(ToolPartHandle handle);

    void addBinding(ToolPartBinding binding);

    void addToolProperties(PropertyStream stream);

    void addSwordProperties(PropertyStream stream);
}
