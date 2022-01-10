package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.identifier.tool.*;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import net.minecraft.util.Identifier;

import java.util.stream.Stream;

public class ForgeroIdentifierFactoryImpl implements ForgeroIdentifierFactory {
    private static ForgeroIdentifierFactoryImpl INSTANCE;

    public static ForgeroIdentifierFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroIdentifierFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public ForgeroIdentifier createForgeroIdentifier(Identifier identifier) {
        String removePath = Stream.of(identifier.getPath().split("/"))
                .reduce((first, second) -> second)
                .orElse("")
                .replace(".png", "")
                .replace("#inventory", "");
        return createForgeroIdentifier(removePath);
    }

    @Override
    public ForgeroIdentifier createForgeroIdentifier(String identifier) {
        return createForgeroIdentifierFromName(identifier);
    }

    @Override
    public ForgeroMaterialIdentifier createForgeroMaterialIdentifier(String identifier) {
        String[] elements = identifier.split("_");
        return new ForgeroMaterialIdentifierImpl(elements[0]);
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart) {
        return new ForgeroModelIdentifier(toolPart.getToolPartIdentifier());
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart) {
        return new ForgeroModelIdentifier(createToolPartModelVariationTemplate(ToolPartModelType.getModelType(toolPart, toolType), toolPart.getPrimaryMaterial()));
    }

    private String createToolPartModelVariationTemplate(ToolPartModelType type, ForgeroMaterial material) {
        return material.getName() + "_" + type.toFileName();
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart, SecondaryMaterial
            secondaryMaterial) {
        return new ForgeroModelIdentifier(createToolPartModelVariationTemplate(ToolPartModelType.getModelType(toolPart), secondaryMaterial));
    }

    @Override
    public ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart
            toolPart, SecondaryMaterial secondaryMaterial) {
        return new ForgeroModelIdentifier(createToolPartModelVariationTemplate(ToolPartModelType.getModelType(toolPart, toolType), secondaryMaterial));
    }

    private ForgeroIdentifier createForgeroIdentifierFromName(String forgeroName) {
        String[] elements = forgeroName.split("_");
        return switch (elements.length) {
            case 6 -> new ForgeroToolIdentifierImpl(forgeroName);
            case 2 -> createForgeroToolIdentifier(forgeroName);
            case 1 -> new ForgeroMaterialIdentifierImpl(forgeroName);
            default -> throw new IllegalStateException("Unexpected value: " + elements.length);
        };

    }

    private ForgeroIdentifier createForgeroToolIdentifier(String forgeroName) {
        if (forgeroName.contains("head")) {
            return new ForgeroToolPartHeadIdentifier(forgeroName);
        } else {
            return new ForgeroToolPartIdentifierImpl(forgeroName);
        }
    }
}
