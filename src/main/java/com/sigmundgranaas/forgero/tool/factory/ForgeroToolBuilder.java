package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.gem.ForgeroGem;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.skin.ForgeroToolPartSkin;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHead;

public interface ForgeroToolBuilder {
    static ForgeroToolBuilder createBuilder(ToolPartHead head, ToolPartHandle handle) {
        return new ForgeroToolBuilderImpl(head, handle);
    }

    ForgeroToolBuilder addBinding(ToolPartBinding binding);

    ForgeroToolBuilder addGem(ForgeroToolPartTypes location, ForgeroGem gem);

    ForgeroToolBuilder addSkin(ForgeroToolPartTypes toolpart, ForgeroToolPartSkin skin);

    ForgeroTool createTool();
}
