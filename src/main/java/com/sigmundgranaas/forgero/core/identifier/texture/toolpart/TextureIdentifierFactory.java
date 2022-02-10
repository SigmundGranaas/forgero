package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

import java.util.Optional;

public interface TextureIdentifierFactory {
    ToolPartModelTextureIdentifier createToolPartTextureIdentifier(ForgeroToolPart part);

    Optional<ToolPartModelTextureIdentifier> createToolPartTextureIdentifier(String identifier);
}
