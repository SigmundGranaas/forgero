package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;

public interface TextureLoader {
    Texture getResource(TextureIdentifier id);

    Texture getResource(PaletteIdentifier id);
}
