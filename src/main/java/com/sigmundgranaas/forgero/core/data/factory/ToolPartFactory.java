package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ToolPartFactory extends DataResourceFactory<ToolPartPojo, ForgeroToolPart> {

    public ToolPartFactory(List<ToolPartPojo> pojos, Set<String> availableNameSpaces) {
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
    public Optional<ForgeroToolPart> createResource(ToolPartPojo data) {
        return Optional.empty();
    }
}
