package com.sigmundgranaas.forgero.core.tool.factory;

import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.skin.ForgeroToolPartSkin;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolBase;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;

public class ForgeroToolBuilderImpl implements ForgeroToolBuilder {
    private final ToolPartHead head;
    private final ToolPartHandle handle;

    public ForgeroToolBuilderImpl(ToolPartHead head, ToolPartHandle handle) {
        this.head = head;
        this.handle = handle;
    }


    @Override
    public ForgeroToolBuilder addBinding(ToolPartBinding binding) {
        return null;
    }

    @Override
    public ForgeroToolBuilder addGem(ForgeroToolPartTypes location, ForgeroGem gem) {
        return this;
    }

    @Override
    public ForgeroToolBuilder addSkin(ForgeroToolPartTypes toolpart, ForgeroToolPartSkin skin) {
        return null;
    }

    @Override
    public ForgeroTool createTool() {
        return new ForgeroToolBase(head, handle);
    }
}
