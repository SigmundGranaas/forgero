package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartTextureIdentifier;

public interface ToolPartTextureService {
    Texture getTexture(ToolPartTextureIdentifier id);
}
