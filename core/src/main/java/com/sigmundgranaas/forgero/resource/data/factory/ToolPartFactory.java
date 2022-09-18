package com.sigmundgranaas.forgero.resource.data.factory;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class ToolPartFactory extends DataResourceFactory<ToolPartPojo, ForgeroToolPart> {

    public ToolPartFactory(Collection<ToolPartPojo> pojos, Set<String> availableNameSpaces) {
        super(pojos, availableNameSpaces);
    }


    @Override
    protected ToolPartPojo mergePojos(ToolPartPojo parent, ToolPartPojo child, ToolPartPojo basePojo) {
        return child;
    }

    @Override
    protected ToolPartPojo createDefaultPojo() {
        return new ToolPartPojo();
    }

    @Override
    public @NotNull Optional<ForgeroToolPart> createResource(ToolPartPojo data) {
        return Optional.empty();
    }
}
