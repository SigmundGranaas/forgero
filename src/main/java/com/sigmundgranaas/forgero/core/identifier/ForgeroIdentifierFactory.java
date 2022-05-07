package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.identifier.model.ForgeroModelIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroMaterialIdentifier;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import net.minecraft.util.Identifier;

public interface ForgeroIdentifierFactory {
    ForgeroIdentifierFactory INSTANCE = ForgeroIdentifierFactoryImpl.getInstance();

    ForgeroIdentifier createForgeroIdentifier(Identifier identifier);

    ForgeroModelIdentifier createToolPartModelIdentifier(Gem gem, ForgeroToolPart part, ToolPartModelType type);

    ForgeroIdentifier createForgeroIdentifier(String identifier);

    ForgeroMaterialIdentifier createForgeroMaterialIdentifier(String identifier);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart, SecondaryMaterial secondaryMaterial);

    ForgeroModelIdentifier createToolPartModelIdentifier(ForgeroToolTypes toolType, ForgeroToolPart toolPart, SecondaryMaterial secondaryMaterial);
}
