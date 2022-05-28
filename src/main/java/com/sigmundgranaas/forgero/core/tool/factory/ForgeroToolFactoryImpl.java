package com.sigmundgranaas.forgero.core.tool.factory;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolBase;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
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
    public List<ForgeroTool> createForgeroTools(@NotNull List<ForgeroToolPart> collection) {
        List<ForgeroTool> tools = new ArrayList<>();
        for (ToolPartHead head : collection.stream()
                .filter(toolPart -> toolPart instanceof ToolPartHead)
                .map(ToolPartHead.class::cast)
                .filter(toolPartHead -> toolPartHead.getSchematic().getName().equals("default")).toList()) {
            //noinspection OptionalGetWithoutIsPresent
            tools.add(new ForgeroToolBase(head, collection.stream()
                    .filter(toolPart -> toolPart instanceof ToolPartHandle)
                    .map(ToolPartHandle.class::cast)
                    .filter(handle -> handle.getPrimaryMaterial().getName().equals("oak")).findAny().get()));
        }
        return tools;
    }
}
