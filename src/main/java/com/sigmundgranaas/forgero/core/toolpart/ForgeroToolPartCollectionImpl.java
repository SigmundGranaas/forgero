package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
public class ForgeroToolPartCollectionImpl implements ForgeroToolPartCollection {
    private final List<ForgeroToolPart> toolParts;

    public ForgeroToolPartCollectionImpl(List<ForgeroToolPart> toolParts) {
        this.toolParts = toolParts;
    }

    @Override
    public @NotNull
    List<ForgeroToolPart> getToolParts() {
        return toolParts;
    }

    @Override
    public @NotNull
    List<ToolPartHandle> getHandles() {
        return getToolPartsSubtype(ToolPartHandle.class);
    }

    @Override
    public @NotNull
    List<ToolPartBinding> getBindings() {
        return getToolPartsSubtype(ToolPartBinding.class);

    }

    @Override
    public @NotNull
    List<ToolPartHead> getHeads() {
        return getToolPartsSubtype(ToolPartHead.class);

    }

    private @NotNull <T> List<T> getToolPartsSubtype(Class<T> type) {
        return toolParts.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }
}
