package com.sigmundgranaas.forgerocore.tool.factory;

import com.sigmundgranaas.forgerocore.gem.ForgeroGem;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolBase;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;

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
