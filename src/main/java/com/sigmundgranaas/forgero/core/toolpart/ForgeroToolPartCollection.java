package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolPartCollection {
    @NotNull
    List<ForgeroToolPart> getToolParts();

    @NotNull
    List<ToolPartHandle> getHandles();

    @NotNull
    List<ToolPartBinding> getBindings();

    @NotNull
    List<ToolPartHead> getHeads();
}
