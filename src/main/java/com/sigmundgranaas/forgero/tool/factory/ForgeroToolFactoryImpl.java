package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPartFactory;
import org.jetbrains.annotations.NotNull;

public class ForgeroToolFactoryImpl implements ForgeroToolFactory {
    private static ForgeroToolFactory INSTANCE;

    public static ForgeroToolFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public ForgeroTool createForgeroTool(@NotNull ForgeroToolPart head, @NotNull ForgeroToolPart handle) {
        return ForgeroToolBuilder.createBuilder(head, handle).createTool();
    }

    @Override
    public ForgeroTool createForgeroTool(@NotNull ForgeroToolPart head, @NotNull ForgeroToolPart handle, @NotNull ForgeroToolPart binding) {
        return ForgeroToolBuilder.createBuilder(head, handle).addBinding(binding).createTool();
    }

    @Override
    public ForgeroTool createForgeroTool(@NotNull ForgeroToolIdentifier identifier) {
        ForgeroToolPart head = ForgeroToolPartFactory.INSTANCE.createToolPart(identifier.getHead());
        ForgeroToolPart handle = ForgeroToolPartFactory.INSTANCE.createToolPart(identifier.getHandle());
        return ForgeroToolBuilder.createBuilder(head, handle).createTool();
    }
}
