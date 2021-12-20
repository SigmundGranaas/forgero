package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;

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
    public ForgeroToolPartIdentifier getHead() {
        return (ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[2] + "_" + toolElements[3]);
    }

    @Override
    public ForgeroToolPartIdentifier getHandle() {
        return (ForgeroToolPartIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolElements[4] + "_" + toolElements[5]);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return switch (getHead().getToolPartType()) {
            case PICKAXEHEAD -> ForgeroToolTypes.PICKAXE;
            case SWORDHEAD -> ForgeroToolTypes.SWORD;
            case SHOVELHEAD -> ForgeroToolTypes.SHOVEL;
            default -> throw new IllegalArgumentException("Cannot be a tool without a proper tool head type");
        };
    }
}
