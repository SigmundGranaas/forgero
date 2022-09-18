package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.gem.ForgeroGem;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;

@SuppressWarnings("unused")
public interface ForgeroToolBuilder {
    static ForgeroToolBuilder createBuilder(ToolPartHead head, ToolPartHandle handle) {
        return new ForgeroToolBuilderImpl(head, handle);
    }

    ForgeroToolBuilder addBinding(ToolPartBinding binding);

    ForgeroToolBuilder addGem(ForgeroToolPartTypes location, ForgeroGem gem);


    ForgeroTool createTool();
}
