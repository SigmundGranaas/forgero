package com.sigmundgranaas.forgerocore.tool.factory;

import com.sigmundgranaas.forgerocore.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgerocore.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolFactory extends ForgeroResourceFactory<ForgeroTool, ToolPojo> {
    ForgeroToolFactory INSTANCE = ForgeroToolFactoryImpl.getInstance();

    ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle);

    ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle, @NotNull ToolPartBinding binding);

    ForgeroTool createForgeroTool(@NotNull ForgeroToolIdentifier identifier);

    List<ForgeroTool> createForgeroTools(@NotNull List<ForgeroToolPart> collection);
}
