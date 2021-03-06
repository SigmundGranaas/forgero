package com.sigmundgranaas.forgero.core.tool.factory;

import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolBase;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;

public class ForgeroToolBuilderImpl implements ForgeroToolBuilder {
    private final ToolPartHead head;
    private final ToolPartHandle handle;
    private ToolPartBinding binding = null;

    public ForgeroToolBuilderImpl(ToolPartHead head, ToolPartHandle handle) {
        this.head = head;
        this.handle = handle;
    }


    @Override
    public ForgeroToolBuilder addBinding(ToolPartBinding binding) {
        this.binding = binding;
        return this;
    }

    @Override
    public ForgeroToolBuilder addGem(ForgeroToolPartTypes location, ForgeroGem gem) {
        return this;
    }


    @Override
    public ForgeroTool createTool() {
        if (binding == null) {
            return new ForgeroToolBase(head, handle);
        } else {
            return new ForgeroToolWithBinding(head, handle, binding);
        }
    }
}
