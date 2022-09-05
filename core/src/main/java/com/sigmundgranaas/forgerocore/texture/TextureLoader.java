package com.sigmundgranaas.forgerocore.texture;

import com.sigmundgranaas.forgerocore.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteIdentifier;

/**
 * Interface for abstracting away the fetching of resources.
 * How textures are fetched might rely on a running instance of Minecraft, and therefore can't be expressed in the core package
 */
public interface TextureLoader {
    Texture getResource(TextureIdentifier id);

    Texture getResource(PaletteIdentifier id);
}
