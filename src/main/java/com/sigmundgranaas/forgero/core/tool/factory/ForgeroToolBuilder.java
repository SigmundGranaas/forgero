package com.sigmundgranaas.forgero.core.tool.factory;

import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.skin.ForgeroToolPartSkin;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;

public interface ForgeroToolBuilder {
    static ForgeroToolBuilder createBuilder(ToolPartHead head, ToolPartHandle handle) {
        return new ForgeroToolBuilderImpl(head, handle);
    }

    ForgeroToolBuilder addBinding(ToolPartBinding binding);

    ForgeroToolBuilder addGem(ForgeroToolPartTypes location, ForgeroGem gem);

    ForgeroToolBuilder addSkin(ForgeroToolPartTypes toolpart, ForgeroToolPartSkin skin);

    ForgeroTool createTool();
}
