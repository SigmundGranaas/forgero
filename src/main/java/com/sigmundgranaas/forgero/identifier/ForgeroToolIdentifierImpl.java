package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.Forgero;

public class ForgeroToolIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroToolIdentifier {
    private final String[] toolElements;

    public ForgeroToolIdentifierImpl(ForgeroIdentifierType tool, String toolDescriptor) {
        super(tool);
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
    public ForgeroToolPartIdentifier getHead() {
        return (ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[2] + "_" + toolElements[3]);
    }

    @Override
    public ForgeroToolPartIdentifier getHandle() {
        return (ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[4] + "_" + toolElements[5]);
    }
}
