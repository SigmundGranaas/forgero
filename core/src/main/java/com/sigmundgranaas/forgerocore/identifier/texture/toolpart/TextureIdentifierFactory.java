package com.sigmundgranaas.forgerocore.identifier.texture.toolpart;

import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;

import java.util.Optional;

public interface TextureIdentifierFactory {
    ToolPartModelTextureIdentifier createToolPartTextureIdentifier(ForgeroToolPart part);

    Optional<ToolPartModelTextureIdentifier> createToolPartTextureIdentifier(String identifier);
}
