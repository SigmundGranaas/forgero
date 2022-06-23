package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for managing registered textures.
 * If Textures are excluded from being generated from a palette, they need to be registered here first
 */
public class ForgeroToolPartTextureRegistry {
    private static ForgeroToolPartTextureRegistry INSTANCE;
    private final Set<String> toolPartTextureGenerationExclusionList;
    private final Set<String> toolPartTextures;
    private final TextureIdentifierFactory factory;

    public ForgeroToolPartTextureRegistry(TextureIdentifierFactory factory, Set<String> toolPartTextureGenerationExclusionList) {
        this.factory = factory;
        this.toolPartTextures = new HashSet<>();
        this.toolPartTextureGenerationExclusionList = toolPartTextureGenerationExclusionList;
    }

    public static ForgeroToolPartTextureRegistry getInstance(TextureIdentifierFactory factory) {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolPartTextureRegistry(factory, new HashSet<>());
        }
        return INSTANCE;
    }

    public void registerTexture(ToolPartModelTextureIdentifier textureIdentifier) {
        toolPartTextures.add(textureIdentifier.getIdentifier());
    }

    public boolean isRegistered(String texture) {
        return toolPartTextures.contains(texture);
    }

    public void registerTexture(String texture) {
        toolPartTextures.add(texture);
    }

    public List<ToolPartModelTextureIdentifier> getTextures() {
        return toolPartTextures.stream().map(factory::createToolPartTextureIdentifier).filter(Optional::isPresent).map(Optional::get).toList();
    }

    public boolean isGeneratedTexture(ToolPartModelTextureIdentifier textureIdentifier) {
        return !toolPartTextureGenerationExclusionList.contains(textureIdentifier.getIdentifier());
    }
}
