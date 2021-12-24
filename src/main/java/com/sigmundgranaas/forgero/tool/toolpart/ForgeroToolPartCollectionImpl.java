package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.material.MaterialCollection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ForgeroToolPartCollectionImpl implements ForgeroToolPartCollection {
    public static ForgeroToolPartCollection INSTANCE;
    private final List<ForgeroToolPart> toolParts;

    public ForgeroToolPartCollectionImpl(List<ForgeroToolPart> toolParts) {
        this.toolParts = toolParts;
    }

    public static ForgeroToolPartCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolPartCollectionImpl(ForgeroToolPartFactory.INSTANCE.createBaseToolParts(MaterialCollection.INSTANCE));
        }
        return INSTANCE;
    }

    private void loadToolParts() {

    }

    @Override
    public @NotNull List<ForgeroToolPart> getToolParts() {
        if (toolParts.isEmpty()) {
            loadToolParts();
        }
        return toolParts;
    }

    @Override
    public @NotNull List<ToolPartHandle> getHandles() {
        return getToolPartsSubtype(ToolPartHandle.class);
    }

    @Override
    public @NotNull List<ToolPartBinding> getBindings() {
        return getToolPartsSubtype(ToolPartBinding.class);

    }

    @Override
    public @NotNull List<ToolPartHead> getHeads() {
        return getToolPartsSubtype(ToolPartHead.class);

    }

    private @NotNull <T> List<T> getToolPartsSubtype(Class<T> type) {
        return toolParts.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }
}
