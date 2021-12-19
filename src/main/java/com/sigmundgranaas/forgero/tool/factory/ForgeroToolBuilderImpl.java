package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.gem.ForgeroGem;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.skin.ForgeroToolPartSkin;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;

public class ForgeroToolBuilderImpl implements ForgeroToolBuilder {
    private final ForgeroToolPart head;
    private final ForgeroToolPart handle;

    public ForgeroToolBuilderImpl(ForgeroToolPart head, ForgeroToolPart handle) {
        this.head = head;
        this.handle = handle;
    }

    @Override
    public ForgeroToolBuilder addBinding(ForgeroToolPart binding) {
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
        return null;
    }
}
