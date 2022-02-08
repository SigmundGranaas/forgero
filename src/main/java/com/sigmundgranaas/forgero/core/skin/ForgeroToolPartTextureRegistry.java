package com.sigmundgranaas.forgero.core.skin;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import io.netty.util.internal.ConcurrentSet;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ForgeroToolPartTextureRegistry {
    private static ForgeroToolPartTextureRegistry INSTANCE;
    Set<String> toolPartTextureGenerationExclusionList;
    Set<String> toolPartTextures;
    TextureIdentifierFactory factory;

    public ForgeroToolPartTextureRegistry(TextureIdentifierFactory factory, Set<String> toolPartTextureGenerationExclusionList) {
        this.factory = factory;
        this.toolPartTextures = new ConcurrentSet<>();
        this.toolPartTextureGenerationExclusionList = toolPartTextureGenerationExclusionList;
    }

    public static ForgeroToolPartTextureRegistry getInstance(TextureIdentifierFactory factory) {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolPartTextureRegistry(factory, new HashSet<>());
        }
        return INSTANCE;
    }

    public ToolPartModelTextureIdentifier registerTexture(ToolPartModelTextureIdentifier textureIdentifier) {
        toolPartTextures.add(textureIdentifier.getIdentifier());
        return textureIdentifier;
    }

    public List<ToolPartModelTextureIdentifier> getTextures() {
        return toolPartTextures.stream().map(factory::createToolPartTextureIdentifier).filter(Optional::isPresent).map(Optional::get).toList();
    }

    public Optional<ToolPartModelTextureIdentifier> getTexture(String textureIdentifier) {
        if (toolPartTextures.contains(textureIdentifier)) {
            return factory.createToolPartTextureIdentifier(textureIdentifier);
        }
        return Optional.empty();
    }

    public boolean isGeneratedTexture(ToolPartModelTextureIdentifier textureIdentifier) {
        return !toolPartTextureGenerationExclusionList.contains(textureIdentifier.getIdentifier());
    }
}
