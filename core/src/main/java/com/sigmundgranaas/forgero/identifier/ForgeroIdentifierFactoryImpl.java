package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.deprecated.ModelLayer;
import com.sigmundgranaas.forgero.deprecated.ToolPartModelType;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.identifier.model.ForgeroModelIdentifier;
import com.sigmundgranaas.forgero.identifier.tool.*;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

public class ForgeroIdentifierFactoryImpl implements ForgeroIdentifierFactory {
    private static ForgeroIdentifierFactoryImpl INSTANCE;

    public static ForgeroIdentifierFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroIdentifierFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public ForgeroIdentifier createForgeroIdentifier(String identifier) {
        return createForgeroIdentifierFromName(identifier);
    }

    @Override
    public ForgeroMaterialIdentifier createForgeroMaterialIdentifier(String identifier) {
        String[] elements = identifier.split(ELEMENT_SEPARATOR);
        return new ForgeroMaterialIdentifierImpl(elements[0]);
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart) {
        return new ForgeroModelIdentifier(toolPart.getPrimaryMaterial().getResourceName(), ToolPartModelType.getModelType(toolPart), ModelLayer.PRIMARY, toolPart.getSchematic().getResourceName());
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart) {
        return new ForgeroModelIdentifier(toolPart.getPrimaryMaterial().getResourceName(), ToolPartModelType.getModelType(toolPart, toolType), ModelLayer.PRIMARY, toolPart.getSchematic().getResourceName());
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart, SecondaryMaterial
            secondaryMaterial) {
        return new ForgeroModelIdentifier(secondaryMaterial.getResourceName(), ToolPartModelType.getModelType(toolPart), ModelLayer.SECONDARY, toolPart.getSchematic().getResourceName());
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart
            toolPart, SecondaryMaterial secondaryMaterial) {
        return new ForgeroModelIdentifier(secondaryMaterial.getResourceName(), ToolPartModelType.getModelType(toolPart, toolType), ModelLayer.SECONDARY, toolPart.getSchematic().getResourceName());
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(Gem gem, ForgeroToolPart toolPart, ToolPartModelType modelType) {
        return new ForgeroModelIdentifier(gem.getResourceName(), modelType, ModelLayer.GEM, toolPart.getSchematic().getResourceName());
    }

    private ForgeroIdentifier createForgeroIdentifierFromName(String forgeroName) {
        String[] elements = forgeroName.split(ELEMENT_SEPARATOR);
        if (elements.length == 1) {
            return new ForgeroMaterialIdentifierImpl(forgeroName);
        } else if (ForgeroToolTypes.isTool(elements[1])) {
            return new ForgeroToolIdentifierImpl(forgeroName);
        } else if (elements.length == 2) {
            return createForgeroToolPartIdentifier(forgeroName);
        }
        throw new IllegalStateException("Unexpected value: " + elements.length);
    }

    private ForgeroIdentifier createForgeroToolPartIdentifier(String forgeroName) {
        if (forgeroName.contains("head")) {
            return new ForgeroToolPartHeadIdentifier(forgeroName);
        } else {
            return new ForgeroToolPartIdentifierImpl(forgeroName);
        }
    }
}
