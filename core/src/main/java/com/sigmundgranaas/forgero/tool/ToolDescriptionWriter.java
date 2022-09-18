package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.property.PropertyStream;
import com.sigmundgranaas.forgero.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;

public interface ToolDescriptionWriter {

    void addHead(ToolPartHead head);

    void addHandle(ToolPartHandle handle);

    void addBinding(ToolPartBinding binding);

    void addToolProperties(PropertyStream stream);

    void addSwordProperties(PropertyStream stream);
}
