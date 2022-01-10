package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierType;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public class ForgeroToolIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroToolIdentifier {
    private final String[] toolElements;

    public ForgeroToolIdentifierImpl(String toolDescriptor) {
        super(ForgeroIdentifierType.TOOL);
        String[] elements = toolDescriptor.split("_");
        if (elements.length != 6) {
            Forgero.LOGGER.warn("Unable to Create ForgeroToolIdentifier with: {}", toolDescriptor);
            throw new IllegalArgumentException("Unable to Create ForgeroTolIdentifier with: " + toolDescriptor);
        }
        this.toolElements = elements;
    }

    @Override
    public ForgeroMaterialIdentifier getMaterial() {
        return (ForgeroMaterialIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[0]);
    }

    @Override
    public ForgeroToolPartHeadIdentifier getHead() {
        return (ForgeroToolPartHeadIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[2] + "_" + toolElements[3]);
    }

    @Override
    public ForgeroToolPartIdentifier getHandle() {
        return (ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[4] + "_" + toolElements[5]);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }
}
