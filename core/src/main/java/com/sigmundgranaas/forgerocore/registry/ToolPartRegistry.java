package com.sigmundgranaas.forgerocore.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgerocore.ForgeroResourceRegistry;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;

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
