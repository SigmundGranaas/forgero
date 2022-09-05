package com.sigmundgranaas.forgerocore.identifier;

import com.sigmundgranaas.forgerocore.deprecated.ToolPartModelType;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.identifier.model.ForgeroModelIdentifier;
import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;


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
