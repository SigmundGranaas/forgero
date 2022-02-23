package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;

/**
 * Service for retrieving a Texture for any given Forgero Tool Part.
 *
 * The service will decide if the requested texture needs to be loaded from a file, generated or fetched from a cache.
 */
public interface ToolPartTextureService {
    Texture getTexture(ToolPartModelTextureIdentifier id);
}
