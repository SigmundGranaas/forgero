package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractForgeroTool implements ForgeroTool {
    protected final ForgeroToolPart head;
    protected final ForgeroToolPart handle;

    protected AbstractForgeroTool(ForgeroToolPart head, ForgeroToolPart handle) {
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
