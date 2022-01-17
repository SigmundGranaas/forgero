package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

public interface TextureLoader {
    Texture getResource(TextureIdentifier id);
}
