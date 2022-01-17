package com.sigmundgranaas.forgero.core.identifier.texture.toolpart;

import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface TextureIdentifierFactory {
    ToolPartTextureIdentifier createToolPartTextureIdentifier(ForgeroToolPart part);

    Optional<ToolPartTextureIdentifier> createToolPartTextureIdentifier(Identifier part);
}
