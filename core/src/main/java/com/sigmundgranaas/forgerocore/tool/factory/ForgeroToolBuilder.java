package com.sigmundgranaas.forgerocore.tool.factory;

import com.sigmundgranaas.forgerocore.gem.ForgeroGem;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;

@SuppressWarnings("unused")
public interface ForgeroToolBuilder {
    static ForgeroToolBuilder createBuilder(ToolPartHead head, ToolPartHandle handle) {
        return new ForgeroToolBuilderImpl(head, handle);
    }

    ForgeroToolBuilder addBinding(ToolPartBinding binding);

    ForgeroToolBuilder addGem(ForgeroToolPartTypes location, ForgeroGem gem);


    ForgeroTool createTool();
}
