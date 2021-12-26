package com.sigmundgranaas.forgero.item.instance.tool;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import org.jetbrains.annotations.NotNull;

public abstract class ForgeroToolInstance {
    private final @NotNull
    ForgeroTool tool;

    public ForgeroToolInstance(@NotNull ForgeroTool tool) {
        this.tool = tool;
    }

    public @NotNull
    ForgeroTool getTool() {
        return tool;
    }

    public abstract ForgeroToolTypes getToolType();


}
