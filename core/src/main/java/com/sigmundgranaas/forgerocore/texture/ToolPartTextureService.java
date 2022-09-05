package com.sigmundgranaas.forgerocore.texture;

import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.ToolPartModelTextureIdentifier;

/**
 * Service for retrieving a Texture for any given Forgero Tool Part.
 * <p>
 * The service will decide if the requested texture needs to be loaded from a file, generated or fetched from a cache.
 * <p>
 * Most tool part textures does not have pre-made textures. Generating textures allows you to manage a huge amount of tool parts/materials/schematics.
 * Forgero is uses a set of "base" textures will contain a certain set of grey scale values. These templates will be parsed and colorized by a palette from a material.json.
 * Palettes are generated from existing textures. Most wood types uses Minecraft's boat texture, which contains a nice range of colors to represent the material.json in tool parts.
 */
public interface ToolPartTextureService {
    Texture getTexture(ToolPartModelTextureIdentifier id);

    void clearCache();
}
