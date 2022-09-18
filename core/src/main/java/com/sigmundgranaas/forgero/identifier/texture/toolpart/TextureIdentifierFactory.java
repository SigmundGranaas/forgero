package com.sigmundgranaas.forgero.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;

import java.util.Optional;

public interface TextureIdentifierFactory {
    ToolPartModelTextureIdentifier createToolPartTextureIdentifier(ForgeroToolPart part);

    Optional<ToolPartModelTextureIdentifier> createToolPartTextureIdentifier(String identifier);
}
