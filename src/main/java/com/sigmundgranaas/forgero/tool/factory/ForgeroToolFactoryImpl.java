package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.ForgeroToolBase;
import com.sigmundgranaas.forgero.tool.toolpart.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ForgeroToolFactoryImpl implements ForgeroToolFactory {
    private static ForgeroToolFactory INSTANCE;

    public static ForgeroToolFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle) {
        return ForgeroToolBuilder.createBuilder(head, handle).createTool();
    }

    @Override
    public ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle, @NotNull ToolPartBinding binding) {
        return ForgeroToolBuilder.createBuilder(head, handle).addBinding(binding).createTool();
    }

    @Override
    public ForgeroTool createForgeroTool(@NotNull ForgeroToolIdentifier identifier) {
        ToolPartHead head = (ToolPartHead) ForgeroToolPartFactory.INSTANCE.createToolPart(identifier.getHead());
        ToolPartHandle handle = (ToolPartHandle) ForgeroToolPartFactory.INSTANCE.createToolPart(identifier.getHandle());
        return ForgeroToolBuilder.createBuilder(head, handle).createTool();
    }

    @Override
    public List<ForgeroTool> createForgeroTools(@NotNull ForgeroToolPartCollection collection) {
        List<ForgeroTool> tools = new ArrayList<>();
        for (ToolPartHead head : collection.getHeads()) {
            for (ToolPartHandle handle : collection.getHandles()) {
                tools.add(new ForgeroToolBase(head, handle));
            }
        }
        return tools;
    }
}
