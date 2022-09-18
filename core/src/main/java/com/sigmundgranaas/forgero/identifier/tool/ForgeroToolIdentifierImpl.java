package com.sigmundgranaas.forgero.identifier.tool;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierType;
import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

public class ForgeroToolIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroToolIdentifier {
    private final String[] toolElements;

    public ForgeroToolIdentifierImpl(String toolDescriptor) {
        super(ForgeroIdentifierType.TOOL);
        String[] elements = toolDescriptor.split(ELEMENT_SEPARATOR);
        if (elements.length != 2) {
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
        return (ForgeroToolPartHeadIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[0] + ELEMENT_SEPARATOR + toolElements[1] + "head");
    }

    @Override
    public ForgeroToolPartIdentifier getHandle() {
        return (ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier("oak" + ELEMENT_SEPARATOR + "handle");
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }

    @Override
    public String getIdentifier() {
        return toolElements[0] + ELEMENT_SEPARATOR + toolElements[1];
    }
}
