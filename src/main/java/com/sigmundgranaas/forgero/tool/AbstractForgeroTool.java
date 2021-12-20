package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHead;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractForgeroTool implements ForgeroTool {
    protected final ToolPartHead head;
    protected final ToolPartHandle handle;

    protected AbstractForgeroTool(ToolPartHead head, ToolPartHandle handle) {
        this.head = head;
        this.handle = handle;
    }

    @Override
    public @NotNull ForgeroToolPart getToolHead() {
        return head;
    }

    @Override
    public @NotNull PrimaryMaterial getPrimaryMaterial() {
        return head.getPrimaryMaterial();
    }

    @Override
    public @NotNull ForgeroToolPart getToolHandle() {
        return handle;
    }
}
