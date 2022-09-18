package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.deprecated.ToolPartModelType;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.identifier.model.ForgeroModelIdentifier;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;


public interface ForgeroIdentifierFactory {
    ForgeroIdentifierFactory INSTANCE = ForgeroIdentifierFactoryImpl.getInstance();

    ForgeroIdentifier createForgeroIdentifier(String identifier);

    ForgeroMaterialIdentifier createForgeroMaterialIdentifier(String identifier);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart, SecondaryMaterial secondaryMaterial);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart, SecondaryMaterial secondaryMaterial);

    ForgeroModelIdentifier createToolPartModelIdentifier(Gem gem, ForgeroToolPart toolPart, ToolPartModelType modelType);
}
