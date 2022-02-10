package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;

public interface ToolPartTextureService {
    Texture getTexture(ToolPartModelTextureIdentifier id);
}
