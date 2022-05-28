package com.sigmundgranaas.forgero.core.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.ForgeroResourceRegistry;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;

import java.util.Optional;

public interface ToolPartRegistry extends ForgeroResourceRegistry<ForgeroToolPart> {
    default ImmutableList<ToolPartHead> getHeads() {
        return getSubTypeAsList(ToolPartHead.class);
    }

    default ImmutableList<ToolPartHandle> getHandles() {
        return getSubTypeAsList(ToolPartHandle.class);
    }

    default ImmutableList<ToolPartBinding> getBindings() {
        return getSubTypeAsList(ToolPartBinding.class);
    }

    default Optional<ToolPartHead> getHead(String identifier) {
        return getResource(identifier).map(ToolPartHead.class::cast);
    }

    default Optional<ToolPartBinding> getBinding(String identifier) {
        return getResource(identifier).map(ToolPartBinding.class::cast);
    }

    default Optional<ToolPartHandle> getHandle(String identifier) {
        return getResource(identifier).map(ToolPartHandle.class::cast);
    }
}
