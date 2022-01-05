package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import net.minecraft.util.Identifier;

public interface ForgeroIdentifierFactory {
    ForgeroIdentifierFactory INSTANCE = ForgeroIdentifierFactoryImpl.getInstance();

    ForgeroIdentifier createForgeroIdentifier(Identifier identifier);

    ForgeroIdentifier createForgeroIdentifier(String identifier);

    ForgeroMaterialIdentifier createForgeroMaterialIdentifier(String identifier);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart, SecondaryMaterial secondaryMaterial);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart, SecondaryMaterial secondaryMaterial);
}
