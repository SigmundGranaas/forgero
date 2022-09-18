package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolFactory extends ForgeroResourceFactory<ForgeroTool, ToolPojo> {
    ForgeroToolFactory INSTANCE = ForgeroToolFactoryImpl.getInstance();

    ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle);

    ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle, @NotNull ToolPartBinding binding);

    ForgeroTool createForgeroTool(@NotNull ForgeroToolIdentifier identifier);

    List<ForgeroTool> createForgeroTools(@NotNull List<ForgeroToolPart> collection);
}
