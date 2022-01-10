package com.sigmundgranaas.forgero.core.tool.factory;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolBase;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartCollection;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ForgeroToolPartFactory;
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
        ForgeroToolBuilder builder = ForgeroToolBuilder.createBuilder(head, handle);
        builder = builder.addBinding(binding);
        return builder.createTool();
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
