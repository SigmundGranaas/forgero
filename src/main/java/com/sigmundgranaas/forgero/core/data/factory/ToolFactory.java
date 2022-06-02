package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class ToolFactory extends DataResourceFactory<ToolPojo, ForgeroTool> {

    public ToolFactory(Collection<ToolPojo> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }

    @Override
    protected ToolPojo mergePojos(ToolPojo parent, ToolPojo child, ToolPojo basePojo) {
        return child;
    }

    @Override
    protected ToolPojo createDefaultPojo() {
        return new ToolPojo();
    }

    @Override
    public @NotNull Optional<ForgeroTool> createResource(ToolPojo data) {
        return Optional.empty();
    }
}
